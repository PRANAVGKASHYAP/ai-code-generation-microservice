package com.micro.intellegence_service.entity;


import com.micro.common_lib.enums.ChatEventStatus;
import com.micro.common_lib.enums.ChatEventType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatEvent {

    // this entity is mainly to handel teh llm response , in one lm response there may be many events like tool call , files edits , or plain messages
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    ChatMessage chatMessage;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ChatEventType chatEventType; //this is like file edit / tool call / normal messsage etc ...
    Integer sequenceOrder;
    @Column(columnDefinition = "text")
    String content; // this content will be used only for the llm response and will be null for the user
    String filePath; // only populated when file is added
    String metadata;

    //adding a saga id so that the file storage event requests can be tracked for idempotency
    String sagaId;

    @Enumerated(EnumType.STRING)
    ChatEventStatus status; // this can also be used to check the state of the file storage event
}
