package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.dl.repositories.ParticipationRepository;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import org.springframework.stereotype.Service;

@Service
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;

    public ParticipationServiceImpl(ParticipationRepository participationRepository) {
        this.participationRepository = participationRepository;
    }

    @Override
    public Participation create(ParticipationForm participationForm) {
        return participationRepository.save(participationForm.toEntity());
    }
}
