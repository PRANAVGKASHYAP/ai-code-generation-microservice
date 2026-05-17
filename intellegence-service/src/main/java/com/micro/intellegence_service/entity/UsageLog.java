package com.micro.intellegence_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "usage_logs" , uniqueConstraints = @UniqueConstraint(columnNames = {"userId" , "date"} ))
public class UsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "user_id" , nullable = false)
    Long userId;
    LocalDate date;
    Integer tokensUsed;
}
