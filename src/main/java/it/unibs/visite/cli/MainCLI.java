package it.unibs.visite.cli;

import java.time.LocalDate;
import java.util.Scanner;
import it.unibs.visite.controller.LoginController;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.controller.VisitBatchController;
/*
    - modifica login con LoginCLI
    - modifica wiring di VolunteerCLI
*/

/**
 * classe principale per avvio CLI dell'applicazione 
 * - violazione single responsibility principle 
 *    1) inizializza FilePersistence ma non dovrebbe occuparsene  
 *    2) provare a separare login/registrazione da menu principale
 *    3) mainCLI fa avvia classi di servizio come ConfigService e RegimeService --> da separare
 *  
 */

public class MainCLI {
    private final Scanner in;
    private final AuthService auth;
    private final VisitBatchController visitBatchController;

    public MainCLI(AuthService auth) {
        this.in = new Scanner(System.in);
        this.auth = auth;
        this.visitBatchController = new VisitBatchController();
    }

    public void run() {
        System.out.println("--------------------");
        System.out.println("|    VISITE CLI    |");
        System.out.println("--------------------");

        LoginCLI loginCLI = new LoginCLI(new LoginController(), in);
        String username = loginCLI.run();

        switch (auth.getUserRole(username)) {
            case "FRUITORE" -> {
                visitBatchController.eseguiBatch(LocalDate.now());
                new FruitoreCLI(username, in).run();
            }
            case "VOLONTARIO" -> {
                visitBatchController.eseguiBatch(LocalDate.now());
                new VolunteerCLI(username, in).run();
            }
            case "ADMIN" -> {
                visitBatchController.eseguiBatch(LocalDate.now());
                new InitWizardCLI(in).run();
                new RegimeCLI(in).run();
            }
        }
        System.out.println("Arrivederci!");
    }
/*

    private final Scanner in = new Scanner(System.in);
    private final FilePersistence persistence;
    private final AuthService auth;
    private final ConfigService config;

    public MainCLI() {
        // cartella dati ~/.visite-cli
        this.persistence = new FilePersistence(Paths.get(System.getProperty("user.home"), ".visite-cli"));
        this.auth = new AuthService(persistence);
        this.config = new ConfigService(persistence, auth);
    }

    public void run() {
        System.out.println("=== VISITE CLI  ===");
        System.out.println("(Configuratore o Volontario)\n");

        LoginMenuCLI loginMenu = new LoginMenuCLI(in, auth, config, persistence);

        String username = loginMenu.logUser();

        if(username == null) {
            System.out.println("Uscita dal programma. Arrivederci!");
            return;
        }
        
        // FRUITORE: entra nella CLI fruitore
        if (auth.isFruitore(username)) {
            FruitoreCLI fruitoreCLI = new FruitoreCLI(username, config);
            fruitoreCLI.run();
            return;
        }

        // VOLONTARIO (non admin): CLI volontario
        if (auth.isVolunteer(username) && !auth.isAdmin(username)) {
            VolunteerCLI vcli = new VolunteerCLI(config, username);
            vcli.run();
            return;
        }

        // ADMIN: esegui wizard se non inizializzato
        new InitWizardCLI(in, config).runWizard();
        // Menu principale (configuratore)
        mainMenu();
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
                case "1" -> new RegimeCLI(in, new RegimeService(config)).run();
                case "2" -> new InitWizardCLI(in, config).runWizard();
                case "0" -> {
                    System.out.println("Uscita dal programma. Arrivederci!");
                    System.exit(0);
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }
*/
}
