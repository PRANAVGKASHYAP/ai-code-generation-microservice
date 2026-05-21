package com.micro.workspace_service.service.impl;//package com.micro.workspace_service.service.impl;
//
//
//import com.micro.workspace_service.dto.DeployResponse;
//import com.micro.workspace_service.service.DeploymentService;
//import io.fabric8.kubernetes.api.model.Pod;
//import io.fabric8.kubernetes.client.KubernetesClient;
//import io.fabric8.kubernetes.client.dsl.ExecListener;
//import io.fabric8.kubernetes.client.dsl.ExecWatch;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.util.HashMap;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DeploymentServiceImpl implements DeploymentService {
//
//    private final KubernetesClient kubernetesClient;
//    private final StringRedisTemplate redisTemplate;
//
//    String NAMESPACE  = "lovable-previews";
//    String POOL_LABEL = "status";
//    String PROJECT_LABEL = "project-id";
//    String IDLE = "idle"; // this is the status of teh container , and will be changed by the java code
//    String BUSY = "busy";
//
//
//    public void registerRoute(Pod pod , String domain){
//        String podIp = pod.getStatus().getPodIP();
//        if (podIp == null | podIp.isEmpty()){
//            throw new RuntimeException("pod " + pod.getMetadata().getName()+ " exists but it has no ip assigned to it ");
//        }
//
//        // register this ip to the redis db
//        redisTemplate.opsForValue().set("route:" + domain , podIp + ":5173" , 6 , TimeUnit.HOURS);
//    }
//
//    @Override
//    public DeployResponse deploy(Long projectId) {
//
//        String domain = "project-" + projectId + "previews.kashyapcode.in";
//
//        // first get the pod that is active and check if this app is already running on that pod or not
//        Pod currPod = findExistingAndActivePod(projectId);
//
//        log.info("Existing pod found: {}", currPod != null ? currPod.getMetadata().getName() : "null");
//
//
//        if(currPod != null){
//            log.info("Project {} already deployed on pod {}", projectId, currPod.getMetadata().getName());
//            registerRoute(currPod , domain);
//            return new DeployResponse("http://" + domain + ":8090");
//        }
//
//        log.info("No existing pod found, claiming new pod for project {}", projectId);
//
//        return claimAndRunPod(projectId);
//    }
//
//
//
//    public DeployResponse claimAndRunPod(Long projectId){
//        Pod activePod = kubernetesClient.pods().inNamespace(NAMESPACE)
//                .withLabel(POOL_LABEL , IDLE)
//                .list().getItems().stream()
//                .findFirst().orElseThrow(
//                        () -> new RuntimeException("no idle pods at this time ")
//                );
//
//
//        String podName = activePod.getMetadata().getName();
//        log.info("claiming the pod with the mane " + podName + " assigned to project id " + projectId);
//
//        // claim this pod by changing its status
//        kubernetesClient.pods()
//                .inNamespace(NAMESPACE)
//                .withName(podName)
//                .edit(p -> {
//                    if (p.getMetadata().getLabels() == null) {
//                        p.getMetadata().setLabels(new HashMap<>());
//                    }
//
//                    p.getMetadata().getLabels().put(POOL_LABEL, BUSY);
//                    p.getMetadata().getLabels().put(PROJECT_LABEL, projectId.toString());
//
//                    return p;
//                });
//
//        // initial sync command
//        String initialSync = String.format(
//                "mc mirror --overwrite myminio/projects/%d/ /app/" , projectId
//        );
//        log.info("Starting initial sync for project {} in pod {}", projectId, podName);
//        execCommand(podName, "syncer", "sh", "-c", initialSync);
//
//        String watcherCommand = String.format(
//                "nohup mc mirror --overwrite --watch myminio/projects/%d/ /app/ > /app/sync.log 2>&1 &",
//                projectId);
//
//        String initialCommand = String.format(
//                "rm -rf /app/* && mc mirror --overwrite myminio/projects/%d/ /app/ 2>&1" , projectId
//        );
//
//        //execCommand(podName , "syncer" , "sh" , "-c" , initialCommand);
//        execCommand(podName , "syncer" , "sh" , "-c" , watcherCommand);
//
//        String startCommand ="npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 &";
//        execCommand(podName , "runner" , "sh" , "-c" , startCommand);
//        String domain = "project-" + projectId + "previews.kashyapcode.in";
//        registerRoute(activePod , domain);
//        return new DeployResponse("http://" + domain + ":8090");
//
//    }
//
//
//    private void execCommand(String podName, String container, String... command) {
//        log.debug("Exec in {}:{} -> {}", podName, container, String.join(" ", command));
//
//        CompletableFuture<String> data = new CompletableFuture<>();
//        try (ExecWatch ignored = kubernetesClient.pods().inNamespace(NAMESPACE).withName(podName)
//                .inContainer(container)
//                .writingOutput(new ByteArrayOutputStream())
//                .writingError(new ByteArrayOutputStream())
//                .usingListener(new ExecListener() {
//                    @Override
//                    public void onClose(int code, String reason) {
//                        data.complete("Done");
//                    }
//                })
//                .exec(command)) {
//
//            // Wait briefly to ensure command fired (Fabric8 exec is async)
//            // For long running background jobs (nohup), we don't wait for "Done"
//            if (command[command.length - 1].trim().endsWith("&")) {
//                Thread.sleep(500);
//            } else {
//                data.get(120, TimeUnit.SECONDS); // Block for synchronous setup commands (npm install)
//            }
//
//        } catch (Exception e) {
//            log.error("Exec failed", e);
//            throw new RuntimeException("Pod Execution Failed", e);
//        }
//    }
//
//
//
//    public Pod findExistingAndActivePod(Long projectId){
//        return kubernetesClient.pods().inNamespace(NAMESPACE)
//                .withLabel(PROJECT_LABEL , projectId.toString())
//                .withLabel(POOL_LABEL , BUSY) // we want only the pods that are running(busy)
//                .list().getItems().stream()
//                .filter(ele -> ele.getStatus().getPhase().equals("Running"))
//                .findFirst()
//                .orElse(null);
//
//    }
//}


