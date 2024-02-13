package be.technobel.corder.dl.datainit;


import be.technobel.corder.bl.services.UserService;
import be.technobel.corder.dl.models.enums.Role;
import be.technobel.corder.pl.config.exceptions.DuplicateUserException;
import be.technobel.corder.pl.models.forms.UserForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

@Configuration
@ConditionalOnProperty(name = "api.user-init", havingValue = "true")
public class UserInit {
    @Value("${corder.username}")
    private String corderUsername;
    @Value("${corder.password}")
    private String corderPassword;
    @Value("${cet.username}")
    private String cetUsername;
    @Value("${cet.password}")
    private String cetPassword;

    @Bean
    CommandLineRunner initUser(
            UserService userService
    ) {
        return args -> {
            HashSet<Role> roleCorder = new HashSet<>();
            roleCorder.add(Role.ADMIN);
            HashSet<Role> roleCeT = new HashSet<>();
            roleCeT.add(Role.LOGISTIC);
            try {
                userService.register(
                        new UserForm(
                                corderUsername,
                                corderPassword,
                                roleCorder
                        )
                );
            }catch (DuplicateUserException e) {
                System.out.println("Corder UserInit pas nécessaire");
            }
            try {
                userService.register(
                        new UserForm(
                                cetUsername,
                                cetPassword,
                                roleCeT
                        )
                );
            }catch (DuplicateUserException e) {
                System.out.println("Cycle en Terre UserInit pas nécessaire");
            }
        };
    }
}