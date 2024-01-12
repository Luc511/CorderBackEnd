package be.technobel.corder.pl.models.forms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginForm(
        @NotBlank(message = "Login is mandatory")
        String login,
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @NotBlank(message = "Password is mandatory")
        String password
) {
}
