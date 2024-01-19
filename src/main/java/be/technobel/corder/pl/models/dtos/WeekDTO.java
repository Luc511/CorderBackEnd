package be.technobel.corder.pl.models.dtos;

import lombok.Builder;

@Builder
public record WeekDTO(
        Long[] days
) {
}
