package be.technobel.corder.pl.models.dtos;

import lombok.Builder;

@Builder
public record DashboardDTO(
        Long totalParticipants,
        Long[] week,
        Long[] last3Pending,
        Long[] last3Validated
) {
}
