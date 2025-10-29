package it.unibs.visite;

import it.unibs.visite.cli.InitWizardCLI;
import it.unibs.visite.cli.MainCLI;
import it.unibs.visite.cli.RegimeCLI;
import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.RegimeService;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        /*
        Scanner in = new Scanner(System.in);
        FilePersistence fp = new FilePersistence(Path.of(System.getProperty("user.home"), ".visite-cli"));
        fp.ensureDirs();
        ConfigService config = new ConfigService(fp);
        AuthService auth = new AuthService(fp);

        System.out.println("== Gestione Visite — CLI ==");
        String username;
        while (true) {
            System.out.print("Username: "); username = in.nextLine();
            System.out.print("Password: "); char[] pwd = in.nextLine().toCharArray();
            if (auth.login(username, pwd)) break;
            System.out.println("Credenziali non valide.");
        }

        // Se primo accesso (admin/admin) forza cambio password
        if (auth.mustChangePassword(username)) {
            System.out.println("È necessario cambiare la password.");
            while (true) {
                System.out.print("Nuova password: "); char[] p1 = in.nextLine().toCharArray();
                System.out.print("Ripeti password: "); char[] p2 = in.nextLine().toCharArray();
                if (!java.util.Arrays.equals(p1, p2)) { System.out.println("Le password non coincidono."); continue; }
                if (p1.length < 6) { System.out.println("Min 6 caratteri."); continue; }
                auth.changePassword(username, p1);
                System.out.println("Password aggiornata.");
                break;
            }
        }

        // Wizard inizializzazione una tantum
        if (!config.isInitialized()) {
            System.out.println("\nIl sistema non è ancora inizializzato.");
            new InitWizardCLI(in, config).runWizard();
        } else {
            System.out.println("\nSistema già inizializzato.");
            System.out.println("Ambito: " + config.getSnapshot().getParametri().getAmbitoTerritoriale() +
                               " — Max per iscrizione: " + config.getSnapshot().getParametri().getMaxPersonePerIscrizione());
            RegimeService regimeService = new RegimeService(config);
            new RegimeCLI(in, regimeService).run();
        }

        System.out.println("\nBye.");
        */

        new MainCLI().run();
    }
}
