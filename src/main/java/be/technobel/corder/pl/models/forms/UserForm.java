package be.technobel.corder.pl.models.forms;

import be.technobel.corder.dl.models.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserForm(
    @NotBlank(message = "Login is mandatory")
    String login,

    @NotBlank(message = "Password is mandatory")
    @Size(min=8, message = "Password must be at least 8 characters long")
    String password,

    Set<Role> roles
) {
}