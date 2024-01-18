package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.MailService;
import be.technobel.corder.bl.ParticipationService;
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

@Service
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final MailService mailService;

    public ParticipationServiceImpl(ParticipationRepository participationRepository, MailService mailService) {
        this.participationRepository = participationRepository;
        this.mailService = mailService;
    }

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

    private String formatAddress(Participation participation) {
        Address Address = participation.getAddress();
        return (Address.getStreet().trim() + Address.getCity().trim() + Address.getPostCode())
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "");
    }

    private String formatEmail(Participation participation) {
        return (participation.getEmail()).trim().toLowerCase();
    }

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

    @Override
    public List<Participation> findAll() {
        return participationRepository.findAll();
    }

    @Override
    public Participation findById(Long id) {
        return participationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Participation avec l'id: " + id + " introuvable")
        );
    }

    @Override
    public Participation addPhoto(MultipartFile photo, Long id) {
        try {
            Participation entity = findById(id);
            entity.setBlob(photo.getBytes());
            entity.setPictureName(photo.getOriginalFilename());
            entity.setPictureType(photo.getContentType());
            return participationRepository.save(entity);
        } catch (IOException e) {
            throw new PhotoException("Impossible d'ajouter une photo au participant avec l'id:  " + id);
        }
    }

    @Override
    public Participation addSatisfaction(SatisfactionForm satisfactionForm) {
        Participation participation = findById(satisfactionForm.id());
        participation.setSatisfaction(satisfactionForm.satisfaction());
        if (satisfactionForm.satisfactionComment() != null) {
            participation.setSatisfactionComment(satisfactionForm.satisfactionComment());
        }
        return participationRepository.save(participation);
    }

    //TODO: fin des tests

    @Transactional
    @Override
    public void validate(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.VALIDATED);
        participation.setStatusUpdateDate(LocalDateTime.now());
        participationRepository.save(participation);
    }

    @Transactional
    @Override
    public void deny(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.DENIED);
        participation.setStatusUpdateDate(LocalDateTime.now());
        participationRepository.save(participation);
    }

    @Transactional
    @Override
    public void ship(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.SHIPPED);
        participation.setStatusUpdateDate(LocalDateTime.now());
        participationRepository.save(participation);
    }

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


    @Override
    public Long countParticipation() {
        return participationRepository.countAllByIdIsNotNull();
    }

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

    @Override
    public List<String> otherProductType() {
        List<Participation> others = participationRepository.findAllByProductTypeNotIn(List.of("Insecticide", "Herbicide", "Fongicide"));
        return others.stream()
                .map(Participation::getProductType)
                .distinct()
                .toList();
    }

    @Override
    public Long[] countNotes() {
        Long[] count = new Long[3];
        for (int i = 0; i < count.length; i++) {
            count[i] = participationRepository.countParticipationBySatisfaction(i+1);
        }
        return count;
    }

    @Override
    public Map<String, Long> countSatisfactionComments() {
        Map<String, Long> comments = new LinkedHashMap<>();
        comments.put("C'était trop long", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("C'était trop long"));
        comments.put("C'était trop court", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("C'était trop court"));
        comments.put("L'appareil ne fonctionnait pas", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("L'appareil ne fonctionnait pas"));
        comments.put("Informations pas claires", participationRepository.countParticipationBySatisfactionCommentIgnoreCase("Informations pas claires"));
        return comments;
    }

    @Override
    public List<String> allOtherSatisfactionComments() {
        return participationRepository
                .findAllBySatisfactionCommentNotIn(List.of("C'était trop long", "C'était trop court", "L'appareil ne fonctionnait pas", "Informations pas claires"))
                .stream()
                .map(Participation::getSatisfactionComment)
                .toList();
    }

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

    @Override
    public Long[] last3Pending() {
        return participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.PENDING)
                .stream()
                .map(Participation::getId)
                .toArray(Long[]::new);
    }

    @Override
    public Long[] last3Validated() {
        return participationRepository.findTop3ByStatusOrderByStatusUpdateDateDesc(Status.VALIDATED)
                .stream()
                .map(Participation::getId)
                .toArray(Long[]::new);
    }

    @Override
    public DashboardDTO dashboardDTOBuilder() {
        return DashboardDTO.builder()
                .countParticipants(countParticipation())
                .days(getWeekWithDays())
                .lastThreePending(last3Pending())
                .lastThreeValidated(last3Validated())
                .build();
    }

    @Override
    public Participation findByEmail(String email) {
        return participationRepository.findByEmail(email);
    }
}
