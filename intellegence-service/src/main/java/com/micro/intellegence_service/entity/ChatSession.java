package com.micro.intellegence_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatSession {
    @EmbeddedId
    ChatSessionId id; // this only will have reference to the user and project id

//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("projectId")
//    @JoinColumn(name = "project_id")
//    Project project;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("userId")
//    @JoinColumn(name = "user_id")
//    User user;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt;
}
