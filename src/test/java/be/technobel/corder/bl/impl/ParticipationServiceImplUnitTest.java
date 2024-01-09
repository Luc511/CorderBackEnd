package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.dl.repositories.ParticipationRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateParticipationException;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ParticipationServiceImpl's 'create' method.
 */
@SpringBootTest
public class ParticipationServiceImplUnitTest {

    ParticipationRepository participationRepository;
    ParticipationServiceImpl participationService;

    @BeforeEach
    public void setUp() {
        participationRepository = mock(ParticipationRepository.class);
        participationService = new ParticipationServiceImpl(participationRepository);
    }

    @Test
    void testCreate() {

        ParticipationForm form = new ParticipationForm(
                "Alice",
                "Smith",
                "email@domain.com",
                Status.PENDING,
                "product",
                "street",
                "city",
                10000,
                true,
                true
        );
        when(participationRepository.save(any())).thenReturn(form.toEntity());

        Participation result = participationService.create(form);

        // assertions here
        assertEquals(form.email(), result.getEmail());
    }

    @Test
    void testDuplicateParticipantEmail() {
        ParticipationForm form = new ParticipationForm(
                "Alice",
                "Smith",
                "email@domain.com",
                Status.PENDING,
                "product",
                "street",
                "city",
                10000,
                true,
                true
        );
        when(participationRepository.findAll()).thenReturn(List.of(form.toEntity()));

        assertThrows(DuplicateParticipationException.class, () -> {
            participationService.create(form);
        });
    }

    @Test
    void testDuplicateParticipantAddress() {
        ParticipationForm form = new ParticipationForm(
                "Alice",
                "Smith",
                "secondEmail@domain.com",
                Status.PENDING,
                "product",
                "street",
                "city",
                10000,
                true,
                true
        );
        when(participationRepository.findAll()).thenReturn(List.of(form.toEntity()));

        assertThrows(DuplicateParticipationException.class, () -> {
            participationService.create(form);
        });
    }

    @Test
    void testFindAll() {
        Participation p1 = new Participation();
        Participation p2 = new Participation();
        List<Participation> expected = Arrays.asList(p1, p2);
        when(participationRepository.findAll()).thenReturn(expected);

        List<Participation> result = participationService.findAll();

        assertEquals(expected, result);
    }

}