package it.unibs.visite.cli;

import java.util.Scanner;

import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.RegistrationService;

public class LoginMenuCLI {
    private final Scanner in;
    private final AuthService auth;
    private final RegistrationService reg;

    public LoginMenuCLI(Scanner in, AuthService auth, ConfigService config, FilePersistence persistence) {
        this.in = in;
        this.auth = auth;
        this.reg = new RegistrationService(config.getSnapshot(), persistence, auth);
    }

    public String showMenu() {
        System.out.println("=== ACCESSO VISITE GUIDATE ===");
        System.out.println("1) Registrati (fruitore)");
        System.out.println("2) Accedi");
        System.out.println("0) Esci");
        System.out.print("> ");

        String choice = in.nextLine().trim();
        return switch (choice) {
            case "1" -> registerUser();
            case "2" -> verify();
            case "0" -> null;
            default -> {
                System.out.println("Scelta non valida. Riprova.\n");
                yield showMenu();
            }
        };
    }

    public String registerUser() {
        String username = null;
        System.out.println("\n=== REGISTRAZIONE FRUITORE ===");
        while (true) {
            System.out.print("Username: ");
            String desired = in.nextLine().trim();

            if (!reg.isUsernameAvailable(desired)) {
                System.out.println("Errore: username già in uso (presente tra credenziali o fruitori). Riprova.\n");
                continue;
            }

            System.out.print("Password: ");
            char[] pass1 = in.nextLine().toCharArray();
            System.out.print("Conferma password: ");
            char[] pass2 = in.nextLine().toCharArray();

            if (!new String(pass1).equals(new String(pass2))) {
                System.out.println("Le password non coincidono. Riprova.\n");
                continue;
            }

            try {
                reg.registerFruitore(desired, pass1);
                System.out.println("Registrazione completata. Benvenuto, " + desired + "!");
                username = desired; // prosegui direttamente come fruitore
                break;
            } catch (IllegalArgumentException ex) {
                System.out.println("Registrazione fallita: " + ex.getMessage());
                        // ripeti ciclo
            }
        }
        return username;
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