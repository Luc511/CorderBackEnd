package be.technobel.corder.pl.datainit;


import be.technobel.corder.bl.ParticipationService;
import be.technobel.corder.bl.UserService;
import be.technobel.corder.dl.models.enums.Role;
import be.technobel.corder.dl.models.enums.Status;
import be.technobel.corder.pl.models.forms.ParticipationForm;
import be.technobel.corder.pl.models.forms.SatisfactionForm;
import be.technobel.corder.pl.models.forms.UserForm;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;

@Configuration
@ConditionalOnProperty(name = "api.data-init", havingValue = "true")
public class DataInitialization {

    @Bean
    CommandLineRunner initDatabase(
            ParticipationService participationService,
            UserService userService
    ) {
        return args -> {
            Faker faker = new Faker(Locale.FRENCH);

            //Create User
            userService.register(
                    new UserForm(
                            "test",
                            "Test1234=",
                            Set.of(Role.ADMIN)
                    )
            );

            for (int i = 0; i < 10; i++) {


                String imageUrl = "https://picsum.photos/200/300";

                // Download the image into memory
                MultipartFile multipartFile;
                try (InputStream in = new URL(imageUrl).openStream()) {
                    Path tempFile = Files.createTempFile("", ".tmp");
                    Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    byte[] imageBytes = Files.readAllBytes(tempFile);
                    multipartFile = new ByteArrayMultipartFile(imageBytes);
                    Files.delete(tempFile);
                }



                String email = faker.internet().emailAddress();
                //Create Participation
                participationService.create(
                        new ParticipationForm(
                                faker.name().firstName(),
                                faker.name().lastName(),
                                email,
                                Status.PENDING,
                                faker.lorem().word(),
                                faker.address().streetAddress(),
                                faker.address().city(),
                                faker.number().numberBetween(0, 9999),
                                faker.bool().bool(),
                                faker.bool().bool()
                        )
                );
                String quote = faker.yoda().quote();
                if (quote.length() > 254)
                    quote = quote.substring(0, 254);
                participationService.addSatisfaction(
                        new SatisfactionForm(
                                participationService.findByEmail(email).getId(),
                                faker.number().numberBetween(1,3),
                                quote
                        )
                );
                participationService.addPhoto(
                        multipartFile,
                        participationService.findByEmail(email).getId()
                );
            }
        };
    }

//    @Bean
//    CommandLineRunner initDatabase(OwnerRepository ownerRepository,
//                                   PlaneRepository planeRepository,
//                                   PilotRepository pilotRepository,
//                                   MechanicRepository mechanicRepository,
//                                   ModelRepository modelRepository,
//                                   FlightRepository flightRepository,
//                                   InterventionRepository interventionRepository) {
//        return args -> {
//            Faker faker = new Faker(Locale.FRENCH);
//
//            for (int i = 0; i < 100; i++) { // Adjust the count of entities as per your needs
//                // Creating Owner entity
//                Owner owner = new Owner();
//                owner.setName(faker.name().fullName());
//                owner.setAddress(faker.address().fullAddress());
//                owner.setPhoneNumber(faker.beer().name());
//                ownerRepository.save(owner);  //Saving to DB
//
//                // Creating Plane entity
//                Plane plane = new Plane();
//                plane.setImmatNum(faker.regexify("[A-Z]{1}-[A-Za-z0-9]{4}"));
//                planeRepository.save(plane);
//
//                // Creating Pilot entity
//                Pilot pilot = new Pilot();
//                pilot.setName(faker.name().firstName());
//                pilot.setAddress(faker.address().fullAddress());
//                pilot.setPhoneNumber(faker.phoneNumber().cellPhone());
//                pilot.setNumLicense(faker.code().asin());
//                pilotRepository.save(pilot);
//
//                // Creating Mechanic entity
//                Mechanic mechanic = new Mechanic();
//                mechanic.setName(faker.name().fullName());
//                mechanic.setAddress(faker.address().fullAddress());
//                mechanic.setPhoneNumber(faker.phoneNumber().phoneNumber());
//                mechanicRepository.save(mechanic);
//
//                // Creating Model entity
//                Model model = new Model();
//                model.setName(faker.name().firstName());
//                model.setBrand(faker.company().name());
//                model.setPower(faker.superhero().power());
//                model.setNbPlaces(faker.number().numberBetween(1,100));
//                modelRepository.save(model);
//
//                // Creating Flight entity
//                Flight flight = new Flight();
//                flight.setPlane(plane); //Connecting with already saved Plane entity
//                flightRepository.save(flight);
//
//                // Creating Intervention entity
//                Intervention intervention = new Intervention();
//                intervention.setName(faker.name().title());
//                intervention.setInterventionDate(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//                intervention.setDuration(Duration.ofHours(faker.number().numberBetween(1,5)));
//                intervention.setCheckMechanic(mechanic); //Connecting with already
//                // saved Mechanic entity
//                intervention.setPlane(plane); // Connecting with already saved Plane entity
//                interventionRepository.save(intervention);
//
//                // Create relationships between entities
//                pilot.getFlights().add(flight);
//                flight.getPilots().add(pilot);
//                pilotRepository.save(pilot); // save Pilot entity with flight relationship
//
//                owner.getOwnedPlanes().add(plane);
//                plane.getOwners().add(owner);
//                ownerRepository.save(owner); // save Owner entity with plane relationship
//
//                model.getPlanes().add(plane);
//                model.getMechanics().add(mechanic);
//                plane.setModel(model);
//                mechanic.getAuthorizationModels().add(model);
//                modelRepository.save(model); // save Model entity with plane and mechanic relationship
//                planeRepository.save(plane); // save Plane entity with model relationship
//                mechanicRepository.save(mechanic); // save Mechanic entity with model relationship
//
//                plane.getInterventions().add(intervention);
//                plane.getFlights().add(flight);
//                planeRepository.save(plane); // save Plane entity with flight and intervention relationship
//                flight.setPlane(plane);
//                flightRepository.save(flight); // save Flight entity with plane relationship
//
//                intervention.setRepairMechanic(mechanic);
//                mechanic.getRepairInterventions().add(intervention);
//                interventionRepository.save(intervention); // save Intervention entity with mechanic relationship
//                mechanicRepository.save(mechanic); // save Mechanic entity with intervention relationship
//            }
//        };
//    }
}