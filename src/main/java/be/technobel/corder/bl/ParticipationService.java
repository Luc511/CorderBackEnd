package be.technobel.corder.bl;

import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.pl.models.forms.ParticipationForm;

import java.util.List;

public interface ParticipationService {
    Participation create(ParticipationForm participationForm);
    List<Participation> findAll();
    Participation findById(Long id);
}
