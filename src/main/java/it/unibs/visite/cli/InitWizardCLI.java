package it.unibs.visite.cli;

import it.unibs.visite.model.*;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.InitWizardService;
import it.unibs.visite.security.AuthService;

import java.util.*;

public class InitWizardCLI {
    private final Scanner in;
    private final ConfigService config;
    private final InitWizardService wizard;

    //V3
    /*private final String passwordInizialeVolontario = "123";
    private AuthService auth;

    public InitWizardCLI(Scanner in, ConfigService config, AuthService auth) {
        this.in = in;
        this.config = config;
        this.auth = auth;
        this.wizard = new InitWizardService(config);
    }*/

    public InitWizardCLI(Scanner in, ConfigService config) {
        this.in = in;
        this.config = config;
        this.wizard = new InitWizardService(config);
    }

    public void runWizard() {
        System.out.println("\n=== WIZARD INIZIALIZZAZIONE ===");

        // 1) Ambito territoriale
        if (config.getSnapshot().getParametri().getAmbitoTerritoriale() == null) {
            System.out.print("Ambito territoriale (es. 'Comune di Palermo'): ");
            String ambito = in.nextLine();
            config.setAmbitoUnaTantum(ambito);
        } else {
            System.out.println("Ambito già impostato: " + config.getSnapshot().getParametri().getAmbitoTerritoriale());
        }

        // 2) Max persone per iscrizione
        while (true) {
            try {
                System.out.print("Max persone per iscrizione (>0): ");
                int max = Integer.parseInt(in.nextLine());
                config.setMaxPersone(max);
                break;
            } catch (Exception e) {
                System.out.println("Valore non valido: " + e.getMessage());
            }
        }

        // 3) Luoghi
        System.out.println("\n--- Inserimento Luoghi ---");
        while (true) {
            System.out.print("Aggiungere un luogo? (s/n): ");
            if (!in.nextLine().trim().equalsIgnoreCase("s")) break;
            System.out.print("Nome luogo: "); String nome = in.nextLine();
            System.out.print("Descrizione: "); String descr = in.nextLine();
            Luogo l = wizard.addLuogo(nome, descr);
            System.out.println("Creato luogo id=" + l.getId());
        }

        // 4) Volontari
        System.out.println("\n--- Inserimento Volontari ---");
        while (true) {
            System.out.print("Aggiungere un volontario? (s/n): ");
            if (!in.nextLine().trim().equalsIgnoreCase("s")) break;
            System.out.print("Nickname (univoco): "); String nick = in.nextLine();
            try {
                Volontario v = wizard.addVolontario(nick);
                config.getAuthService().createVolunteer(nick, config.getAuthService().getPswrdDefaultVolontario().toCharArray());;

                System.out.println("Creato volontario: " + v.getNickname());
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }

        // 5) Tipi di visita + associazione >=1 volontario
        System.out.println("\n--- Tipi di Visita ---");
        var luoghi = new ArrayList<>(config.getSnapshot().getLuoghi());
        if (luoghi.isEmpty()) {
            System.out.println("Nessun luogo: crea almeno un luogo prima.");
        } else {
            Map<Integer, Luogo> idx2Luogo = new LinkedHashMap<>();
            int i = 1;
            for (Luogo l : luoghi) { idx2Luogo.put(i, l); System.out.printf("%d) %s%n", i++, l.getNome()); }
            while (true) {
                System.out.print("Aggiungere un tipo di visita? (s/n): ");
                if (!in.nextLine().trim().equalsIgnoreCase("s")) break;

                int scelta;
                while (true) {
                    try {
                        System.out.print("Scegli luogo (numero): ");
                        scelta = Integer.parseInt(in.nextLine());
                        if (!idx2Luogo.containsKey(scelta)) throw new RuntimeException();
                        break;
                    } catch (Exception e) { System.out.println("Scelta non valida."); }
                }
                Luogo l = idx2Luogo.get(scelta);
                System.out.print("Titolo tipo visita: "); String titolo = in.nextLine();
                System.out.print("Descrizione: "); String desc = in.nextLine();
                TipoVisita t = wizard.addTipoVisita(l.getId(), titolo, desc);

                // associa almeno un volontario
                var volontari = new ArrayList<>(config.getSnapshot().getVolontari());
                if (volontari.isEmpty()) {
                    System.out.println("Attenzione: non ci sono volontari, creane almeno uno prima.");
                } else {
                    System.out.println("Associa volontari (almeno uno). Disponibili:");
                    for (Volontario v : volontari) System.out.println(" - " + v.getNickname());
                    boolean added = false;
                    while (true) {
                        System.out.print("Nickname volontario da associare (invio per terminare): ");
                        String nick = in.nextLine().trim();
                        if (nick.isEmpty()) {
                            if (added) break;
                            System.out.println("Devi associare almeno un volontario.");
                            continue;
                        }
                        try {
                            wizard.assocVolontarioATipo(t.getId(), nick);
                            added = true;
                            System.out.println("Associato.");
                        } catch (Exception e) { System.out.println("Errore: " + e.getMessage()); }
                    }
                }
            }
        }

        // 6) Validazione invarianti
        try {
            wizard.validateInvariants();
            config.markInitialized();
            System.out.println("\nWizard completato. Sistema inizializzato.");
        } catch (Exception e) {
            System.out.println("Errore di validazione: " + e.getMessage());
            System.out.println("Correggere i dati (ad es. associare almeno un volontario per ogni tipo di visita).");
        }
    }
}
