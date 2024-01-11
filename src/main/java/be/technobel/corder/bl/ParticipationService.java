package be.technobel.corder.bl;

import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ParticipationService {
    Participation create(ParticipationForm participationForm);
    List<Participation> findAll();
    Participation findById(Long id);
    Participation addPhoto(MultipartFile photo, Long id);
    Participation addSatisfaction(SatisfactionForm satisfactionForm);
}
