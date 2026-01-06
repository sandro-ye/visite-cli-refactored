package it.unibs.visite.cli;

import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.RegimeService;

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
}
