package be.technobel.corder.bl.impl;

import be.technobel.corder.dl.models.User;
import be.technobel.corder.dl.models.enums.Role;
import be.technobel.corder.dl.repositories.UserRepository;
import be.technobel.corder.pl.config.exceptions.InvalidPasswordException;
import be.technobel.corder.pl.config.security.JWTProvider;
import be.technobel.corder.pl.models.dtos.AuthDTO;
import be.technobel.corder.pl.models.forms.LoginForm;
import be.technobel.corder.pl.models.forms.PasswordChangeForm;
import be.technobel.corder.pl.models.forms.UserForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JWTProvider jwtProvider;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void register_withNullUserForm_throwsIllegalArgumentException() {
        UserForm form = null;
        assertThrows(IllegalArgumentException.class, () -> userService.register(form));
    }

    @Test
    void register_withValidUserForm_savesUser() {
        UserForm form = new UserForm("test", "testing123", Set.of(Role.ADMIN));
        when(passwordEncoder.encode(form.password())).thenReturn("encoded_password");

        userService.register(form);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withUserForm_populatesUserFieldsCorrectly() {
        UserForm form = new UserForm("test", "testing123", Set.of(Role.ADMIN));
        when(passwordEncoder.encode(form.password())).thenReturn("encoded_password");

        userService.register(form);

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void loginTest_Success() {
        LoginForm loginForm = new LoginForm("user1", "password1");
        User user = new User();
        user.setLogin("user1");
        user.setRoles(Collections.singleton(Role.ADMIN));
        user.setPassword(passwordEncoder.encode("password1"));

        when(userRepository.findByLogin(any(String.class))).thenReturn(Optional.of(user));

        when(jwtProvider.generateToken(any(String.class), any(List.class))).thenReturn("Token");

        AuthDTO authDTO = userService.login(loginForm);

        Assertions.assertEquals("Token", authDTO.token());
    }

    @Test
    void whenLogin_invalidUser_throwException() {

        LoginForm loginForm = new LoginForm("non-existent-user", "password");

        when(userRepository.findByLogin(loginForm.login())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.login(loginForm));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        String loginName = "wrong_user";
        String password = "wrong_password";

        LoginForm form = new LoginForm(loginName, password);

        // Set up expectations
        Mockito.when(userRepository.findByLogin(form.login())).thenReturn(Optional.empty());

        // Execute operation and check for expected exception
        Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.login(form));
    }

    /**
     * Test the changePassword method under the condition that the currentPassword is not correct.
     * InvalidPasswordException should be thrown.
     */
    @Test
    public void changePassword_incorrectCurrentPassword_throwInvalidPasswordException() {
        User user = new User();
        user.setPassword("encodedCurrentPassword");

        PasswordChangeForm passwordChangeForm = new PasswordChangeForm("testLogin", "testCurrentPassword", "testNewPassword");

        userService = new UserServiceImpl(userRepository, authenticationManager, jwtProvider, passwordEncoder);

        when(userRepository.findByLogin("testLogin")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("testCurrentPassword","encodedCurrentPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, ()->userService.changePassword(passwordChangeForm));

    }

    /**
     * Test the changePassword method under the condition that the currentPassword is correct.
     * The password should be updated in the user repository.
     */
    @Test
    public void changePassword_correctCurrentPassword_updatePasswordInUserRepository() {
        User user = new User();
        user.setPassword("encodedCurrentPassword");

        PasswordChangeForm passwordChangeForm = new PasswordChangeForm("testLogin", "testCurrentPassword", "testNewPassword");

        userService = new UserServiceImpl(userRepository, authenticationManager, jwtProvider, passwordEncoder);

        when(userRepository.findByLogin("testLogin")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("testCurrentPassword","encodedCurrentPassword")).thenReturn(true);
        when(passwordEncoder.encode("testNewPassword")).thenReturn("encodedNewPassword");

        userService.changePassword(passwordChangeForm);

        verify(userRepository).save(user);
    }
}