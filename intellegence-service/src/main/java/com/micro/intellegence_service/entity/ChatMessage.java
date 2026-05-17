package com.micro.intellegence_service.entity;

import com.micro.common_lib.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
            @JoinColumns({
                    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false),
                    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
            }
            )
    ChatSession chatSession;

    @OneToMany(mappedBy = "chatMessage" , cascade = CascadeType.ALL , fetch = FetchType.LAZY)
            @OrderBy("sequenceOrder ASC")
    List<ChatEvent> events;

    @Column(columnDefinition = "text" )
    String content; // this is the user message
    Integer tokensUsed;
    @CreationTimestamp
    Instant createdAt;
    MessageRole messageRole; // USER OR ASSISTANT
}
