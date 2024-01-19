package be.technobel.corder.pl.models.dtos;

import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;

import java.time.LocalDate;

public record ParticipationDTO(
        Long id,
        String participantLastName,
        String participantFirstName,
        LocalDate participationDate,
        AddressDTO participantAddress,
        String productType,
        Status status
) {
    public static ParticipationDTO fromEntity(Participation participation) {
        return new ParticipationDTO(
                participation.getId(),
                participation.getLastName(),
                participation.getFirstName(),
                participation.getParticipationDate(),
                AddressDTO.fromEntity(participation),
                participation.getProductType(),
                participation.getStatus()
        );
    }
}
