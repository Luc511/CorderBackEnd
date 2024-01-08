package be.technobel.corder.pl.controllers;

import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/participation")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/")
    public ResponseEntity<Participation> createParticipation(@RequestBody ParticipationForm participationForm) {
        return ResponseEntity.ok(participationService.create(participationForm));
    }
}
