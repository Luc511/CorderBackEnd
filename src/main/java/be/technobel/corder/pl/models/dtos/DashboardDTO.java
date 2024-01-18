package be.technobel.corder.pl.models.dtos;

import lombok.Builder;

import java.util.Map;

@Builder
public record DashboardDTO(
        Long countParticipants,
        Map<String, Long> days,
        Long[] lastThreePending,
        Long[] lastThreeValidated
) {
}