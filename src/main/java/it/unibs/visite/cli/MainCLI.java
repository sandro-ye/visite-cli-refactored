package it.unibs.visite.cli;

import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.InitWizardService;
import it.unibs.visite.service.RegimeService;
import it.unibs.visite.cli.VolunteerCLI;

import java.nio.file.Paths;
import java.util.Scanner;

//Possiamo rendere questa classe il main del programma

public class MainCLI {

    private final Scanner in = new Scanner(System.in);
    private final FilePersistence persistence;
    private final AuthService auth;
    private final ConfigService config;
    private final InitWizardCLI wizardCLI;
    private final RegimeCLI regimeCLI;
    private final VolunteerCLI volunteerCLI;

    public MainCLI() {
        // cartella dati ~/.visite-cli
        this.persistence = new FilePersistence(Paths.get(System.getProperty("user.home"), ".visite-cli"));
        this.auth = new AuthService(persistence);
        this.config = new ConfigService(persistence);
        this.wizardCLI = new InitWizardCLI(in, config);
        this.regimeCLI = new RegimeCLI(in, new RegimeService(config));
        this.volunteerCLI = new VolunteerCLI(auth, config, null);
    }

   public void run() {
    System.out.println("=== VISITE CLI - VERSIONE 2 ===");
    System.out.println("(Configuratore o Volontario)\n");

    // LOGIN
    String username = doLogin();

    // Se primo accesso → cambio password obbligatorio
    if (auth.mustChangePassword(username)) {
        doChangePassword(username);
    }

    // SE È VOLONTARIO → vai direttamente nel template volontario
    if (auth.isVolunteer(username) && !auth.isAdmin(username)) {
        VolunteerCLI vcli = new VolunteerCLI(auth, config, username);
        vcli.run();
        return; // esci dal metodo run() dopo il logout del volontario
    }

    // Se non inizializzato → wizard iniziale (solo configuratore)
    if (!config.isInitialized()) {
        System.out.println("\nSistema non inizializzato. Avvio wizard di configurazione iniziale...");
        wizardCLI.runWizard();
        config.markInitialized();
        System.out.println("Wizard completato con successo.");
    }

    // Menu principale (solo per configuratore)
    mainMenu();
}


    // ================= LOGIN =====================
    public String doLogin() {
        System.out.println("\n=== LOGIN CONFIGURATORE E VOLONTARIO===");
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
    /*
     * // MAIN ENTRY POINT
     * public static void main(String[] args) {
     * new MainCLI().run();
     * }
     */
}