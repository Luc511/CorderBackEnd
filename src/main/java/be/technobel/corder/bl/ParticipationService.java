package be.technobel.corder.bl;

import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ParticipationService {
    Participation create(ParticipationForm participationForm);
    List<Participation> findAll();
    Participation findById(Long id);
    Participation addPhoto(MultipartFile photo, Long id);
    Participation addSatisfaction(SatisfactionForm satisfactionForm);
    void validate(Long id);
    void deny(Long id);
    void ship(Long id);
    int[] getWeek(LocalDate firstDay);
}
