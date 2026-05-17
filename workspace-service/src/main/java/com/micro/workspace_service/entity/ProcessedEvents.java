package com.micro.workspace_service.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class ProcessedEvents {
    @Id
    private String id; // this is the sage id
    private LocalDateTime processedTime;

}
