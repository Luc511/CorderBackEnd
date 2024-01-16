package be.technobel.corder.pl.controllers;

import be.technobel.corder.bl.UserService;
import be.technobel.corder.pl.models.forms.LoginForm;
import be.technobel.corder.pl.models.forms.PasswordChangeForm;
import be.technobel.corder.pl.models.forms.UserForm;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginForm form){
        return ResponseEntity.ok(userService.login(form));
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserForm form){
        userService.register(form);
        return ResponseEntity.ok("User created");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody @Valid PasswordChangeForm form) {
        userService.changePassword(form);
        return ResponseEntity.ok("Password changed successfully");
    }
}
