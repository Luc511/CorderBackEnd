package be.technobel.corder.pl.models.dtos;

import be.technobel.corder.dl.models.enums.ProductType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * A Data Transfer Object that encapsulates various statistics.
 *
 * @param totalParticipation The total number of participants.
 * @param totalParticipationLast5Months An array of counts of participants for each of the last 5 months. The most recent month is at index 0.
 * @param totalByProvince A map of counts of participants by province.
 * @param totalByProductType A map of counts for each type of product used.
 * @param otherProductNames An array containing the names of other products used.
 * @param notes An array of counts of participants by notes (ratings?), where the index corresponds to the note minus 1.
 * @param totalSatisfactionComment A map of counts of satisfaction comments.
 * @param otherSatisfactionComments An array containing all other satisfaction comments.
 */
@Builder
public record StatsDTO(
        Long totalParticipation,
        Long[] totalParticipationLast5Months,
        Map<String, Long> totalByProvince,
        Map<String, Long> totalByProductType,
        List<String> otherProductNames,
        Long[] notes,
        Map<String, Long> totalSatisfactionComment,
        List<String> otherSatisfactionComments
) {
}
