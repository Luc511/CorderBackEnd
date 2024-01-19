package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.services.MailService;
import be.technobel.corder.bl.services.ParticipationService;
import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.dl.repositories.ParticipationRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateParticipationException;
import be.technobel.corder.pl.config.exceptions.PhotoException;
import be.technobel.corder.pl.models.dtos.DashboardDTO;
import be.technobel.corder.pl.models.dtos.StatsDTO;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the ParticipationService interface and provides the functionality for managing participations.
 */
@Service
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final MailService mailService;

    public ParticipationServiceImpl(ParticipationRepository participationRepository, MailService mailService) {
        this.participationRepository = participationRepository;
        this.mailService = mailService;
    }

    /**
     * Checks if the given participation is unique based on the email and address.
     *
     * @param participation The participation to check for uniqueness.
     * @throws DuplicateParticipationException If the participation is not unique based on the email or address.
     */
    private void isUniqueParticipant(Participation participation) {

        String email = formatEmail(participation);

        List<String> emails = findAll().stream()
                .map(this::formatEmail)
                .toList();

        if (emails.contains(email)) {
            throw new DuplicateParticipationException("Ce participant a déjà joué avec cet email !");
        }

        String address = formatAddress(participation);

        List<String> addresses = findAll().stream()
                .map(this::formatAddress)
                .toList();

        if (addresses.contains(address)) {
            throw new DuplicateParticipationException("Ce foyer a déjà une participation !");
        }
    }

    /**
     * Formats the address of a participation.
     *
     * @param participation The participation for which to format the address.
     * @return The formatted address as a string.
     */
    private String formatAddress(Participation participation) {
        Address Address = participation.getAddress();
        return (Address.getStreet().trim() + Address.getCity().trim() + Address.getPostCode())
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Formats the email of a participation by removing leading and trailing spaces and converting to lowercase.
     *
     * @param participation The participation for which to format the email.
     * @return The formatted email as a string.
     */
    private String formatEmail(Participation participation) {
        return (participation.getEmail()).trim().toLowerCase();
    }

    /**
     * Creates a new participation based on the provided participation form.
     *
     * @param participationForm The participation form used to create the participation.
     * @return The created participation.
     */
    @Override
    public Participation create(ParticipationForm participationForm) {
        Participation participation = participationForm.toEntity();

        isUniqueParticipant(participationForm.toEntity());

        participation.setStatus(Status.PENDING);
        participation.setParticipationDate(LocalDate.now());

        //MAIL
        Map<String, Object> variables = new HashMap<>();
        variables.put("greeting", "Merci " + participationForm.firstName() + " !");
        String content = mailService.buildEmailTemplate("email-validation-template", variables);
        mailService.sendMail(participationForm.email(), "Merci pour votre participation !", content, true);

        return participationRepository.save(participation);
    }

    /**
     * Retrieves all participations.
     *
     * @return List of Participation objects representing all participations.
     */
    @Override
    public List<Participation> findAll() {
        return participationRepository.findAll();
    }

    /**
     * Retrieves a Participation object by its id.
     *
     * @param id The id of the participation.
     * @return The Participation object with the given id.
     * @throws EntityNotFoundException If no participation with the given id is found.
     */
    @Override
    public Participation findById(Long id) {
        return participationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Participation avec l'id: " + id + " introuvable")
        );
    }

    /**
     * Adds a photo to a participation by setting the photo's bytes, original filename, and content type.
     *
     * @param photo The photo to be added.
     * @param id The ID of the participation to which the photo is added.
     * @throws PhotoException If there is an error adding the photo to the participation.
     */
    @Override
    public void addPhoto(MultipartFile photo, Long id) {
        try {
            Participation entity = findById(id);
            entity.setBlob(photo.getBytes());
            entity.setPictureName(photo.getOriginalFilename());
            entity.setPictureType(photo.getContentType());
            participationRepository.save(entity);
        } catch (IOException e) {
            throw new PhotoException("Impossible d'ajouter une photo au participant avec l'id:  " + id);
        }
    }

    /**
     * Updates the satisfaction and satisfaction comment of a participation.
     *
     * @param satisfactionForm The satisfaction form containing the participation ID, satisfaction level, and optional satisfaction comment.
     */
    @Override
    public void addSatisfaction(SatisfactionForm satisfactionForm) {
        Participation participation = findById(satisfactionForm.id());
        participation.setSatisfaction(satisfactionForm.satisfaction());
        if (satisfactionForm.satisfactionComment() != null) {
            participation.setSatisfactionComment(satisfactionForm.satisfactionComment());
        }
        participationRepository.save(participation);
    }

    //TODO: fin des tests

    /**
     * Updates the status and status update date of a participation to VALIDATED.
     *
     * @param id The ID of the participation to validate.
     */
    @Transactional
    @Override
    public void validate(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.VALIDATED);
        participation.setStatusUpdateDate(LocalDateTime.now());
        participationRepository.save(participation);
    }

    /**
     * Denies a participation by setting its status to DENIED and updating the status update date.
     *
     * @param id The ID of the participation to deny.
     */
    @Transactional
    @Override
    public void deny(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.DENIED);
        participation.setStatusUpdateDate(LocalDateTime.now());
        participationRepository.save(participation);
    }

    /**
     * Ships a participation by updating its status to SHIPPED and setting the status update date to the current date and time.
     *
     * @param id The ID of the participation to ship.
     */
    @Transactional
    @Override
    public void ship(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.SHIPPED);
        participation.setStatusUpdateDate(LocalDateTime.now());
        participationRepository.save(participation);
    }

    /**
     * Retrieves the participation counts for each day of a week starting from the given first day.
     *
     * @param firstDay The first day of the week.
     * @return An array of Long values representing the participation count for each day of the week. The first element of the
     * array represents the participation count for the first day, the second element represents the count for the second day,
     * and so on.
     */
    @Override
    public Long[] getWeek(LocalDate firstDay) {
        Long[] week = new Long[7];
        for (int i = 0; i < 7; i++) {
            LocalDate date = firstDay.minusDays(i);
            Long count = participationRepository.countParticipationsByParticipationDate(date);
            week[i] = count;
        }
        return week;
    }

    /**
     * Retrieves the count of participations for each day of the current week.
     *
     * @return A map where the key is the name of the day (e.g. MONDAY) and the value is the count of participations for that day.
     */
    @Override
    public Map<String, Long> getWeekWithDays() {
        LocalDate start = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 0; i <= 6; i++) {
            LocalDate currentDate = start.plusDays(i);
            Long count = participationRepository.countParticipationsByParticipationDate(currentDate);
            map.put(currentDate.getDayOfWeek().toString(), count);
        }
        return map;
    }


    /**
     * Counts the total number of participations.
     *
     * @return The count of participations as a Long.
     */
    @Override
    public Long countParticipation() {
        return participationRepository.countAllByIdIsNotNull();
    }

    /**
     * Counts the number of participations for the last 5 months.
     *
     * @return An array of Long values representing the count of participations for each of the last 5 months.
     */
    @Override
    public Long[] countParticipationLast5Months() {
        Long[] count = new Long[5];
        for (int i = 0; i < count.length; i++) {
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1).minusMonths(i);
            LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
            count[i] = participationRepository.countAllByParticipationDateBetween(startOfMonth, endOfMonth);
        }
        return count;
    }

    /**
     * Counts the number of participations by province.
     *
     * @return A map that contains the province names as keys and the corresponding count of participations as values.
     */
    @Override
    public Map<String, Long> countByProvince() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("Brabant Wallon", participationRepository.countParticipationByAddress_PostCodeBetween(1300, 1499));
        map.put("Liège", participationRepository.countParticipationByAddress_PostCodeBetween(4000, 4999));
        map.put("Namur", participationRepository.countParticipationByAddress_PostCodeBetween(5000, 5680));
        map.put("Hainaut", participationRepository.countParticipationByAddress_PostCodeBetween(6000, 6599) + participationRepository.countParticipationByAddress_PostCodeBetween(7000, 7999));
        map.put("Luxembourg", participationRepository.countParticipationByAddress_PostCodeBetween(6600, 6999));
        return map;
    }

    /**
     * Counts the number of participations grouped by product type.
     *
     * @return A map where the key is the product type and the value is the count of participations for that product type.
     *         The keys in the map are 'insecticide', 'herbicide', 'fongicide', and 'autre'.
     */
    @Override
    public Map<String, Long> countByProductType() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("insecticide", participationRepository.countParticipationByProductType("Insecticide"));
        map.put("herbicide", participationRepository.countParticipationByProductType("Herbicide"));
        map.put("fongicide", participationRepository.countParticipationByProductType("Fongicide"));
        List<Participation> others = participationRepository.findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"));
        map.put("autre", (long) others.size());
        return map;
    }

    /**
     * Retrieves a list of product types that are not "Insecticide", "Herbicide", or "Fongicide".
     *
     * @return A list of product types that are not "Insecticide", "Herbicide", or "Fongicide".
     */
    @Override
    public List<String> otherProductType() {
        List<Participation> others = participationRepository.findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"));
        return others.stream()
                .map(Participation::getProductType)
                .distinct()
                .toList();
    }

    /**
     * This method counts the number of participations for each satisfaction level.
     *
     * @return An array of Long values representing the count of participations for each satisfaction level.
     */
    @Override
    public Long[] countNotes() {
        Long[] count = new Long[3];
        for (int i = 0; i < count.length; i++) {
            count[i] = participationRepository.countParticipationBySatisfaction(i+1);
        }
        return count;
    }

    /**
     * Counts the number of satisfaction comments for each type of comment.
     *
     * @return a {@code Map} where the keys are the satisfaction comment types and the values are the counts of each type of comment.
     */
    @Override
    public Map<String, Long> countSatisfactionComments() {
        Map<String, Long> comments = new LinkedHashMap<>();
        comments.put("C'était trop long", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("C'était trop long"));
        comments.put("C'était trop court", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("C'était trop court"));
        comments.put("L'appareil ne fonctionnait pas", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("L'appareil ne fonctionnait pas"));
        comments.put("Informations pas claires", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("Informations pas claires"));
        return comments;
    }

    /**
     * Retrieves all satisfaction comments except for the predefined ones.
     *
     * @return List of strings representing all other satisfaction comments.
     */
    @Override
    public List<String> allOtherSatisfactionComments() {
        return participationRepository
                .findAllBySatisfactionCommentNotIn(List.of("C'était trop long", "C'était trop court", "L'appareil ne fonctionnait pas", "Informations pas claires"))
                .stream()
                .map(Participation::getSatisfactionComment)
                .toList();
    }

    /**
     * Builds a StatsDTO object with various statistics.
     *
     * @return The constructed StatsDTO object.
     */
    @Override
    public StatsDTO statsDTOBuilder() {
        return StatsDTO.builder()
                .countParticipants(countParticipation())
                .countParticipantsEachLast5Months(countParticipationLast5Months())
                .countByProvince(countByProvince())
                .productsUsed(countByProductType())
                .otherProductsUsed(otherProductType())
                .countNotes(countNotes())
                .countSatisfactionComments(countSatisfactionComments())
                .allOthersSatisfactionComment(allOtherSatisfactionComments())
                .build();
    }

    /**
     * Retrieves the ids of the last 3 pending participations.
     *
     * @return An array of Long containing the ids of the last 3 pending participations.
     */
    @Override
    public Long[] last3Pending() {
        return participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.PENDING)
                .stream()
                .map(Participation::getId)
                .toArray(Long[]::new);
    }

    /**
     * Retrieves the IDs of the last 3 participations that have been validated.
     *
     * @return An array of Long values representing the IDs of the last 3 validated participations.
     */
    @Override
    public Long[] last3Validated() {
        return participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.VALIDATED)
                .stream()
                .map(Participation::getId)
                .toArray(Long[]::new);
    }

    /**
     * Builds a DashboardDTO object containing various statistics and data for a dashboard.
     *
     * @return The DashboardDTO object with the built data.
     */
    @Override
    public DashboardDTO dashboardDTOBuilder() {
        return DashboardDTO.builder()
                .countParticipants(countParticipation())
                .days(getWeekWithDays())
                .lastThreePending(last3Pending())
                .lastThreeValidated(last3Validated())
                .build();
    }

    /**
     * Finds a participation by email.
     *
     * @param email The email address to search for.
     * @return The participation object corresponding to the given email, or null if not found.
     */
    @Override
    public Participation findByEmail(String email) {
        return participationRepository.findByEmail(email);
    }


}
