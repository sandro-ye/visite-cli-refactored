package it.unibs.visite.cli;

import it.unibs.visite.model.*;
import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.InitWizardService;

import java.util.*;
import java.util.stream.Collectors;

public class InitWizardCLI {
    private final Scanner scanner;
    private final ConfigService config;
    private final InitWizardService wizard;

    public InitWizardCLI(Scanner in, ConfigService config) {
        this.scanner = in;
        this.config = config;
        this.wizard = new InitWizardService(config);
    }


    public void runWizard() {
        System.out.println("\n=== WIZARD INIZIALIZZAZIONE ===");

        //Imposta i parametri globali al primo avvio
        ParametriSistema p = config.getSnapshot().getParametri();
        if (p.isInitialized()) {
            System.out.println("Il sistema risulta già inizializzato.");
            return;
        } else {
            impostaParametriSistema(p);
        }

        creaLuoghiConVisiteEVolontari();

        System.out.print("\nVuoi aggiungere nuovi tipi di visita a luoghi esistenti? (s/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            aggiungiTipiVisitaALuoghiEsistenti();
        }

        System.out.print("\nVuoi aggiungere nuovi volontari o associarli a tipi di visita esistenti? (s/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            aggiungiVolontariATipiEsistenti();
        }

        verificaInvariants();

    }

