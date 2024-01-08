package be.technobel.corder.bl;

import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.pl.models.forms.ParticipationForm;

public interface ParticipationService {
    Participation create(ParticipationForm participationForm);
}
