package be.technobel.corder.pl.models.forms;

import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParticipationForm(
        @NotBlank(message = "First name cannot be blank") String firstName,
        @NotBlank(message = "Last name cannot be blank") String lastName,
        @NotBlank(message = "Email cannot be blank") String email,
        @NotNull(message = "Status cannot be blank") Status status,
        @NotBlank(message = "Product type cannot be blank") String productType,
        @NotBlank(message = "Street cannot be blank") String street,
        @NotBlank(message = "City cannot be blank") String city,
        @Min(value = 0, message = "Post code should not be less than 0")
        @Max(value = 99999, message = "Post code should not be greater than 99999") int postCode,
        boolean acceptNewsletter,
        boolean acceptExposure
) {
        public Participation toEntity() {
                return Participation.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .status(status)
                        .productType(productType)
                        .address(Address.builder()
                                .city(city)
                                .street(street)
                                .postCode(postCode)
                                .build())
                        .acceptNewsletter(acceptNewsletter)
                        .acceptExposure(acceptExposure)
                        .build();
        }
}
