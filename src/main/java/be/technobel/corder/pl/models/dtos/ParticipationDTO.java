package be.technobel.corder.pl.models.dtos;

import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.ProductType;
import be.technobel.corder.dl.models.enums.Status;

import java.time.LocalDate;

public record ParticipationDTO(
        Long id,
        String lastName,
        String firstName,
        LocalDate participationDate,
        Address address,
        String productType,
        Status status
) {
    public static ParticipationDTO fromEntity(Participation participation) {
        return new ParticipationDTO(
                participation.getId(),
                participation.getLastName(),
                participation.getFirstName(),
                participation.getParticipationDate(),
                participation.getAddress(),
                participation.getProductType(),
                participation.getStatus()
        );
    }
}
