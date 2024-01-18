package be.technobel.corder.dl.datainit;


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
}