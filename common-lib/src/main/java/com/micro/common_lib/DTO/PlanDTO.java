package com.micro.common_lib.DTO;

public record PlanDTO(
        Long id,
        String name,
        Integer maxProjects,
        Boolean unlimitedAi,
        Integer maxTokensPerDay,
        String price

) {
}
