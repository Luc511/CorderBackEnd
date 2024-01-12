package be.technobel.corder.bl;

import be.technobel.corder.pl.models.dtos.AuthDTO;
import be.technobel.corder.pl.models.forms.LoginForm;
import be.technobel.corder.pl.models.forms.UserForm;

public interface UserService {
    void register(UserForm form);
    AuthDTO login(LoginForm form);
}
