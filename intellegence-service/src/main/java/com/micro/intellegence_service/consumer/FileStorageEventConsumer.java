package com.micro.intellegence_service.consumer;


import com.micro.common_lib.enums.ChatEventStatus;
import com.micro.common_lib.event.FileStoreResponseEvent;
import com.micro.intellegence_service.entity.ChatEvent;
import com.micro.intellegence_service.repository.ChatEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageEventConsumer {

    // this will check based on the saga id of the file storage events
    // this consumer is to check if the file edit events are stored in the db or not by the workspace service
    private final KafkaTemplate kafkaTemplate;
    private final ChatEventRepository  chatEventRepository;

    @Transactional
    @KafkaListener(topics = "file-storage-response-event" , groupId = "intellegence-service-group")
    public void listenToFileStorageResponse(FileStoreResponseEvent responseEvent){
        // 1. retrieve the chat event by the saga id
        //2. check its status success aldready? 
        //3. if it pending of failed take corresponding action
        
        chatEventRepository.findBySagaId(responseEvent.sagaId()).ifPresent(ele ->{
            if(! ChatEventStatus.PENDING.equals(ele.getStatus())){
                log.info("this event is aldready handeled ");
                return;
            }

            if(responseEvent.success()){
                ele.setStatus(ChatEventStatus.COMPLETED);
            }else{
                ele.setStatus(ChatEventStatus.FAILED);
            }

            chatEventRepository.save(ele); // save the chat event object if it has been updated
        });
    }

}
