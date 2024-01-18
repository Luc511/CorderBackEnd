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
    public void addPhotoTest() throws IOException {
        byte[] mockBytes = {1, 0, 1};
        MultipartFile photo = new MockMultipartFile("photo", "mock.png", MediaType.IMAGE_PNG_VALUE, mockBytes);

        Participation updatedParticipation = participation;
        updatedParticipation.setBlob(mockBytes);

        when(participationRepository.findById(anyLong())).thenReturn(java.util.Optional.of(participation));
        when(participationRepository.save(any(Participation.class))).thenReturn(updatedParticipation);

        // When
        Participation returnedParticipation = participationService.addPhoto(photo, participation.getId());

        // Then
        verify(participationRepository, times(1)).findById(participation.getId());
        verify(participationRepository, times(1)).save(participation);

        assertNotNull(returnedParticipation.getBlob());
        assertArrayEquals(mockBytes, returnedParticipation.getBlob());
    }

    @Test
    public void addPhotoTest_EntityNotFound() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(participationRepository.findById(anyLong())).thenReturn(Optional.of(participation));

        try {
            participationService.addPhoto(file, participation.getId());
        } catch (EntityNotFoundException e) {
            assertEquals("Participation avec l'id: 1 introuvable", e.getMessage());
        }
    }

    @Test
    public void shouldAddSatisfactionScore() {
        //Given
        SatisfactionForm mockForm = new SatisfactionForm(1L, 3, "test");
        Participation mockParticipation = new Participation();

        when(participationRepository.findById(any(Long.class))).thenReturn(Optional.of(mockParticipation));
        when(participationRepository.save(any(Participation.class))).thenReturn(mockParticipation);

        //When
        Participation result = participationService.addSatisfaction(mockForm);

        //Then
        assertEquals(result.getSatisfaction(), mockForm.satisfaction());
        verify(participationRepository, times(1)).save(any(Participation.class));
    }

    @Test
    public void testValidateWhenIdExists(){
        // Given
        var expectedParticipation = new Participation();
        expectedParticipation.setId(15L);
        expectedParticipation.setStatus(Status.PENDING);
        Mockito.when(participationRepository.findById(15L)).thenReturn(java.util.Optional.of(expectedParticipation));

        // When
        participationService.validate(15L);

        // Then
        verify(participationRepository).findById(15L);
        assertThat(expectedParticipation.getStatus()).isEqualTo(Status.VALIDATED);
        assertThat(expectedParticipation.getStatusUpdateDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void testValidateWhenIdDoesNotExists(){
        // Given
        when(participationRepository.findById(999L)).thenThrow(new EntityNotFoundException("Participation with id: 999 does not exist"));

        // Then
        try {
            // When
            participationService.validate(999L);
        } catch (EntityNotFoundException exception) {
            // Assert
            assertThat(exception).hasMessage("Participation with id: 999 does not exist");
        }
    }

    @Test
    public void testDenyWhenIdExists(){
        // Given
        var expectedParticipation = new Participation();
        expectedParticipation.setId(15L);
        expectedParticipation.setStatus(Status.PENDING);
        Mockito.when(participationRepository.findById(15L)).thenReturn(java.util.Optional.of(expectedParticipation));

        // When
        participationService.deny(15L);

        // Then
        verify(participationRepository).findById(15L);
        assertThat(expectedParticipation.getStatus()).isEqualTo(Status.DENIED);
        assertThat(expectedParticipation.getStatusUpdateDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void testDenyWhenIdDoesNotExists(){
        // Given
        when(participationRepository.findById(999L)).thenThrow(new EntityNotFoundException("Participation with id: 999 does not exist"));

        // Then
        try {
            // When
            participationService.deny(999L);
        } catch (EntityNotFoundException exception) {
            // Assert
            assertThat(exception).hasMessage("Participation with id: 999 does not exist");
        }
    }



    /**
     * Test for case when every day in week has a constant number of participants
     */
    @Test
    void testGetWeekConstantParticipants() {
        ParticipationServiceImpl participationService = new ParticipationServiceImpl(participationRepository, null);
        long constantParticipants = 5L;
        Long[] expectedWeek = new Long[]{constantParticipants, constantParticipants, constantParticipants, constantParticipants, constantParticipants, constantParticipants, constantParticipants};
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 4))).thenReturn(constantParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 3))).thenReturn(constantParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 2))).thenReturn(constantParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 1))).thenReturn(constantParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.MARCH, 31))).thenReturn(constantParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.MARCH, 30))).thenReturn(constantParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.MARCH, 29))).thenReturn(constantParticipants);

        Long[] resultWeek = participationService.getWeek(LocalDate.of(2023, Month.APRIL, 4));

        assertArrayEquals(resultWeek, expectedWeek);
    }

    /**
     * Test for case when every day in week doesn't have any participants
     */
    @Test
    void testGetWeekWithNoParticipants() {
        ParticipationServiceImpl participationService = new ParticipationServiceImpl(participationRepository, null);
        long zeroParticipants = 0L;
        Long[] expectedWeek = new Long[]{zeroParticipants, zeroParticipants, zeroParticipants, zeroParticipants, zeroParticipants, zeroParticipants, zeroParticipants};
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 4))).thenReturn(zeroParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 3))).thenReturn(zeroParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 2))).thenReturn(zeroParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.APRIL, 1))).thenReturn(zeroParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.MARCH, 31))).thenReturn(zeroParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.MARCH, 30))).thenReturn(zeroParticipants);
        when(participationRepository.countParticipationsByParticipationDate(LocalDate.of(2023, Month.MARCH, 29))).thenReturn(zeroParticipants);

        Long[] resultWeek = participationService.getWeek(LocalDate.of(2023, Month.APRIL, 4));

        assertArrayEquals(resultWeek, expectedWeek);
    }

}