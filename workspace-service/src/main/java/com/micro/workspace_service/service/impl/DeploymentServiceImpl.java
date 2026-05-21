package com.micro.workspace_service.service.impl;


import com.micro.workspace_service.dto.DeployResponse;
import com.micro.workspace_service.service.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentServiceImpl implements DeploymentService {

    private final KubernetesClient kubernetesClient;
    private final StringRedisTemplate redisTemplate;

    String NAMESPACE  = "lovable-previews";
    String POOL_LABEL = "status";
    String PROJECT_LABEL = "project-id";
    String IDLE = "idle"; // this is the status of teh container , and will be changed by the java code
    String BUSY = "busy";


    public void registerRoute(Pod pod , String domain){
        String podIp = pod.getStatus().getPodIP();
        if (podIp == null | podIp.isEmpty()){
            throw new RuntimeException("pod " + pod.getMetadata().getName()+ " exists but it has no ip assigned to it ");
        }

        // register this ip to the redis db
        redisTemplate.opsForValue().set("route:" + domain , podIp + ":5173" , 6 , TimeUnit.HOURS);
    }

    @Override
    public DeployResponse deploy(Long projectId) {

        String domain = "project-" + projectId + "previews.kashyapcode.in";

        // first get the pod that is active and check if this app is already running on that pod or not
        Pod currPod = findExistingAndActivePod(projectId);

        log.info("Existing pod found: {}", currPod != null ? currPod.getMetadata().getName() : "null");


        if(currPod != null){
            log.info("Project {} already deployed on pod {}", projectId, currPod.getMetadata().getName());
            registerRoute(currPod , domain);
            return new DeployResponse("http://" + domain + ":8090");
        }

        log.info("No existing pod found, claiming new pod for project {}", projectId);

        return claimAndRunPod(projectId);
    }



    public DeployResponse claimAndRunPod(Long projectId){
        Pod activePod = kubernetesClient.pods().inNamespace(NAMESPACE)
                .withLabel(POOL_LABEL , IDLE)
                .list().getItems().stream()
                .findFirst().orElseThrow(
                        () -> new RuntimeException("no idle pods at this time ")
                );


        String podName = activePod.getMetadata().getName();
        log.info("claiming the pod with the mane " + podName + " assigned to project id " + projectId);

        // claim this pod by changing its status
        kubernetesClient.pods()
                .inNamespace(NAMESPACE)
                .withName(podName)
                .edit(p -> {
                    if (p.getMetadata().getLabels() == null) {
                        p.getMetadata().setLabels(new HashMap<>());
                    }

                    p.getMetadata().getLabels().put(POOL_LABEL, BUSY);
                    p.getMetadata().getLabels().put(PROJECT_LABEL, projectId.toString());

                    return p;
                });

        // initial sync command
        String initialSync = String.format(
                "mc mirror --overwrite myminio/projects/%d/ /app/" , projectId
        );
        log.info("Starting initial sync for project {} in pod {}", projectId, podName);
        execCommand(podName, "syncer", "sh", "-c", initialSync);

        String watcherCommand = String.format(
                "nohup mc mirror --overwrite --watch myminio/projects/%d/ /app/ > /app/sync.log 2>&1 &",
                projectId);

        String initialCommand = String.format(
                "rm -rf /app/* && mc mirror --overwrite myminio/projects/%d/ /app/ 2>&1" , projectId
        );

        //execCommand(podName , "syncer" , "sh" , "-c" , initialCommand);
        execCommand(podName , "syncer" , "sh" , "-c" , watcherCommand);

        String startCommand ="npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 &";
        execCommand(podName , "runner" , "sh" , "-c" , startCommand);
        String domain = "project-" + projectId + "app.domain.com";
        registerRoute(activePod , domain);
        return new DeployResponse("http://" + domain + ":8090");

    }


    private void execCommand(String podName, String container, String... command) {
        log.debug("Exec in {}:{} -> {}", podName, container, String.join(" ", command));

        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch ignored = kubernetesClient.pods().inNamespace(NAMESPACE).withName(podName)
                .inContainer(container)
                .writingOutput(new ByteArrayOutputStream())
                .writingError(new ByteArrayOutputStream())
                .usingListener(new ExecListener() {
                    @Override
                    public void onClose(int code, String reason) {
                        data.complete("Done");
                    }
                })
                .exec(command)) {

            // Wait briefly to ensure command fired (Fabric8 exec is async)
            // For long running background jobs (nohup), we don't wait for "Done"
            if (command[command.length - 1].trim().endsWith("&")) {
                Thread.sleep(500);
            } else {
                data.get(120, TimeUnit.SECONDS); // Block for synchronous setup commands (npm install)
            }

        } catch (Exception e) {
            log.error("Exec failed", e);
            throw new RuntimeException("Pod Execution Failed", e);
        }
    }



    public Pod findExistingAndActivePod(Long projectId){
        return kubernetesClient.pods().inNamespace(NAMESPACE)
                .withLabel(PROJECT_LABEL , projectId.toString())
                .withLabel(POOL_LABEL , BUSY) // we want only the pods that are running(busy)
                .list().getItems().stream()
                .filter(ele -> ele.getStatus().getPhase().equals("Running"))
                .findFirst()
                .orElse(null);

    }
}
