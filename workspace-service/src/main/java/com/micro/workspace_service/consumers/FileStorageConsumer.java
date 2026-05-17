package com.micro.workspace_service.consumers;

import com.micro.common_lib.event.FileStoreRequestEvent;
import com.micro.common_lib.event.FileStoreResponseEvent;
import com.micro.workspace_service.entity.ProcessedEvents;
import com.micro.workspace_service.repository.ProcessedEventsRepository;
import com.micro.workspace_service.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageConsumer {
    // this will listen to the file store request event produced by the intellegence service
    private final FileService fileService;
    private final ProcessedEventsRepository processedEventsRepository;
    private final KafkaTemplate kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "file-storage-event" , groupId = "workspace-service-group")
    public void consumeFileStorageEvent(FileStoreRequestEvent requestEvent){
        String filePath = requestEvent.filePath();
        String fileContent = requestEvent.content();
        Long projectID = requestEvent.projectId();

        // here in the file save call send the file in the file store state

        //1. check for idempotency using the saga id
        if(processedEventsRepository.existsById(requestEvent.sagaId())){
            log.info("this event with this id already processed the id is " + requestEvent.sagaId());
            sendFileStorageStatus(requestEvent , true , null);
        }
        //2. save the file and send the status
        try {
            log.info("Saving the file " + filePath);
            fileService.saveFile(projectID , filePath , fileContent);

            processedEventsRepository.save(ProcessedEvents.builder()
                            .id(requestEvent.sagaId())
                            .processedTime(LocalDateTime.now())
                            .build());
            // on storing th key using the event repository , we maintain idempotency
            sendFileStorageStatus(requestEvent , true , null);
        }catch (Exception e){
            log.info("error saving the file , failed with the error " + e.getMessage());
            sendFileStorageStatus(requestEvent , false , e.getMessage());
        }
    }

    private void sendFileStorageStatus(FileStoreRequestEvent requestEvent , Boolean success , String error) {
        FileStoreResponseEvent fileStoreResponseEvent = new FileStoreResponseEvent(requestEvent.sagaId() , success , requestEvent.projectId() , error);
        kafkaTemplate.send("file-storage-response-event" , fileStoreResponseEvent);
    }
}
