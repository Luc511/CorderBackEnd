package be.technobel.corder.bl.services;

import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.pl.models.dtos.DashboardDTO;
import be.technobel.corder.pl.models.dtos.StatsDTO;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ParticipationService {
    Participation create(ParticipationForm participationForm);

    List<Participation> findAll();

    Participation findById(Long id);

    void addPhoto(MultipartFile photo, Long id);

    void addSatisfaction(SatisfactionForm satisfactionForm);

    void validate(Long id);

    void deny(Long id);

    void ship(Long id);

    Long[] getWeek(LocalDate firstDay);

    Map<String, Long> getWeekWithDays();

    Long countParticipation();

    Long[] countParticipationLast5Months();

    Map<String, Long> countByProvince();

    Map<String, Long> countByProductType();

    List<String> otherProductType();

    Long[] countNotes();

    Map<String, Long> countSatisfactionComments();

    List<String> allOtherSatisfactionComments();

    StatsDTO statsDTOBuilder();

    Long[] last3Pending();

    Long[] last3Validated();

    DashboardDTO dashboardDTOBuilder();

    Participation findByEmail(String email);
}
