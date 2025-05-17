package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;

@Named()
@SessionScoped
@Getter
public class UserController {
    private User user = new User();
    private boolean showHelperText = false;

    @PostConstruct
    public void init() {
        showHelperText = false;
    }

    public String irSaludo() {
        if (user.getName() == null || user.getName().isEmpty()) {
            showHelperText = true;
            return null;
        }
        return "hola?faces-redirect=true";
    }
}
