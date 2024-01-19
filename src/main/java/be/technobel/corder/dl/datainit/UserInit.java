package be.technobel.corder.dl.datainit;


import be.technobel.corder.bl.services.UserService;
import be.technobel.corder.dl.models.enums.Role;
import be.technobel.corder.pl.config.exceptions.DuplicateUserException;
import be.technobel.corder.pl.models.forms.UserForm;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

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
            try {
                userService.register(
                        new UserForm(
                                "test",
                                "Test1234=",
                                roles
                        )
                );
            }catch (DuplicateUserException e) {
                System.out.println("UserInit pas nécessaire");
            }
        };
    }
}