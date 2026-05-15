package it.unibs.visite.cli;

import java.util.Scanner;
import it.unibs.visite.controller.InitWizardController;

public class InitWizardCLI {
    private final InitWizardController controller;
    private final Scanner in;

    public InitWizardCLI(Scanner in) {
        this.controller = new InitWizardController();
        this.in = in;
    }

    public void run() {
        System.out.println("=== WIZARD INIZIALIZZAZIONE ===");

        if(controller.isInitialized()) {
            System.out.println("Il sistema risulta già inizializzato.");
            return;
        } else {
            impostaParametriSistema();
        }

        creaLuoghiConVisiteEVolontari();

        if (leggiStringa("\nVuoi aggiungere nuovi tipi di visita a luoghi esistenti? (s/n): ").equalsIgnoreCase("s")) {
            aggiungiTipiVisitaALuoghiEsistenti();
        }

        if (leggiStringa("\nVuoi aggiungere nuovi volontari o associarli a tipi di visita esistenti? (s/n): ").equalsIgnoreCase("s")) {
            aggiungiVolontariATipiEsistenti();
        }

        verificaInvariants();
        System.out.println("\nWizard completato. Sistema inizializzato.");
    }

    private void impostaParametriSistema() {
        System.out.println("\n=== Impostazione dei parametri di sistema ===");

        String ambito = leggiStringa("Abmito territoriale (es: 'Provincia di Parma'): ");
        while(ambito == null || ambito.isEmpty()) {
            System.out.println("L'ambito territoriale è obbligatorio");
            ambito = leggiStringa(">");
        }

        int maxPersonePerIscrizione = leggiIntero("Numero massimo di persone per iscrizione: ");
            while (maxPersonePerIscrizione <= 0) {
                System.out.println("Il numero deve essere positivo.");
                maxPersonePerIscrizione = leggiIntero("Numero massimo di persone per iscrizione: ");
            }
        
        controller.avvia(ambito, maxPersonePerIscrizione);
        System.out.println("\nParametri impostati correttamente.\n");
    }

    private void creaLuoghiConVisiteEVolontari() {
        System.out.println("=== Creazione dei luoghi ===");
        System.out.println("È necessario creare almeno un luogo per poter proseguire.");

        while (true) {
            String risposta = leggiStringa("Vuoi creare un nuovo luogo? (s/n): ");
            if (risposta.isEmpty()) risposta = "n";
            if (risposta.equalsIgnoreCase("n")) {
                if (!controller.hasLuoghi()) {
                    System.out.println("Devi creare almeno un luogo prima di continuare");
                    continue;
                } else return;
            }

            String nome = leggiStringa("Nome del luogo: ");
            String descrizione = leggiStringa("Descrizione (facoltativa): ");

            controller.addLuogo(nome, descrizione);
            System.out.println("Luogo creato: " + nome);

            creaTipiVisitaPerLuogo(nome);
        }
    }

    private void creaTipiVisitaPerLuogo(String nomeLuogo) {
        System.out.println("\n=== Creazione tipi di visita per il luogo '" + nomeLuogo + "' ===");
        System.out.println("Ogni luogo deve avere almeno un tipo di visita associato");

        while(true) {
            String risposta = leggiStringa("Vuoi aggiungere un tipo di visita a questo luogo? (s/n): ");
            if (risposta.isBlank()) risposta = "n";
            if (risposta.equalsIgnoreCase("n")) {
                if (!controller.hasTipiVisita()) {
                    System.out.println("Ogni luogo deve avere almeno un tipo di visita per luogo");
                    continue;
                } else return;
            }

            String titolo = leggiStringa("Titolo: ");
            String descrizione = leggiStringa("Descrizione: ");
            controller.aggiungiTipoVisitaALuogo(nomeLuogo, titolo, descrizione);

            new TipoVisitaCLI(titolo, in).run();

            System.out.println("Tipo di visita '" + titolo + "' creato per il luogo '" + nomeLuogo + "'.");

            creaVolontariPerTipoVisita(titolo);
        }
    }

    private void creaVolontariPerTipoVisita(String nomeTipoVisita) {
        System.out.println("\n=== Assegna volontari al tipo di visita '" + nomeTipoVisita + "' ===");

        while (true) {
            System.out.println("Volontari esistenti: ");
            controller.getAllVolontariNicknames().forEach(n -> System.out.println(" - " + n));
            String nickname = leggiStringa("Inserisci nickname volontario (nuovo o esistente, vuoto per terminare): ");
            if (nickname.isEmpty()) {
                if (!controller.hasVolontari()) {
                    System.out.println("Devi associare almeno un volontario");
                    continue;
                } else break;
            }

            controller.aggiungiVolontarioATipoVisita(nickname, nomeTipoVisita);
            System.out.println("Volontario '" + nickname + "' associato al tipo '" + nomeTipoVisita + "'.");
        }
    }

    private void aggiungiTipiVisitaALuoghiEsistenti() {
        System.out.println("\n=== Aggiunta tipi di visita a luoghi esistenti ===");
        controller.getAllLuoghiNames().forEach(name -> System.out.println(" - " + name));
        String nomeLuogo = leggiStringa("Inserisci il nome del luogo a cui vuoi aggiungere un tipo di visita (vuoto per terminare): ");
        while(!nomeLuogo.isEmpty()) {
            String titolo = leggiStringa("Titolo: ");
            String descrizione = leggiStringa("Descrizione: ");
            controller.aggiungiTipoVisitaALuogo(nomeLuogo, titolo, descrizione);
            new TipoVisitaCLI(titolo, in).run();
            System.out.println("Tipo di visita '" + titolo + "' creato per il luogo '" + nomeLuogo + "'.");
        }
    }

