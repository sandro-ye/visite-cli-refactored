package it.unibs.visite.controller;

import it.unibs.visite.model.LoginResult;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.FruitoreService;

public class LoginController {
    private final AuthService authService;
    private final FruitoreService fruitoreService;

    public LoginController(AuthService authService, FruitoreService fruitoreService) {
        this.authService = authService;
        this.fruitoreService = fruitoreService;
    }

    public LoginResult login(String username, char[] password) {
        return authService.login(username, password);
    }

    public void registerUser(String username, char[] password) {
        authService.createFruitore(username, password);
        fruitoreService.registraFruitore(username);
    }

    public void passwordChange(String username, char[] pass1, char[] pass2) {
        authService.changePassword(username, pass1, pass2);
    }
}
