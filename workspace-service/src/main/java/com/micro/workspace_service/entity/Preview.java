package com.micro.workspace_service.entity;

import com.micro.common_lib.enums.PreviceStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Preview {
    Long id;
    Project project;
    String namespace;
    String podName;
    String previewUrl;

    Instant createdAt;
    Instant startedAt;
    Instant terminatedAt;
    PreviceStatus status;
}
