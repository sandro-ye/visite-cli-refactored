package it.unibs.visite.controller;

import it.unibs.visite.model.LoginResult;
import it.unibs.visite.security.AuthService;

public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public LoginResult login(String username, char[] password) {
        return authService.login(username, password);
    }

    public void registerUser(String username, char[] password) {
        authService.createFruitore(username, password);
    }

    public void passwordChange(String username, char[] pass1, char[] pass2) {
        authService.changePassword(username, pass1, pass2);
    }
}
