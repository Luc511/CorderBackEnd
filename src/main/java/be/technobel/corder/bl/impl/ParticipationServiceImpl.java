package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.MailService;
import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.dl.repositories.ParticipationRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateParticipationException;
import be.technobel.corder.pl.config.exceptions.PhotoException;
import be.technobel.corder.pl.models.dtos.StatsDTO;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import com.speedment.jpastreamer.application.JPAStreamer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final MailService mailService;

    private final JPAStreamer jpaStreamer;

    @Autowired
    public ParticipationServiceImpl(ParticipationRepository participationRepository, MailService mailService, JPAStreamer jpaStreamer) {
        this.participationRepository = participationRepository;
        this.mailService = mailService;
        this.jpaStreamer = jpaStreamer;
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
        return (Address.getStreet() + Address.getCity() + Address.getPostCode()).trim().toLowerCase();
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
        participationRepository.save(participation);
    }

    @Transactional
    @Override
    public void deny(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.DENIED);
        participationRepository.save(participation);
    }

    @Transactional
    @Override
    public void ship(Long id) {
        Participation participation = findById(id);
        participation.setStatus(Status.SHIPPED);
        participationRepository.save(participation);
    }

    @Override
    public Long[] getWeek(LocalDate firstDay) {
        Long[] week = new Long[7];
        for (int i = 0; i < 7; i++) {
            LocalDate date = firstDay.plusDays(i);
            Long count = participationRepository.countParticipationsByParticipationDate(date);
            week[i] = count;
        }
        return week;
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
        Map<String, Long> map = new HashMap<>();
        map.put("Brabant Wallon", participationRepository.countParticipationByAddress_PostCodeBetween(1300, 1499));
        map.put("Liège", participationRepository.countParticipationByAddress_PostCodeBetween(4000, 4999));
        map.put("Namur", participationRepository.countParticipationByAddress_PostCodeBetween(5000, 5680));
        map.put("Hainaut", participationRepository.countParticipationByAddress_PostCodeBetween(6000, 6599) + participationRepository.countParticipationByAddress_PostCodeBetween(7000, 7999));
        map.put("Luxembourg", participationRepository.countParticipationByAddress_PostCodeBetween(6600, 6999));
        return map;
    }

    @Override
    public Map<String, Long> countByProductType() {
        Map<String, Long> map = new HashMap<>();
        map.put("insecticide", participationRepository.countParticipationByProductType("insecticide"));
        map.put("herbicide", participationRepository.countParticipationByProductType("herbicide"));
        map.put("fongicide", participationRepository.countParticipationByProductType("fongicide"));
        List<Participation> others = participationRepository.findAllByProductTypeNotIn(List.of("insecticide", "herbicide", "fongicide"));
        map.put("autre", (long) others.size());
        return map;
    }

    @Override
    public List<String> otherProductType() {
        List<Participation> others = participationRepository.findAllByProductTypeNotIn(List.of("insecticide", "herbicide", "fongicide"));
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
        Map<String, Long> comments = new HashMap<>();
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
                .totalParticipation(countParticipation())
                .totalParticipationLast5Months(countParticipationLast5Months())
                .totalByProvince(countByProvince())
                .totalByProductType(countByProductType())
                .otherProductNames(otherProductType())
                .notes(countNotes())
                .totalSatisfactionComment(countSatisfactionComments())
                .otherSatisfactionComments(allOtherSatisfactionComments())
                .build();
    }
}
