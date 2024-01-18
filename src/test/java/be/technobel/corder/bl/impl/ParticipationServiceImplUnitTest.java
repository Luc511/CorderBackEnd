package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.MailService;
import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.dl.repositories.ParticipationRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateParticipationException;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ParticipationServiceImpl.
 */
@SpringBootTest
public class ParticipationServiceImplUnitTest {

    @Mock
    ParticipationRepository participationRepository;
    @InjectMocks
    ParticipationServiceImpl participationService;
    @Mock
    MailServiceImpl mailService;

    Participation participation;
    ParticipationForm participationForm;
    Address address;
    SatisfactionForm satisfactionForm;

    @BeforeEach
    public void setUp() {
        address = new Address(99L, "rue du paradis", "ciel", 5432);
        participation = new Participation(
                99L,
                LocalDate.now(),
                "Alice",
                "Smith",
                "alicesmith@gmail.com",
                address,
                Status.PENDING,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                true,
                true
                );
        participationForm = new ParticipationForm(
                participation.getFirstName(),
                participation.getLastName(),
                participation.getEmail(),
                participation.getStatus(),
                participation.getProductType(),
                participation.getAddress().getStreet(),
                participation.getAddress().getCity(),
                participation.getAddress().getPostCode(),
                participation.isAcceptNewsletter(),
                participation.isAcceptExposure()
        );
        satisfactionForm = new SatisfactionForm(
                99L,
                1,
                "très satisfait"
        );

    }

    @Test
    void testCreate() {
        when(participationRepository.save(any(Participation.class))).thenReturn(participation);

        Participation result = participationService.create(participationForm);

        // assertions here
        assertEquals(participation.getEmail(), result.getEmail());
        verify(participationRepository, times(1)).save(any(Participation.class));
    }
    @Test
    void testCreateDuplicateParticipantEmail() {
        when(participationRepository.findAll()).thenReturn(List.of(participation));

        Exception exception = assertThrows(DuplicateParticipationException.class, () -> {
            participationService.create(participationForm);
        });

        String expected = "Ce participant a déjà joué avec cet email !";
        String actual = exception.getMessage();

        assertTrue(actual.contains(expected));
    }

    @Test
    void testCreateDuplicateParticipantAddress() {
        participation.setEmail("alicesmith2@gmail.com");
        when(participationRepository.findAll()).thenReturn(List.of(participation));

        Exception exception = assertThrows(DuplicateParticipationException.class, () -> {
            participationService.create(participationForm);
        });

        String expected = "Ce foyer a déjà une participation !";
        String actual = exception.getMessage();

        assertTrue(actual.contains(expected));
    }
    @Test
    void testCreateDuplicateParticipantAddressWithSpaces() {
        participation.setEmail("alicesmith2@gmail.com");
        address.setStreet(" " + address.getStreet() + " ");
        address.setCity(" " + address.getCity() + " ");

        when(participationRepository.findAll()).thenReturn(List.of(participation));

        Exception exception = assertThrows(DuplicateParticipationException.class, () -> {
            participationService.create(participationForm);
        });

        String expected = "Ce foyer a déjà une participation !";
        String actual = exception.getMessage();

        assertTrue(actual.contains(expected));
    }
    @Test
    void testCreateDuplicateParticipantAddressWithUnderscores() {
        participation.setEmail("alicesmith2@gmail.com");

        address.setStreet("_" + address.getStreet() + "?");
        address.setCity("°" + address.getCity() + ")");

        when(participationRepository.findAll()).thenReturn(List.of(participation));

        Exception exception = assertThrows(DuplicateParticipationException.class, () -> {
            participationService.create(participationForm);
        });

        String expected = "Ce foyer a déjà une participation !";
        String actual = exception.getMessage();

        assertTrue(actual.contains(expected));
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

    @Test
    public void findById_existingId_shouldReturnParticipation() {

        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));

        Participation actualParticipation = participationService.findById(participation.getId());

        assertSame(participation, actualParticipation);
    }

    @Test
    void findById_nonExistingId_shouldThrowException() {
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> participationService.findById(participation.getId()));

        assertEquals(exception.getMessage(), "Participation avec l'id: " + participation.getId() + " introuvable");
    }
    @Test
    void testCreateDuplicateParticipantEmailAndAddressWithSpaces() {
        when(participationRepository.findAll()).thenReturn(List.of(participation));

        participation.setEmail(" alicesmith@gmail.com ");

        Address newAddress = new Address();
        newAddress.setStreet(" rue du paradis ");
        newAddress.setCity(" ciel ");
        newAddress.setPostCode(5432);

        participation.setAddress(newAddress);

        Exception exception = assertThrows(DuplicateParticipationException.class, () -> {
            participationService.create(participationForm);
        });

        String expected = "Ce participant a déjà joué avec cet email !";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    void testAddSatisfaction_noParticipation_shouldThrowException() {

        when(participationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> participationService.addSatisfaction(satisfactionForm));
    }

    @Test
    void testAddSatisfaction() {

        when(participationRepository.findById(anyLong())).thenReturn(Optional.of(participation));

        participationService.addSatisfaction(satisfactionForm);

        verify(participationRepository, times(1)).save(participation);
        assertEquals(satisfactionForm.satisfactionComment(), participation.getSatisfactionComment());
        assertEquals(satisfactionForm.satisfaction(), participation.getSatisfaction());
    }

// Tests pour la méthode validate

    @Test
    void testValidate_noParticipation_shouldThrowException() {
        when(participationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> participationService.validate(participation.getId()));
    }

    @Test
    void testValidate() {
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));

        participationService.validate(participation.getId());

        verify(participationRepository, times(1)).save(participation);
        assertEquals(Status.VALIDATED, participation.getStatus());
    }

    // pour deny
    @Test
    void testDeny_noParticipation_shouldThrowException() {
        when(participationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> participationService.deny(participation.getId()));
    }

    @Test
    void testDeny() {
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));

        participationService.deny(participation.getId());

        verify(participationRepository, times(1)).save(participation);
        assertEquals(Status.DENIED, participation.getStatus());
    }

    // pour ship
    @Test
    void testShip_noParticipation_shouldThrowException() {
        when(participationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> participationService.ship(participation.getId()));
    }

    @Test
    void testShip() {
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));

        participationService.ship(participation.getId());

        verify(participationRepository, times(1)).save(participation);
        assertEquals(Status.SHIPPED, participation.getStatus());
    }

    @Test
    void testCountParticipation_nonExistingId_shouldReturnZero() {
        when(participationRepository.countAllByIdIsNotNull()).thenReturn(0L);
        long count = participationService.countParticipation();

        assertEquals(0L, count);
        verify(participationRepository, times(1)).countAllByIdIsNotNull();
    }

    @Test
    void testFindByEmail() {
        when(participationRepository.findByEmail(anyString())).thenReturn(participation);
        Participation foundParticipation = participationService.findByEmail(participation.getEmail());

        assertEquals(participation, foundParticipation);
    }

    @Test
    void testFindByEmail_nullValue() {
        when(participationRepository.findByEmail(null)).thenReturn(null);
        Participation foundParticipation = participationService.findByEmail(null);

        assertNull(foundParticipation);
    }

}