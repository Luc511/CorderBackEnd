package be.technobel.corder.pl.controllers;

import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.dl.models.Participation;
import be.technobel.corder.pl.models.dtos.ParticipationByIdDTO;
import be.technobel.corder.pl.models.dtos.ParticipationDTO;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/participation")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/")
    public ResponseEntity<ParticipationDTO> createParticipation(@RequestBody ParticipationForm participationForm) {
        return ResponseEntity.ok(ParticipationDTO.fromEntity(participationService.create(participationForm)));
    }
    @GetMapping("/")
    public ResponseEntity<List<ParticipationDTO>> getAllParticipations() {
        List<ParticipationDTO> participations = participationService.findAll()
                .stream()
                .map(ParticipationDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(participations);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ParticipationByIdDTO> getParticipationById(@PathVariable Long id) {
        Participation participation = participationService.findById(id);
        return ResponseEntity.ok(ParticipationByIdDTO.fromEntity(participation));
    }
    @PostMapping("/photo/{id}")
    public ResponseEntity<?> addPhoto(@RequestParam("photo") MultipartFile photo, @PathVariable Long id) {
        participationService.addPhoto(photo, id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/photo")
    public ResponseEntity<?> getPhoto(@RequestParam("id") Long id) {
        Participation participation = participationService.findById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(participation.getPictureType()));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(participation.getPictureName()).build());
        return new ResponseEntity<>(participation.getBlob(), headers, HttpStatus.OK);
    }
}
