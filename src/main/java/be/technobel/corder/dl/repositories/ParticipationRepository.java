package be.technobel.corder.dl.repositories;

import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    int countParticipationsByParticipationDate (LocalDate participationDate);
}