import com.micro.workspace_service.dto.DeployResponse;
import com.micro.workspace_service.service.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentServiceImpl implements DeploymentService {

    private final KubernetesClient client;
    private final StringRedisTemplate redisTemplate;


    private String namespace = "lovable-previews";


    private String baseDomain = "kashyapcode.in";


    private String proxyPort = "80";

    private static final String POOL_LABEL = "status";
    private static final String PROJECT_LABEL = "project-id";
    private static final String IDLE = "idle";
    private static final String BUSY = "busy";

    public DeployResponse deploy(Long projectId) {
        // Dynamically build the domain: project-123.app.domain.com
        String domain = "project-" + projectId + "." + baseDomain;

        // Use default port 80 format logic for clean URLs, or explicit ports for local testing
        String formattedUrl = proxyPort.equals("80")
                ? "http://" + domain
                : "http://" + domain + ":" + proxyPort;

        Pod existingPod = findActivePod(projectId);

        if (existingPod != null) {
            log.info("Found existing pod {} for project {}. Resuming...", existingPod.getMetadata().getName(), projectId);
            registerRoute(domain, existingPod);
            return new DeployResponse(formattedUrl);
        }

        return claimAndStartNewPod(projectId, domain, formattedUrl);
    }

    private Pod findActivePod(Long projectId) {
        return client.pods().inNamespace(namespace)
                .withLabel(PROJECT_LABEL, projectId.toString())
                .withLabel(POOL_LABEL, BUSY)
                .list().getItems().stream()
                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                .findFirst()
                .orElse(null);
    }

    private DeployResponse claimAndStartNewPod(Long projectId, String domain, String formattedUrl) {
        Pod pod = client.pods().inNamespace(namespace)
                .withLabel(POOL_LABEL, IDLE)
                .list().getItems().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No idle runners available. Please scale up the runner-pool."));

        String podName = pod.getMetadata().getName();
        log.info("Claiming pod {} for project {}", podName, projectId);

        client.pods().inNamespace(namespace).withName(podName).edit(p -> {
            p.getMetadata().getLabels().put(POOL_LABEL, BUSY);
            p.getMetadata().getLabels().put(PROJECT_LABEL, projectId.toString());
            return p;
        });

        try {
            String initialSyncCmd = String.format("rm -rf /app/* && mc mirror --overwrite myminio/projects/%d/ /app/", projectId);
            execCommand(podName, "syncer", "sh", "-c", initialSyncCmd);

            String watchCmd = String.format("nohup mc mirror --overwrite --watch myminio/projects/%d/ /app/ > /app/sync.log 2>&1 &", projectId);
            execCommand(podName, "syncer", "sh", "-c", watchCmd);

            String startCmd = "npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 &";
            execCommand(podName, "runner", "sh", "-c", startCmd);

            Pod updatedPod = client.pods().inNamespace(namespace).withName(podName).get();
            registerRoute(domain, updatedPod);

            log.info("Deployment successful: {}", formattedUrl);
            return new DeployResponse(formattedUrl);

        } catch (Exception e) {
            log.error("Deployment failed for project {}. Releasing pod {}.", projectId, podName, e);
            client.pods().inNamespace(namespace).withName(podName).delete();
            throw new RuntimeException("Failed to deploy project " + projectId + ": " + e.getMessage(), e);
        }
    }

    private void registerRoute(String domain, Pod pod) {
        String podIp = pod.getStatus().getPodIP();
        if (podIp == null) throw new RuntimeException("Pod is running but has no IP!");

        redisTemplate.opsForValue().set("route:" + domain, podIp + ":5173", 6, TimeUnit.HOURS);
        log.info("Route Registered: {} -> {}", domain, podIp);
    }

    private void execCommand(String podName, String container, String... command) {
        log.debug("Exec in {}:{} -> {}", podName, container, String.join(" ", command));

        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch ignored = client.pods().inNamespace(namespace).withName(podName)
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

            if (command[command.length - 1].trim().endsWith("&")) {
                Thread.sleep(500);
            } else {
                data.get(30, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            log.error("Exec failed", e);
            throw new RuntimeException("Pod Execution Failed", e);
        }
    }


}
