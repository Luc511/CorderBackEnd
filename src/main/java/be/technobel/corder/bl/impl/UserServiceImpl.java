package be.technobel.corder.bl.impl;

import be.technobel.corder.bl.UserService;
import be.technobel.corder.dl.models.User;
import be.technobel.corder.dl.repositories.UserRepository;
import be.technobel.corder.pl.config.security.JWTProvider;
import be.technobel.corder.pl.models.dtos.AuthDTO;
import be.technobel.corder.pl.models.forms.LoginForm;
import be.technobel.corder.pl.models.forms.UserForm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public void register(UserForm form) {
        if(form == null)
            throw new IllegalArgumentException("form peut pas être null");

        User entity = new User();
        entity.setLogin(form.login());
        entity.setPassword(passwordEncoder.encode(form.password()));
        entity.setRoles(form.roles());

        userRepository.save(entity);
    }

    @Override
    public AuthDTO login(LoginForm form) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(form.login(),form.password()));

        //exception gérée dans mon implémentationn de service
        User user = userRepository.findByLogin(form.login()).get();

        String token = jwtProvider.generateToken(user.getUsername(), List.copyOf(user.getRoles()));

        return AuthDTO.builder()
                .token(token)
                .login(user.getLogin())
                .roles(user.getRoles())
                .build();
    }
}
