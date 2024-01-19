package be.technobel.corder.pl.controllers;

import be.technobel.corder.bl.services.ParticipationService;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.pl.models.dtos.*;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * The ParticipationController class handles the endpoints related to participations.
 */
@RestController
@RequestMapping("/participation")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/")
    public ResponseEntity<ParticipationDTO> createParticipation(@Valid @RequestBody ParticipationForm participationForm) {
        return ResponseEntity.ok(ParticipationDTO.fromEntity(participationService.create(participationForm)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<ParticipationDTO>> getAllParticipations() {
        List<ParticipationDTO> participations = participationService.findAll()
                .stream()
                .map(ParticipationDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(participations);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('LOGISTIC')")
    @GetMapping("/{id}")
    public ResponseEntity<ParticipationByIdDTO> getParticipationById(@PathVariable Long id) {
        Participation participation = participationService.findById(id);
        return ResponseEntity.ok(ParticipationByIdDTO.fromEntity(participation));
    }

    @PostMapping("/photo/{id}")
    public void addPhoto(@RequestBody MultipartFile file, @PathVariable Long id) {
        participationService.addPhoto(file, id);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('LOGISTIC')")
    @GetMapping("/photo")
    public ResponseEntity<?> getPhoto(@RequestParam("id") Long id) {
        Participation participation = participationService.findById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(participation.getPictureType()));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(participation.getPictureName()).build());
        return new ResponseEntity<>(participation.getBlob(), headers, HttpStatus.OK);
    }

    @PostMapping("/rating")
    public void addRating(@Valid @RequestBody SatisfactionForm satisfactionForm) {
        participationService.addSatisfaction(satisfactionForm);
    }

    //TODO: ces 3 méthodes Patch ne fonctionnent pas sur le front sauf si je retire le PreAuthorize (sur Swagger si)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/validate")
    public void validate(@RequestParam Long id) {
        participationService.validate(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/deny")
    public void deny(@RequestParam Long id) {
        participationService.deny(id);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('LOGISTIC')")
    @PatchMapping("/ship")
    public void ship(@RequestParam Long id) {
        participationService.ship(id);
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('LOGISTIC')")
    @GetMapping("/getWeek")
    public ResponseEntity<WeekDTO> getWeek(@RequestParam LocalDate firstDay) {
        return ResponseEntity.ok(WeekDTO.builder().days(participationService.getWeek(firstDay)).build());
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('LOGISTIC')")
    @GetMapping("/stats")
    public ResponseEntity<StatsDTO> getAllStats() {
        return ResponseEntity.ok(participationService.statsDTOBuilder());
    }

    @PreAuthorize("hasRole('ADMIN') || hasRole('LOGISTIC')")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(participationService.dashboardDTOBuilder());
    }
}
