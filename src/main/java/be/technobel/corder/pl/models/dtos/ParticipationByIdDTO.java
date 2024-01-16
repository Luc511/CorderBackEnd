package be.technobel.corder.pl.models.dtos;

import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.ProductType;
import be.technobel.corder.dl.models.enums.Status;

public record ParticipationByIdDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        Address address,
        Status status,
        String productType,
        boolean acceptNewsLetter,
        boolean acceptExposure
) {
    public static ParticipationByIdDTO fromEntity(Participation participation) {
        return new ParticipationByIdDTO(
                participation.getId(),
                participation.getFirstName(),
                participation.getLastName(),
                participation.getEmail(),
                participation.getAddress(),
                participation.getStatus(),
                participation.getProductType(),
                participation.isAcceptNewsletter(),
                participation.isAcceptExposure()
        );
    }
}
