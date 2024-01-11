package be.technobel.corder.pl.models.forms;

import be.technobel.corder.dl.models.Participation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SatisfactionForm(
        @NotNull(message = "id cannot be null")
        Long id,
        @Min(value = 0, message = "minimum 0")
        @Max(value = 5, message = "maximum 5") //TODO: max 5 or max 3?
        int satisfaction,
        /**
         * satisfaction comment can be null
         */
        String satisfactionComment
) {
}
