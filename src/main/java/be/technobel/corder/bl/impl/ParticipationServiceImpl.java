package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.MailService;
import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Address;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.dl.repositories.ParticipationRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateParticipationException;
import be.technobel.corder.pl.config.exceptions.PhotoException;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import jakarta.persistence.EntityNotFoundException;
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
        //mailService.sendMail(participationForm.email(), "Merci pour votre participation !", content, true);

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

}