    private void aggiungiVolontariATipiEsistenti() {
        System.out.println("\n=== Aggiunta volontari a tipi di visita esistenti ===");
        controller.getAllTipiVisitaNames().forEach(name -> System.out.println(" - " + name));
        String nomeTipoVisita = leggiStringa("Inserisci il nome del tipo di visita a cui vuoi aggiungere un volontario (vuoto per terminare): ");
        while(!nomeTipoVisita.isEmpty()) {
            String nickname = leggiStringa("Inserisci il nickname del volontario (vuoto per terminare): ");
            if (nickname.isEmpty()) {
                break;
            }
            controller.aggiungiVolontarioATipoVisita(nickname, nomeTipoVisita);
            System.out.println("Volontario '" + nickname + "' associato al tipo '" + nomeTipoVisita + "'.");
        }
    }

    private void verificaInvariants() {
        try {
            controller.validateInvariants();
        } catch (Exception e) {
            System.out.println("Errore di validazione: " + e.getMessage());
            System.out.println("Correggere i dati (ad es. associare almeno un volontario per ogni tipo di visita).");
        }
    }

    private String leggiStringa(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private int leggiIntero(String msg) {
        System.out.print(msg);
        while(!in.hasNextInt()) {
            in.nextLine();
            System.out.println("Input non valido. Riprova.");
        }
        int value = in.nextInt();
        in.nextLine();
        return value;
    }

    /*
    private final Scanner scanner;
    private final InitWizardService wizard;

    public InitWizardCLI(Scanner in, ConfigService config) {
        this.scanner = in;
        this.wizard = new InitWizardService(config);
    }


    public void runWizard() {
        System.out.println("\n=== WIZARD INIZIALIZZAZIONE ===");

        //Imposta i parametri globali al primo avvio
        if (wizard.isInitialized()) {
            System.out.println("Il sistema risulta già inizializzato.");
            return;
        } else {
            impostaParametriSistema(wizard.getParametriSistema());
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
            wizard.addLuogo(luogo);
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

            TipoVisita tipo = wizard.addTipoVisitaInDS(luogo.getId(), titolo, descrizione);
            luogo.addTipoVisita(tipo);

            creaVolontariPerTipoVisita(tipo.getId(), tipo.getTitolo());
            almenoUno = true;
        }
    }

    private void creaVolontariPerTipoVisita(String tipoId, String titolo) {
        System.out.println("\n=== Assegna volontari al tipo di visita '" + titolo + "' ===");

        boolean almenoUno = false;
        while (true) {
            List<String> esistenti = wizard.getAllVolontariNicknames();

            if (!esistenti.isEmpty()) { System.out.println("Volontari esistenti: " + esistenti); }

            System.out.print("Inserisci nickname volontario (nuovo o esistente, vuoto per terminare): ");
            String nickname = scanner.nextLine().trim();
            if (nickname.isEmpty()) {
                if (almenoUno) break;
                System.out.println("Devi associare almeno un volontario");
                continue;
            }

            Volontario volontario = wizard.getVolontario(nickname);
            wizard.assocVolontarioATipo(tipoId, volontario.getNickname());
            System.out.println("Volontario '" + volontario.getNickname() + "' associato al tipo '" + titolo + ".");
            almenoUno = true;
        }

    }

    private void aggiungiTipiVisitaALuoghiEsistenti() {
        List<Luogo> luoghi = new ArrayList<>(wizard.getSnapshot().getLuoghi());
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
        List<TipoVisita> tipi = new ArrayList<>(wizard.getSnapshot().getTipiVisita());
        if (tipi.isEmpty()) {
            System.out.println("Nessun tipo di visita disponibile.");
            return;
        }

        while(true) {
            System.out.println("\nTipi di visita disponibili: ");
            for (int i = 0; i < tipi.size(); i++) {
                TipoVisita t = tipi.get(i);
                Luogo l = wizard.getSnapshot().getLuogo(t.getLuogoId());
                String nomeLuogo = l != null ? l.getNome() : "(luogo sconosciuto)";
                System.out.printf("%d) %s (Luogo: %s)%n", i + 1, t.getTitolo(), nomeLuogo);
            }
            System.out.print("Seleziona il numero del tipo di visita (0 per terminare): ");
            int scelta = Integer.parseInt(scanner.nextLine().trim());
            if (scelta <= 0 || scelta > tipi.size()) break;

            TipoVisita selezionato = tipi.get(scelta - 1);
            creaVolontariPerTipoVisita(selezionato.getId(), selezionato.getTitolo());
        }
    }


    private void verificaInvariants() {
        try {
            wizard.validateInvariants();
            wizard.markInitialized();
            System.out.println("\nWizard completato. Sistema inizializzato.");
        } catch (Exception e) {
            System.out.println("Errore di validazione: " + e.getMessage());
            System.out.println("Correggere i dati (ad es. associare almeno un volontario per ogni tipo di visita).");
        }
    }

    */
}
