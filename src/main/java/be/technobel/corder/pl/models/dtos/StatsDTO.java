package be.technobel.corder.pl.models.dtos;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * A Data Transfer Object that encapsulates various statistics.
 *
 * @param countParticipants The total number of participants.
 * @param countParticipantsEachLast5Months An array of counts of participants for each of the last 5 months. The most recent month is at index 0.
 * @param countByProvince A map of counts of participants by province.
 * @param productsUsed A map of counts for each type of product used.
 * @param otherProductsUsed An array containing the names of other products used.
 * @param countNotes An array of counts of participants by countNotes (ratings?), where the index corresponds to the note minus 1.
 * @param countSatisfactionComments A map of counts of satisfaction comments.
 * @param allOthersSatisfactionComment An array containing all other satisfaction comments.
 */
@Builder
public record StatsDTO(
        Long countParticipants,
        Long[] countParticipantsEachLast5Months,
        Map<String, Long> countByProvince,
        Map<String, Long> productsUsed,
        List<String> otherProductsUsed,
        Long[] countNotes,
        Map<String, Long> countSatisfactionComments,
        List<String> allOthersSatisfactionComment
) {
}
