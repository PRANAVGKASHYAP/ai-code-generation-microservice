package com.micro.workspace_service.entity;

import com.micro.common_lib.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
@Table(name = "project_members")
public class ProjectMember {

    @EmbeddedId
    ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("userId")
//    @JoinColumn(name = "user_id", nullable = false)
//    User user; // the user can be fetched from the projectMemberID entity as it has the user id key

    @Enumerated
    @Column(nullable = false)
    ProjectRole role;

    @CreationTimestamp
    Instant invitedAt;
    Instant acceptedAt;

}
