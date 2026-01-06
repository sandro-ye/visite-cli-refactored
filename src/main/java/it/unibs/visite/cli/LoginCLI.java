package it.unibs.visite.cli;

import java.util.Scanner;

import it.unibs.visite.controller.LoginController;
import it.unibs.visite.model.LoginResult;

/**
 * da aggiungere cli per cambio password
 */
public class LoginCLI {
    private final LoginController loginController;
    private final Scanner in;

    public LoginCLI(LoginController loginController, Scanner in) {
        this.in = in;
        this.loginController = loginController;
    }

    public void run() {
        System.out.println("=== ACCESSO VISITE GUIDATE ===");
        stampaMenu();
        int choice = leggiIntero(">");

        switch(choice) {
            case 1 -> {
                System.out.println("\n=== REGISTRAZIONE FRUITORE ===");
                String username = leggiStringa("Username: ");
                String password = leggiStringa("Password: ");
                loginController.registerUser(username, password.toCharArray());
            }
            case 2 -> {
                System.out.println("\n=== LOGIN ===");
                String username = leggiStringa("Username: ");
                String password = leggiStringa("Password: ");
                LoginResult success = loginController.login(username, password.toCharArray());
                switch (success) {
                    case FIRST_ACCESS -> changePassword(username);
                    case SUCCESS -> System.out.println("Login effettuato con successo.");
                    case FAILURE -> System.out.println("Login fallito. Credenziali errate.");
                }
            }
            case 0 -> {
                System.out.println("Uscita in corso...");
                return; 
            }
        }
    }

    private void stampaMenu() {
        System.out.println("1) Registrati");
        System.out.println("2) Accedi");
        System.out.println("0) Esci");
        System.out.print("> ");
    }

    private void changePassword(String username) {
        System.out.println("Devi cambiare la password al primo accesso.");
        String pass1 = leggiStringa("Vecchia password: ");
        String pass2 = leggiStringa("Nuova password: ");
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
