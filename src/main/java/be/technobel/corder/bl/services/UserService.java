package be.technobel.corder.bl.services;

import be.technobel.corder.pl.models.dtos.AuthDTO;
import be.technobel.corder.pl.models.forms.LoginForm;
import be.technobel.corder.pl.models.forms.PasswordChangeForm;
import be.technobel.corder.pl.models.forms.UserForm;

public interface UserService {
    void register(UserForm form);
    AuthDTO login(LoginForm form);
    void changePassword(PasswordChangeForm passwordChangeForm);
}
