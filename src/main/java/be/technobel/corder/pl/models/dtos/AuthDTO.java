package be.technobel.corder.pl.models.dtos;

import be.technobel.corder.dl.models.enums.Role;
import lombok.Builder;

import java.util.Set;

@Builder
public record AuthDTO(
        String login,
        String token,
        Set<Role> roles
) {
}
