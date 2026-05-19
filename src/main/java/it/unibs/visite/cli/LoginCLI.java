package it.unibs.visite.cli;

import java.util.Scanner;

import it.unibs.visite.controller.LoginController;
import it.unibs.visite.model.LoginResult;

public class LoginCLI {
    private final LoginController loginController;
    private final Scanner in;

    public LoginCLI(LoginController loginController, Scanner in) {
        this.in = in;
        this.loginController = loginController;
    }

    public String run() {
        System.out.println("=== ACCESSO VISITE GUIDATE ===");
        stampaMenu();
        int choice = leggiIntero(">");

        String verifiedUsername = null;

        switch(choice) {
            case 1 -> {
                System.out.println("\n=== REGISTRAZIONE FRUITORE ===");
                String username = leggiStringa("Username: ");
                String password = leggiStringa("Password: ");
                loginController.registerUser(username, password.toCharArray());
                verifiedUsername = username;
            }
            case 2 -> {
                System.out.println("\n=== LOGIN ===");
                String username = leggiStringa("Username: ");
                String password = leggiStringa("Password: ");
                LoginResult success = loginController.login(username, password.toCharArray());
                switch (success) {
                    case FIRST_ACCESS -> { 
                        changePassword(username);
                        verifiedUsername = username; 
                    }
                    case SUCCESS -> {
                        System.out.println("Login effettuato con successo.");
                        verifiedUsername = username;
                    }
                    case FAILURE -> {
                        System.out.println("Login fallito. Credenziali errate.");
                        return run(); // riprova login
                    }
                }
            }
            case 0 -> {
                System.out.println("Uscita in corso...");
            }
            default -> {
                System.out.println("Scelta non valida. Riprova.");
                return run(); // riprova menu
            }
        }
        return verifiedUsername;
    }

    private void stampaMenu() {
        System.out.println("1) Registrati");
        System.out.println("2) Accedi");
        System.out.println("0) Esci");
    }

    private void changePassword(String username) {
        System.out.println("Devi cambiare la password al primo accesso.");
        String pass1 = leggiStringa("Inserisci nuova password: ");
        String pass2 = leggiStringa("Ripeti nuova password: ");
        loginController.passwordChange(username, pass1.toCharArray(), pass2.toCharArray());
        System.out.println("Password cambiata con successo. Effettua il login.");
    }

    private int leggiIntero(String msg) {
        System.out.println(msg);
        while(!in.hasNextInt()) {
            in.nextLine();
            System.out.println("Input non valido. Riprova.");
        }
        int value = in.nextInt();
        in.nextLine();
        return value;
    }

    private String leggiStringa(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }
}
