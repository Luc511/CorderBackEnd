package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.services.UserService;
import be.technobel.corder.dl.models.User;
import be.technobel.corder.dl.repositories.UserRepository;
import be.technobel.corder.pl.config.exceptions.DuplicateUserException;
import be.technobel.corder.pl.config.exceptions.InvalidPasswordException;
import be.technobel.corder.pl.config.security.JWTProvider;
import be.technobel.corder.pl.models.dtos.AuthDTO;
import be.technobel.corder.pl.models.forms.LoginForm;
import be.technobel.corder.pl.models.forms.PasswordChangeForm;
import be.technobel.corder.pl.models.forms.UserForm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the UserService interface.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, AuthenticationManager authenticationManager, JWTProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a user with the specified user form.
     *
     * @param form the user form containing the user details
     * @throws IllegalArgumentException if the form is null
     * @throws DuplicateUserException if the login specified in the form is already used by another user
     */
    @Override
    public void register(UserForm form) {
        if(form == null)
            throw new IllegalArgumentException("form peut pas être null");
        if (userRepository.existsByLogin(form.login()))
            throw new DuplicateUserException("Login already used");
        User entity = new User();
        entity.setLogin(form.login());
        entity.setPassword(passwordEncoder.encode(form.password()));
        entity.setRoles(form.roles());

        userRepository.save(entity);
    }

    /**
     * Authenticates a user and returns an AuthDTO object containing the generated token, login, and roles.
     *
     * @param form The login form containing the user's login and password.
     * @return An AuthDTO object containing the generated token, login, and roles.
     * @throws UsernameNotFoundException If the username is not found in the UserRepository.
     */
    @Override
    public AuthDTO login(LoginForm form) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(form.login(),form.password()));

        Optional<User> optionalUser = userRepository.findByLogin(form.login());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            String token = jwtProvider.generateToken(user.getUsername(), List.copyOf(user.getRoles()));

            return AuthDTO.builder()
                    .token(token)
                    .login(user.getLogin())
                    .roles(user.getRoles())
                    .build();
        } else {
            throw new UsernameNotFoundException("Username not found");
        }
    }

    /**
     * Changes the password of a user.
     *
     * @param passwordChangeForm the form containing the user's login, current password, and new password
     * @throws InvalidPasswordException if the current password is invalid
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public void changePassword(PasswordChangeForm passwordChangeForm) {
        String login = passwordChangeForm.user();
        String currentPassword = passwordChangeForm.oldPassword();
        String newPassword = passwordChangeForm.newPassword();

        Optional<User> optionalUser = userRepository.findByLogin(passwordChangeForm.user());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if(!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new InvalidPasswordException("Current password invalid");
            }

            user.setPassword(passwordEncoder.encode(newPassword));

            userRepository.save(user);
        } else {
            throw new UsernameNotFoundException("Username not found");
        }
    }
}
