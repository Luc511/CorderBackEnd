package be.technobel.corder.pl.models.dtos;

import be.technobel.corder.dl.models.Participation;
import lombok.Builder;

@Builder
public record AddressDTO(
        String street,
        String city,
        String postCode
) {
    public static AddressDTO fromEntity(Participation participation) {
        return AddressDTO.builder()
                .street(participation.getAddress().getStreet())
                .city(participation.getAddress().getCity())
                .postCode(String.valueOf(participation.getAddress().getPostCode()))
                .build();
    }
}
