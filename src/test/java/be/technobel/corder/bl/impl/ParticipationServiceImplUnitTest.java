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
import com.speedment.jpastreamer.application.JPAStreamer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ParticipationServiceImpl's 'create' method.
 */
@SpringBootTest
public class ParticipationServiceImplUnitTest {

    ParticipationRepository participationRepository;
    ParticipationServiceImpl participationService;
    MailServiceImpl mailService;
    JPAStreamer jpaStreamer;

    @BeforeEach
    public void setUp() {
        participationRepository = mock(ParticipationRepository.class);
        mailService = mock(MailServiceImpl.class);
        participationService = new ParticipationServiceImpl(participationRepository, mailService, jpaStreamer);
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

    @Test
    public void findById_existingId_shouldReturnParticipation() {
        // Given
        Long id = 1L;
        Participation expectedParticipation = new Participation();
        expectedParticipation.setId(id);
        when(participationRepository.findById(id)).thenReturn(Optional.of(expectedParticipation));

        // When
        Participation actualParticipation = participationService.findById(id);

        // Then
        assertTrue(expectedParticipation == actualParticipation);
    }

    @Test
    void findById_nonExistingId_shouldThrowException() {
        // Given
        Long id = 2L;
        when(participationRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> participationService.findById(id));
    }

    @Test
    public void addPhotoTest() throws IOException {
        Participation participation = new Participation();
        MultipartFile file = Mockito.mock(MultipartFile.class);
        byte[] data = "example".getBytes();
        when(file.getBytes()).thenReturn(data);
        when(participationRepository.findById(anyLong())).thenReturn(Optional.of(participation));
        when(participationRepository.save(participation)).thenReturn(participation);

        Participation updatedParticipation = participationService.addPhoto(file, 1L);

        assertEquals(data, updatedParticipation.getBlob(), "Blob must have the same data");
        assertEquals(file.getOriginalFilename(), updatedParticipation.getPictureName(), "PictureName must be same");
        assertEquals(file.getContentType(), updatedParticipation.getPictureType(), "PictureType must be same");
    }

    @Test
    public void addPhotoTest_EntityNotFound() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(participationRepository.findById(anyLong())).thenThrow(new EntityNotFoundException("Participation avec l'id: 1 introuvable"));

        try {
            participationService.addPhoto(file, 1L);
        } catch (EntityNotFoundException e) {
            assertEquals("Participation avec l'id: 1 introuvable", e.getMessage());
        }
    }

    @Test
    public void addPhotoTest_PhotoException() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException());
        Participation participation = new Participation();
        when(participationRepository.findById(anyLong())).thenReturn(Optional.of(participation));

        try {
            participationService.addPhoto(file, 1L);
        } catch (RuntimeException e) {
            assertEquals("Impossible d'ajouter une photo au participant avec l'id:  1", e.getMessage());
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

}