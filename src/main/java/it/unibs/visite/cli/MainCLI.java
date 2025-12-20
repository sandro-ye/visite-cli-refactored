package it.unibs.visite.cli;

import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.RegimeService;
import it.unibs.visite.service.RegistrationService;

import java.nio.file.Paths;
import java.util.Scanner;

/**
 * classe principale per avvio CLI dell'applicazione 
 * - violazione single responsibility principle 
 *    1) inizializza FilePersistence ma non dovrebbe occuparsene  
 *    2) provare a separare login/registrazione da menu principale
 *    3) mainCLI fa avvia classi di servizio come ConfigService e RegimeService --> da separare
 * - modificare funzionamento della CLI e conseguente logica di business con pattern strategy
 *  
 */

public class MainCLI {

    private final Scanner in = new Scanner(System.in);
    private final FilePersistence persistence;
    private final AuthService auth;
    private final ConfigService config;
    private final InitWizardCLI wizardCLI;
    private final RegimeCLI regimeCLI;
    private final RegistrationService reg;

    public MainCLI() {
        // cartella dati ~/.visite-cli
        this.persistence = new FilePersistence(Paths.get(System.getProperty("user.home"), ".visite-cli"));
        this.auth = new AuthService(persistence);
        this.config = new ConfigService(persistence, auth);

        // [MODIFICA] RegistrationService usa il DataStore CONDIVISO dell'applicazione
        this.reg  = new RegistrationService(config.getSnapshot(), persistence, auth);

        this.wizardCLI = new InitWizardCLI(in, config);
        this.regimeCLI = new RegimeCLI(in, new RegimeService(config));
        // Niente VolunteerCLI qui; verrà creato più avanti con l'username effettivo.
    }

    public void run() {
        System.out.println("=== VISITE CLI  ===");
        System.out.println("(Configuratore o Volontario)\n");

        String username = null;

        System.out.println("=== ACCESSO VISITE GUIDATE ===");
        System.out.println("1) Registrati (fruitore)");
        System.out.println("2) Accedi");
        System.out.println("0) Esci");
        System.out.print("> ");

        String choice = in.nextLine().trim();
        switch (choice) {
            case "1" -> {
                // [MODIFICA] Flusso registrazione con RegistrationService
                while (true) {
                    System.out.println("--- registrazione nuovo fruitore ---");
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
            }
            case "2" -> username = doLogin();
            case "0" -> {
                System.out.println("Uscita dal programma. Arrivederci!");
                System.exit(0);
            }
            default -> {
                System.out.println("Scelta non valida.");
                System.exit(1);
            }
        }

        // Se primo accesso → cambio password obbligatorio (tipicamente volontari/admin)
        if (auth.mustChangePassword(username)) {
            doChangePassword(username);
        }

        // FRUITORE: entra nella CLI fruitore
        if (auth.isFruitore(username)) {
            FruitoreCLI fruitoreCLI = new FruitoreCLI(username, config);
            fruitoreCLI.run();
            return;
        }

        // VOLONTARIO (non admin): CLI volontario
        if (auth.isVolunteer(username) && !auth.isAdmin(username)) {
            VolunteerCLI vcli = new VolunteerCLI(auth, config, username);
            vcli.run();
            return;
        }

        // CONFIGURATORE: inizializzazione se necessario
        if (!config.isInitialized()) {
            System.out.println("\nSistema non inizializzato. Avvio wizard di configurazione iniziale...");
            wizardCLI.runWizard();
            config.markInitialized();
            System.out.println("Wizard completato con successo.");
        }

        // Menu principale (configuratore)
        mainMenu();
    }

    // ================= LOGIN =====================
    public String doLogin() {
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

    // =========== Cambio password iniziale ============
    private void doChangePassword(String username) {
        System.out.println("\nCambio password obbligatorio (primo accesso).");
        while (true) {
            System.out.print("Nuova password: ");
            char[] pass1 = in.nextLine().toCharArray();
            System.out.print("Conferma nuova password: ");
            char[] pass2 = in.nextLine().toCharArray();

            if (new String(pass1).equals(new String(pass2))) {
                auth.changePassword(username, pass1);
                System.out.println("Password cambiata con successo.");
                break;
            } else {
                System.out.println("Le password non coincidono. Riprova.");
            }
        }
    }

    // =============== MENU PRINCIPALE =================
    private void mainMenu() {
        // Se è il 16 ci sono delle operazioni speciali da fare
        if (config.isGiornoSedici()) {
            System.out.println("\nOggi è il 16 del mese.");
            System.out.print("Vuoi eseguire le operazioni del giorno 16? (s/n): ");
            String risposta = in.nextLine().trim().toLowerCase();
            if (risposta.equals("s") || risposta.equals("si")) {
                try {
                    new GiornoSediciCLI(config).start();
                } catch (Exception e) {
                    System.out.println("Errore durante le operazioni del giorno 16: " + e.getMessage());
                    return;
                }
            }
        }

        while (true) {
            System.out.println("\n=== MENU PRINCIPALE ===");
            System.out.println("1) Funzioni a regime");
            System.out.println("2) Riesegui wizard di inizializzazione (se non completato)");
            System.out.println("0) Esci");
            System.out.print("Scelta: ");

            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> regimeCLI.run();
                case "2" -> {
                    if (!config.isInitialized()) {
                        wizardCLI.runWizard();
                        config.markInitialized();
                    } else {
                        System.out.println("Il wizard è già stato completato e non può essere rieseguito.");
                    }
                }
                case "0" -> {
                    System.out.println("Uscita dal programma. Arrivederci!");
                    System.exit(0);
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }
}