    // PARAMETRI DI SISTEMA
    private void impostaParametriSistema(ParametriSistema parametri) {
        System.out.println("\n=== Impostazione dei parametri di sistema ===");

        //Ambito territoriale (una tantum)
        while(true) {
            System.out.print("Abmito territoriale (es: 'Provincia di Parma'): ");
            String ambito = scanner.nextLine().trim();
            if (ambito.isEmpty()) {
                System.out.println("L'ambito territoriale è obbligatorio");
                continue;
            }
            try {
                parametri.setAmbitoTerritorialeUnaTantum(ambito);
                break;
            } catch (IllegalStateException e) {
                System.out.println("Ambito già impostato");
                return;
            }
        }

        //Numero massimo persone per iscrizione
        while(true) {
            System.out.print("Numero massimo di persone per iscrizione: ");
            String input = scanner.nextLine().trim();
            try {
                int max = Integer.parseInt(input);
                parametri.setMaxPersonePerIscrizione(max);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero intero valido.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        parametri.markInitialized();
        System.out.println("\nParametri impostati correttamente.\n");
    }

    private void creaLuoghiConVisiteEVolontari() {
        System.out.println("=== Creazione dei luoghi ===");
        System.out.println("È necessario creare almeno un luogo per poter proseguire.");

        boolean almenoUno = false;
        while (true) {
            System.out.print("Vuoi creare un nuovo luogo? (s/n): ");
            String risposta = scanner.nextLine().trim();

            if (risposta.isEmpty()) risposta = "n";
            if (risposta.equalsIgnoreCase("n")) {
                if (!almenoUno) {
                    System.out.println("Devi creare almeno un luogo prima di continuare");
                    continue;
                } else return;
            }

            System.out.print("Nome del luogo: ");
            String nome = scanner.nextLine().trim();
            System.out.print("Descrizione (facoltativa): ");
            String descrizione = scanner.nextLine().trim();
            
            Luogo luogo = new Luogo(nome, descrizione);
            config.getSnapshot().addLuogo(luogo);
            System.out.println("Luogo creato: " + luogo.getNome() + " (ID:" + luogo.getId() + ")");

            creaTipiVisitaPerLuogo(luogo);
            System.out.println();
            almenoUno = true;
        }
    }

    private void creaTipiVisitaPerLuogo(Luogo luogo) {
        System.out.println("\n=== Creazione tipi di visita per il luogo '" + luogo.getNome() + "' ===");
        System.out.println("Ogni luogo deve avere almeno un tipo di visita associato");

        boolean almenoUno = false;
        while(true) {
            System.out.print("Vuoi aggiungere un tipo di visita a questo luogo? (s/n): ");
            String risposta = scanner.nextLine().trim();
            if (risposta.equalsIgnoreCase("n")) {
                if (!almenoUno) {
                    System.out.println("Ogni luogo deve avere almeno un tipo di visita");
                    continue;
                } else return;
            }

            System.out.print("Titolo: ");
            String titolo = scanner.nextLine().trim();
            System.out.print("Descrizione: ");
            String descrizione = scanner.nextLine().trim();

            TipoVisita tipo = new TipoVisita(luogo.getId(), titolo, descrizione);
            config.getSnapshot().addTipoVisita(tipo);
            luogo.addTipoVisita(tipo);

            creaVolontariPerTipoVisita(tipo);
            almenoUno = true;
        }
    }

    private void creaVolontariPerTipoVisita(TipoVisita tipo) {
        System.out.println("\n=== Assegna volontari al tipo di visita '" + tipo.getTitolo() + "' ===");

        boolean almenoUno = false;
        while (true) {
            //Elenco volontari esistenti
            List<String> esistenti = config.getSnapshot().getVolontari().stream()
                    .map(Volontario::getNickname)
                    .collect(Collectors.toList());
            
            if (!esistenti.isEmpty()) { System.out.println("Volontari esistenti: " + esistenti); }

            System.out.print("Inserisci nickname volontario (nuovo o esistente, vuoto per terminare): ");
            String nickname = scanner.nextLine().trim();
            if (nickname.isEmpty()) {
                if (almenoUno) break;
                System.out.println("Devi associare almeno un volontario");
                continue;
            }

            Volontario volontario;
            if (!config.getSnapshot().volontarioEsiste(nickname)) {
                volontario = new Volontario(nickname);
                config.getSnapshot().addVolontario(volontario);
                config.getAuthService().createVolunteer(nickname, config.getAuthService().getPswrdDefaultVolontario().toCharArray());
                System.out.println("Creato nuovo volontario: " + nickname);
            } else {
                volontario = config.getSnapshot().getVolontario(nickname);
            }

            tipo.addVolontario(nickname, config.getSnapshot());
            System.out.println("Volontario '" + nickname + "' associato al tipo '" + tipo.getTitolo() + ".");
            almenoUno = true;
        }

    }

    private void aggiungiTipiVisitaALuoghiEsistenti() {
        List<Luogo> luoghi = new ArrayList<>(config.getSnapshot().getLuoghi());
        if (luoghi.isEmpty()) {
            System.out.println("Nessun luogo disponibile");
            return;
        }

        while(true) {
            System.out.println("\nLuoghi disponibili: ");
            for (int i = 0; i < luoghi.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, luoghi.get(i).getNome());
            }
            System.out.print("Seleziona il numero del luogo (0 per terminare): ");
            int scelta = Integer.parseInt(scanner.nextLine().trim());
            if (scelta <= 0 || scelta > luoghi.size()) break;

            Luogo selezionato = luoghi.get(scelta - 1);
            creaTipiVisitaPerLuogo(selezionato);
        }
    }

    private void aggiungiVolontariATipiEsistenti() {
        List<TipoVisita> tipi = new ArrayList<>(config.getSnapshot().getTipiVisita());
        if (tipi.isEmpty()) {
            System.out.println("Nessun tipo di visita disponibile.");
            return;
        }

        while(true) {
            System.out.println("\nTipi di visita disponibili: ");
            for (int i = 0; i < tipi.size(); i++) {
                TipoVisita t = tipi.get(i);
                Luogo l = config.getSnapshot().getLuogo(t.getLuogoId());
                String nomeLuogo = l != null ? l.getNome() : "(luogo sconosciuto)";
                System.out.printf("%d) %s (Luogo: %s)%n", i + 1, t.getTitolo(), nomeLuogo);
            }
            System.out.print("Seleziona il numero del tipo di visita (0 per terminare): ");
            int scelta = Integer.parseInt(scanner.nextLine().trim());
            if (scelta <= 0 || scelta > tipi.size()) break;

            TipoVisita selezionato = tipi.get(scelta - 1);
            creaVolontariPerTipoVisita(selezionato);
        }
    }


    private void verificaInvariants() {
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
