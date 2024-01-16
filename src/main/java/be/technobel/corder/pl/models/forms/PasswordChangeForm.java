package be.technobel.corder.pl.models.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeForm(
        @NotBlank(message = "Login is mandatory")
        String login,
        @NotBlank(message = "Current password is mandatory")
        String currentPassword,
        @NotBlank(message = "New password is mandatory")
        @Size(min=8, message = "New password must be at least 8 characters long")
        String newPassword
) {
}
