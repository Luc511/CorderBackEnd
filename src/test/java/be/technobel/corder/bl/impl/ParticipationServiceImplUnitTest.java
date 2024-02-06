package be.technobel.corder.bl.impl;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ParticipationServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
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
    void testCreateDuplicateParticipantAddressWithDifferentChars() {
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
    void getWeek_withParticipations() {
        LocalDate today = LocalDate.now();
        Long[] expectedCounts = {5L, 4L, 3L, 2L, 1L, 6L, 7L};

        when(participationRepository.countParticipationsByParticipationDate(today)).thenReturn(expectedCounts[0]);
        when(participationRepository.countParticipationsByParticipationDate(today.minusDays(1))).thenReturn(expectedCounts[1]);
        when(participationRepository.countParticipationsByParticipationDate(today.minusDays(2))).thenReturn(expectedCounts[2]);
        when(participationRepository.countParticipationsByParticipationDate(today.minusDays(3))).thenReturn(expectedCounts[3]);
        when(participationRepository.countParticipationsByParticipationDate(today.minusDays(4))).thenReturn(expectedCounts[4]);
        when(participationRepository.countParticipationsByParticipationDate(today.minusDays(5))).thenReturn(expectedCounts[5]);
        when(participationRepository.countParticipationsByParticipationDate(today.minusDays(6))).thenReturn(expectedCounts[6]);

        Long[] weekCounts = participationService.getWeek(today);

        assertArrayEquals(expectedCounts, weekCounts);
        verify(participationRepository, times(7)).countParticipationsByParticipationDate(any());
    }

    @Test
    void getWeekWithDays_withParticipations() {
        LocalDate start = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<String, Long> expectedCounts = Map.of(
                "MONDAY", 5L,
                "TUESDAY", 4L,
                "WEDNESDAY", 2L,
                "THURSDAY", 0L,
                "FRIDAY", 3L,
                "SATURDAY", 6L,
                "SUNDAY", 1L
        );

        for (int i = 0; i <= 6; i++) {
            LocalDate currentDate = start.plusDays(i);
            when(participationRepository.countParticipationsByParticipationDate(currentDate)).thenReturn(expectedCounts.get(currentDate.getDayOfWeek().toString()));
        }

        Map<String, Long> weekCounts = participationService.getWeekWithDays();

        assertEquals(expectedCounts, weekCounts);
        for (int i = 0; i <= 6; i++) {
            LocalDate currentDate = start.plusDays(i);
            verify(participationRepository, times(1)).countParticipationsByParticipationDate(currentDate);
        }
    }

    @Test
    void countParticipation_withParticipations() {
        Long expectedCount = 5L;
        when(participationRepository.countAllByIdIsNotNull()).thenReturn(expectedCount);

        Long actualCount = participationService.countParticipation();

        assertEquals(expectedCount, actualCount);
        verify(participationRepository, times(1)).countAllByIdIsNotNull();
    }

    @Test
    void countParticipation_noParticipations() {
        Long expectedCount = 0L;
        when(participationRepository.countAllByIdIsNotNull()).thenReturn(expectedCount);

        Long actualCount = participationService.countParticipation();

        assertEquals(expectedCount, actualCount);
        verify(participationRepository, times(1)).countAllByIdIsNotNull();
    }

    @Test
    void countParticipationLast5Months_withParticipations() {
        Long[] expectedCounts = {120L, 98L, 76L, 54L, 32L};
        for (int i = 0; i < 5; i++) {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
            when(participationRepository.countAllByParticipationDateBetween(startOfMonth, endOfMonth)).thenReturn(expectedCounts[i]);
        }

        Long[] actualCounts = participationService.countParticipationLast5Months();

        assertArrayEquals(expectedCounts, actualCounts);
        for (int i = 0; i < 5; i++) {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
            verify(participationRepository, times(1)).countAllByParticipationDateBetween(startOfMonth, endOfMonth);
        }
    }

    @Test
    void countParticipationLast5Months_noParticipations() {
        Long[] expectedCounts = {0L, 0L, 0L, 0L, 0L};
        for (int i = 0; i < 5; i++) {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
            when(participationRepository.countAllByParticipationDateBetween(startOfMonth, endOfMonth)).thenReturn(expectedCounts[i]);
        }

        Long[] actualCounts = participationService.countParticipationLast5Months();

        assertArrayEquals(expectedCounts, actualCounts);
        for (int i = 0; i < 5; i++) {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
            verify(participationRepository, times(1)).countAllByParticipationDateBetween(startOfMonth, endOfMonth);
        }
    }

    @Test
    void countByProvince_withParticipations() {
        Map<String, Long> expectedCounts = Map.of(
                "Brabant Wallon", 50L,
                "Liège", 45L,
                "Namur", 30L,
                "Hainaut", 80L,
                "Luxembourg", 20L
        );

        when(participationRepository.countParticipationByAddress_PostCodeBetween(1300, 1499)).thenReturn(expectedCounts.get("Brabant Wallon"));
        when(participationRepository.countParticipationByAddress_PostCodeBetween(4000, 4999)).thenReturn(expectedCounts.get("Liège"));
        when(participationRepository.countParticipationByAddress_PostCodeBetween(5000, 5680)).thenReturn(expectedCounts.get("Namur"));
        when(participationRepository.countParticipationByAddress_PostCodeBetween(6000, 6599)).thenReturn(35L);
        when(participationRepository.countParticipationByAddress_PostCodeBetween(7000, 7999)).thenReturn(45L);
        when(participationRepository.countParticipationByAddress_PostCodeBetween(6600, 6999)).thenReturn(expectedCounts.get("Luxembourg"));

        Map<String, Long> actualCounts = participationService.countByProvince();

        assertEquals(expectedCounts, actualCounts);
        verify(participationRepository, times(1)).countParticipationByAddress_PostCodeBetween(1300, 1499);
        verify(participationRepository, times(1)).countParticipationByAddress_PostCodeBetween(4000, 4999);
        verify(participationRepository, times(1)).countParticipationByAddress_PostCodeBetween(5000, 5680);
        verify(participationRepository, times(1)).countParticipationByAddress_PostCodeBetween(6000, 6599);
        verify(participationRepository, times(1)).countParticipationByAddress_PostCodeBetween(7000, 7999);
        verify(participationRepository, times(1)).countParticipationByAddress_PostCodeBetween(6600, 6999);
    }

    @Test
    void countByProductType_withParticipations() {
        Map<String, Long> expectedCounts = Map.of(
                "insecticide", 50L,
                "herbicide", 45L,
                "fongicide", 30L,
                "autre", 25L
        );

        when(participationRepository.countParticipationByProductType("Insecticide")).thenReturn(expectedCounts.get("insecticide"));
        when(participationRepository.countParticipationByProductType("Herbicide")).thenReturn(expectedCounts.get("herbicide"));
        when(participationRepository.countParticipationByProductType("Fongicide")).thenReturn(expectedCounts.get("fongicide"));
        List<Participation> otherParticipations = new ArrayList<>();
        for (int i = 0; i < expectedCounts.get("autre"); i++) {
            otherParticipations.add(new Participation());
        }
        when(participationRepository.findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"))).thenReturn(otherParticipations);

        Map<String, Long> actualCounts = participationService.countByProductType();

        assertEquals(expectedCounts, actualCounts);
        verify(participationRepository, times(1)).countParticipationByProductType("Insecticide");
        verify(participationRepository, times(1)).countParticipationByProductType("Herbicide");
        verify(participationRepository, times(1)).countParticipationByProductType("Fongicide");
        verify(participationRepository, times(1)).findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"));
    }

    @Test
    void otherProductType_withVariousProducts() {
        List<String> expectedProductTypes = List.of("OtherProduct1", "OtherProduct2");
        List<Participation> otherParticipations = new ArrayList<>();
        for (String productType : expectedProductTypes) {
            Participation p = new Participation();
            p.setProductType(productType);
            otherParticipations.add(p);
        }
        when(participationRepository.findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"))).thenReturn(otherParticipations);

        List<String> actualProductTypes = participationService.otherProductType();

        assertEquals(expectedProductTypes, actualProductTypes);
        verify(participationRepository, times(1)).findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"));
    }


    @Test
    void otherProductType_withNoOtherProducts() {
        List<Participation> otherParticipations = new ArrayList<>();
        when(participationRepository.findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"))).thenReturn(otherParticipations);

        List<String> actualProductTypes = participationService.otherProductType();

        assertTrue(actualProductTypes.isEmpty());
        verify(participationRepository, times(1)).findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"));
    }

    @Test
    void countNotes_withParticipations() {
        Long[] expectedCounts = {10L, 20L, 30L};
        for (int i = 0; i < 3; i++) {
            when(participationRepository.countParticipationBySatisfaction(i + 1)).thenReturn(expectedCounts[i]);
        }

        Long[] actualCounts = participationService.countNotes();

        assertArrayEquals(expectedCounts, actualCounts);
        for (int i = 0; i < 3; i++) {
            verify(participationRepository, times(1)).countParticipationBySatisfaction(i + 1);
        }
    }

    @Test
    void countNotes_noParticipations() {
        Long[] expectedCounts = {0L, 0L, 0L};
        for (int i = 0; i < 3; i++) {
            when(participationRepository.countParticipationBySatisfaction(i + 1)).thenReturn(expectedCounts[i]);
        }

        Long[] actualCounts = participationService.countNotes();

        assertArrayEquals(expectedCounts, actualCounts);
        for (int i = 0; i < 3; i++) {
            verify(participationRepository, times(1)).countParticipationBySatisfaction(i + 1);
        }
    }

    @Test
    void countSatisfactionComments_withComments() {
        Map<String, Long> expectedCounts = Map.of(
                "C'était trop long", 1L,
                "C'était trop court", 2L,
                "L'appareil ne fonctionnait pas", 3L,
                "Informations pas claires", 4L
        );
        for (Map.Entry<String, Long> entry : expectedCounts.entrySet()) {
            when(participationRepository.countParticipationBySatisfactionCommentIgnoreCase(entry.getKey())).thenReturn(entry.getValue());
        }

        Map<String, Long> actualCounts = participationService.countSatisfactionComments();

        assertEquals(expectedCounts, actualCounts);
        for (String key : expectedCounts.keySet()) {
            verify(participationRepository, times(1)).countParticipationBySatisfactionCommentIgnoreCase(key);
        }
    }

    @Test
    void countSatisfactionComments_noComments() {
        Map<String, Long> expectedCounts = Map.of(
                "C'était trop long", 0L,
                "C'était trop court", 0L,
                "L'appareil ne fonctionnait pas", 0L,
                "Informations pas claires", 0L
        );
        for (Map.Entry<String, Long> entry : expectedCounts.entrySet()) {
            when(participationRepository.countParticipationBySatisfactionCommentIgnoreCase(entry.getKey())).thenReturn(entry.getValue());
        }

        Map<String, Long> actualCounts = participationService.countSatisfactionComments();

        assertEquals(expectedCounts, actualCounts);
        for (String key : expectedCounts.keySet()) {
            verify(participationRepository, times(1)).countParticipationBySatisfactionCommentIgnoreCase(key);
        }
    }

    @Test
    void allOtherSatisfactionComments_withVariousComments() {
        List<String> expectedComments = List.of("Autre commentaire 1", "Autre commentaire 2");
        List<Participation> otherParticipations = new ArrayList<>();
        for (String comment : expectedComments) {
            Participation p = new Participation();
            p.setSatisfactionComment(comment);
            otherParticipations.add(p);
        }
        when(participationRepository.findAllBySatisfactionCommentNotIn(List.of("C'était trop long", "C'était trop court", "L'appareil ne fonctionnait pas", "Informations pas claires"))).thenReturn(otherParticipations);

        List<String> actualComments = participationService.allOtherSatisfactionComments();

        assertEquals(expectedComments, actualComments);
        verify(participationRepository, times(1)).findAllBySatisfactionCommentNotIn(List.of("C'était trop long", "C'était trop court", "L'appareil ne fonctionnait pas", "Informations pas claires"));
    }

    @Test
    void allOtherSatisfactionComments_withNoOtherComments() {
        List<Participation> otherParticipations = new ArrayList<>();
        when(participationRepository.findAllBySatisfactionCommentNotIn(List.of("C'était trop long", "C'était trop court", "L'appareil ne fonctionnait pas", "Informations pas claires"))).thenReturn(otherParticipations);

        List<String> actualComments = participationService.allOtherSatisfactionComments();

        assertTrue(actualComments.isEmpty());
        verify(participationRepository, times(1)).findAllBySatisfactionCommentNotIn(List.of("C'était trop long", "C'était trop court", "L'appareil ne fonctionnait pas", "Informations pas claires"));
    }

    @Test
    void last3Pending_withPendingParticipations() {
        Long[] expectedIds = {123L, 456L, 789L};
        List<Participation> top3Participations = new ArrayList<>();
        for (Long id : expectedIds) {
            Participation p = new Participation();
            p.setId(id);
            p.setStatus(Status.PENDING);
            top3Participations.add(p);
        }
        when(participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.PENDING)).thenReturn(top3Participations);

        Long[] actualIds = participationService.last3Pending();

        assertArrayEquals(expectedIds, actualIds);
        verify(participationRepository, times(1)).findTop3ByStatusOrderByStatusUpdateDateDesc(Status.PENDING);
    }

    @Test
    void last3Pending_noPendingParticipations() {
        when(participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.PENDING)).thenReturn(new ArrayList<>());

        Long[] actualIds = participationService.last3Pending();

        assertArrayEquals(new Long[0], actualIds);
        verify(participationRepository, times(1)).findTop3ByStatusOrderByStatusUpdateDateDesc(Status.PENDING);
    }

    @Test
    void last3Validated_withValidatedParticipations() {
        Long[] expectedIds = {123L, 456L, 789L};
        List<Participation> top3Participations = new ArrayList<>();
        for (Long id : expectedIds) {
            Participation p = new Participation();
            p.setId(id);
            p.setStatus(Status.VALIDATED);
            top3Participations.add(p);
        }
        when(participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.VALIDATED)).thenReturn(top3Participations);

        Long[] actualIds = participationService.last3Validated();

        assertArrayEquals(expectedIds, actualIds);
        verify(participationRepository, times(1)).findTop3ByStatusOrderByStatusUpdateDateDesc(Status.VALIDATED);
    }

    @Test
    void last3Validated_noValidatedParticipations() {
        when(participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.VALIDATED)).thenReturn(new ArrayList<>());

        Long[] actualIds = participationService.last3Validated();

        assertArrayEquals(new Long[0], actualIds);
        verify(participationRepository, times(1)).findTop3ByStatusOrderByStatusUpdateDateDesc(Status.VALIDATED);
    }

    @Test
    void findByEmail_existingEmail() {
        String testEmail = "test@example.com";
        Participation expectedParticipation = new Participation();
        expectedParticipation.setEmail(testEmail);
        when(participationRepository.findByEmail(testEmail)).thenReturn(expectedParticipation);

        Participation actualParticipation = participationService.findByEmail(testEmail);

        assertEquals(expectedParticipation, actualParticipation);
        verify(participationRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    void findByEmail_nonExistingEmail() {
        String testEmail = "nonExistingTest@example.com";
        when(participationRepository.findByEmail(testEmail)).thenReturn(null);

        Participation actualParticipation = participationService.findByEmail(testEmail);

        assertNull(actualParticipation);
        verify(participationRepository, times(1)).findByEmail(testEmail);
    }

}