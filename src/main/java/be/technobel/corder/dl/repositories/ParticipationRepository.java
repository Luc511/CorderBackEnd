package be.technobel.corder.dl.repositories;

import be.technobel.corder.dl.models.Participation;
import jakarta.mail.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Long countParticipationsByParticipationDate (LocalDate participationDate);
    Long countAllByIdIsNotNull ();
    Long countAllByParticipationDateBetween(LocalDate startDate, LocalDate endDate);
    Long countParticipationByAddress_PostCodeBetween(int address_startPostCode, int address_endPostCode);
    Long countParticipationByProductType(String productType);
    List<Participation> findAllByProductTypeNotIn(Collection<String> productType);
    Long countParticipationBySatisfaction(int satisfaction);
    Long countParticipationBySatisfactionCommentIgnoreCase(String satisfactionComment);
    List<Participation> findAllBySatisfactionCommentNotIn(Collection<String> satisfactionComment);

}