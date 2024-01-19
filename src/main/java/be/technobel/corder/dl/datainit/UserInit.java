package be.technobel.corder.dl.datainit;


import be.technobel.corder.bl.services.ParticipationService;
import be.technobel.corder.bl.services.UserService;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Configuration
@ConditionalOnProperty(name = "api.user-init", havingValue = "true")
public class UserInit {

    @Bean
    CommandLineRunner initUser(
            UserService userService
    ) {
        return args -> {
            HashSet<Role> roles = new HashSet<>();
            roles.add(Role.ADMIN);
            userService.register(
                    new UserForm(
                            "test",
                            "Test1234=",
                            roles
                    )
            );
        };
    }
}