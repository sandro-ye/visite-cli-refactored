package it.unibs.visite.cli;

import java.util.Scanner;
import it.unibs.visite.security.AuthService;

public class LoginMenuCLI {
    private final Scanner in;
    private final AuthService auth;

    public LoginMenuCLI(Scanner in, AuthService auth) {
        this.in = in;
        this.auth = auth;
    }

    public String verify() {
        System.out.println("\n=== LOGIN CONFIGURATORE, VOLONTARIO E FRUITORE ===");
        while (true) {
            System.out.print("Username: ");
            String username = in.nextLine().trim();
            System.out.print("Password: ");
            char[] password = in.nextLine().toCharArray();

            if (auth.login(username, password)) {
                System.out.println("Login riuscito.");
                return username;
            } else {
                System.out.println("Credenziali non valide. Riprova.");
            }
        }
    }
}