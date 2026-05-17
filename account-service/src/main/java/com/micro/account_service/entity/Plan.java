package com.micro.account_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // pro , business etc
    @Column(unique = true)
    private String stripePriceId;
    private Integer maxProjects;
    private Integer maxTokensPerDay;
    private Boolean unlimitedAi;
    private Boolean active;
}
