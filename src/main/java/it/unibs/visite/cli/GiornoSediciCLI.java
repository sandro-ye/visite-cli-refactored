package it.unibs.visite.cli;

import it.unibs.visite.controller.GiornoSediciController;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Volontario;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * CLI per la gestione delle operazioni del giorno 16.
 * Implementa il flusso obbligatorio:
 *  1 Chiusura raccolta disponibilità + generazione piano visite
 *  2 Gestione aggiunte/rimozioni
 *  3 Riapertura raccolta disponibilità
 */
public class GiornoSediciCLI {
    private final GiornoSediciController controller;
    private final TipoVisitaCLI tipoVisitaCLI;
    private final Scanner in;

    public GiornoSediciCLI(GiornoSediciController controller, TipoVisitaCLI tipoVisitaCLI, Scanner in) {
        this.in = in;
        this.controller = controller;
        this.tipoVisitaCLI = tipoVisitaCLI;
    }

    public void run() {
        System.out.println("\n=== MENU OPERAZIONI GIORNO 16 ===");
        produciPianoVisite();
        gestioneAggiunteRimozioni(); //aggiunta/rimozione volontari, luoghi, tipi visita, preclusioni
        riapriRaccoltaDisponibilita();
        System.out.println("\nTutte le operazioni del giorno 16 sono state completate.");
    }

    private void produciPianoVisite() {
        YearMonth target = YearMonth.now().plusMonths(1);
        System.out.println("Produzione piano visite per il mese i+1: " + target);

        for(LocalDate data : controller.giorniNonPreclusiIn(target)) {
            System.out.println("Data: " + data);
            for(TipoVisita tipo : controller.visiteProgrammabiliPerData().getOrDefault(data, List.of())) {
                System.out.println("  Tipo visita: " + tipo.getTitolo());
                List<String> volontari = controller.volontariDisponibiliInDataPerTipo(data, tipo).stream()
                    .map(v -> v.getNickname())
                    .toList();
                System.out.println("Scegli il volontario (nickname) da assegnare alla visite (invio per saltare): ");
                for (int i = 0; i < volontari.size(); i++) {
                    System.out.println((i + 1) + ") " + volontari.get(i));
                }

                String volontario = in.nextLine().trim();
                if (volontario.isEmpty()) {
                    System.out.println("Visita del " + data + " per tipo '" + tipo.getTitolo() + "' saltata.");
                    continue;
                }

                controller.salvaAssegnazioneVolontario(volontario, tipo, data);
            }
        }

        System.out.println("Piano visite per " + target + " prodotto con successo.");
    }

    private void gestioneAggiunteRimozioni() {
        while(true) {
            System.out.println("\nVuoi aggiungere/rimuovere volontari, luoghi o tipi di visita, o aggiungere preclusione? (s/n)");
            String choice = in.nextLine().trim().toLowerCase();
            if (choice.equals("n")) break;

            System.out.println("Cosa vuoi gestire?");
            System.out.println(" 1) Volontari");
            System.out.println(" 2) Luoghi");
            System.out.println(" 3) Tipi visita");
            System.out.println(" 4) Preclusione");
            System.out.println(" 0) Nessuna gestione, procedi alla riapertura raccolta disponibilità");
            System.out.print("Scelta: ");
            String entityChoice = in.nextLine().trim();
            switch (entityChoice) {
                case "1" -> gestioneVolontari();
                case "2" -> gestioneLuoghi();
                case "3" -> gestioneTipiVisita();
                case "4" -> aggiungiPreclusione();
                case "0" -> { return; }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void gestioneVolontari() {
        String choice = in.nextLine().trim();
        System.out.println("1) Aggiungi volontario");
        System.out.println("2) Rimuovi volontario");
        System.out.println("3) Associa volontario a tipo di visita");
        System.out.println("0) Nessuna gestione volontari, torna al menu precedente");
        System.out.print("Scelta: ");
        switch (choice) {
            case "1" -> aggiungiVolontario();
            case "2" -> rimuoviVolontario();
            case "3" -> associaVolontarioATipoVisita();
            case "0" -> { return; }
            default -> System.out.println("Scelta non valida.");
        }
    }

    private void aggiungiVolontario() {
        System.out.print("Inserisci nickname del nuovo volontario: ");
        String nickname = in.nextLine().trim();
        controller.aggiungiVolontario(nickname);
        System.out.println("Volontario '" + nickname + "' aggiunto con successo.");
    }

    private void rimuoviVolontario() {
        for(Volontario v : controller.getTuttiVolontari()) {
            System.out.println("- " + v.getNickname());
        }
        System.out.print("Inserisci nickname del volontario da rimuovere: ");
        String nickname = in.nextLine().trim();
        controller.rimuoviVolontario(nickname);
        System.out.println("Volontario '" + nickname + "' rimosso con successo.");
    }

    private void associaVolontarioATipoVisita() {
        for(Volontario v : controller.getTuttiVolontari()) {
            System.out.println("- " + v.getNickname());
        }
        System.out.print("Inserisci nickname del volontario da associare: ");
        String nickname = in.nextLine().trim();

        for(TipoVisita t : controller.getTuttiTipiVisita()) {
            System.out.println("- " + t.getTitolo());
        }
        System.out.print("Inserisci titolo del tipo di visita a cui associare il volontario: ");
        String titolo = in.nextLine().trim();

        controller.associaVolontarioATipoVisita(nickname, titolo);
        System.out.println("Volontario '" + nickname + "' associato al tipo di visita '" + titolo + "' con successo.");
    }

    private void gestioneLuoghi() {
        // simile a gestioneVolontari, con opzioni per aggiungere/rimuovere luoghi
        String choice = in.nextLine().trim();
        System.out.println("1) Aggiungi luogo");
        System.out.println("2) Rimuovi luogo");
        System.out.println("0) Nessuna gestione luoghi, torna al menu precedente");
        System.out.print("Scelta: ");
        switch (choice) {
            case "1" -> aggiungiLuogo();
            case "2" -> rimuoviLuogo();
            case "0" -> { return; }
            default -> System.out.println("Scelta non valida.");
        }
    }

    private void aggiungiLuogo() {
        System.out.print("Inserisci nome del nuovo luogo: ");
        String nome = in.nextLine().trim();
        System.out.println("Inserisci descrizione del luogo (opzionale, invio per saltare): ");
        String descrizione = in.nextLine().trim();
        controller.aggiungiLuogo(nome, descrizione.isEmpty() ? null : descrizione);
        System.out.println("Luogo '" + nome + "' aggiunto con successo.");
    }

    private void rimuoviLuogo() {
        for(Luogo l : controller.getTuttiLuoghi()) {
            System.out.println("- " + l.getNome());
        }
        System.out.print("Inserisci nome del luogo da rimuovere: ");
        String nome = in.nextLine().trim();
        controller.rimuoviLuogo(nome);
        System.out.println("Luogo '" + nome + "' rimosso con successo.");
    }

    private void gestioneTipiVisita() {
        // simile a gestioneVolontari, con opzioni per aggiungere/rimuovere tipi di visita
        String choice = in.nextLine().trim();
        System.out.println("1) Aggiungi tipo di visita");
        System.out.println("2) Rimuovi tipo di visita");
        System.out.println("0) Nessuna gestione tipi di visita, torna al menu precedente");
        System.out.print("Scelta: ");
        switch (choice) {
            case "1" -> aggiungiTipoVisita();
            case "2" -> rimuoviTipoVisita();
            case "0" -> { return; }
            default -> System.out.println("Scelta non valida.");
        }
    }

    private void aggiungiTipoVisita() {
        for(Luogo l : controller.getTuttiLuoghi()) {
            System.out.println("- " + l.getNome());
        }
        System.out.println("Inserisci nome del luogo associato (deve esistere): ");
        String luogo = in.nextLine().trim();
        System.out.println("Inserisci titolo del nuovo tipo di visita: ");
        String titolo = in.nextLine().trim();
        System.out.println("Inserisci descrizione del tipo di visita (opzionale, invio per saltare): ");
        String descrizione = in.nextLine().trim();
        controller.aggiungiTipoVisita(luogo, titolo, descrizione.isEmpty() ? null : descrizione);
        tipoVisitaCLI.completaCreazione(titolo);
        System.out.println("Tipo di visita '" + titolo + "' aggiunto con successo.");
    }

    private void rimuoviTipoVisita() {
        for(TipoVisita t : controller.getTuttiTipiVisita()) {
            System.out.println("- " + t.getTitolo());
        }
        System.out.print("Inserisci titolo del tipo di visita da rimuovere: ");
        String titolo = in.nextLine().trim();
        controller.rimuoviTipoVisita(titolo);
        System.out.println("Tipo di visita '" + titolo + "' rimosso con successo.");
    }

    private void aggiungiPreclusione() {
        YearMonth target = YearMonth.now().plusMonths(3);
        System.out.println("Imposteremo una preclusione per il mese i+3: " + target);
        System.out.print("Inserisci data da escludere (YYYY-MM-DD): ");
        String ds = in.nextLine().trim();
        try {
            LocalDate d = LocalDate.parse(ds);
            controller.aggiungiPreclusione(d);
            System.out.println("Preclusione aggiunta: " + d);
        } catch (DateTimeParseException ex) {
            System.out.println("Formato data non valido.");
        }
    }

    private void riapriRaccoltaDisponibilita() {
        System.out.println("Riapertura della raccolta delle disponibilità per " + LocalDate.now().getMonth().plus(2) + "...");
        controller.riapriRaccoltaDisponibilita();
        System.out.println("Raccolta disponibilità per " + LocalDate.now().getMonth().plus(2) + " riaperta con successo.");
    }

}

    /*
    private final ConfigService configService;
    private final Scanner scanner = new Scanner(System.in);

    public GiornoSediciCLI(ConfigService configService) {
        this.configService = configService;
    }

    public void start() {
        if (!configService.isGiornoSedici()) {
        throw new IllegalStateException("Le operazioni di gestione sono consentite solo il giorno 16 del mese.");
        }

        System.out.println("=====================================");
        System.out.println("    OPERAZIONI DEL GIORNO 16");
        System.out.println("=====================================\n");

        // Calcola mese corrente e mese successivo
        YearMonth meseCorrente = YearMonth.now();
        YearMonth meseProssimo = meseCorrente.plusMonths(1);
        YearMonth meseDopoProssimo = meseCorrente.plusMonths(2);

        // 1 CHIUSURA RACCOLTA DISPONIBILITÀ + GENERAZIONE PIANO
        System.out.println("[1] Chiusura raccolta disponibilità per " + meseProssimo + "...");
        try {
            
            if (configService.getSnapshot().getFaseCorrente() != AppPhase.RACCOLTA_DISPONIBILITA) {
                System.out.println("[WARN] Fase non coerente con la generazione del piano. Ripristino RACCOLTA_DISPONIBILITA...");
                configService.setPhase(AppPhase.RACCOLTA_DISPONIBILITA);
            }
            List<Visita> piano = configService.chiudiDisponibilitaEGeneraPiano(meseProssimo);
            System.out.println("Piano delle visite per " + meseProssimo + " generato con successo.\n");
            stampaPiano(piano, meseProssimo);
        } catch (Exception e) {
            System.out.println("Errore durante la generazione del piano: " + e.getMessage());
            return; // interrompe il flusso: senza piano non si prosegue
        }

        // 2 GESTIONE AGGIUNTE / RIMOZIONI
        System.out.println("[2] Ora puoi gestire aggiunte o rimozioni di luoghi, tipi di visita o volontari.");
        System.out.println("Premi INVIO per accedere al menu di gestione...");
        scanner.nextLine();
        new GestioneGiorno16CLI(scanner, new RegimeService(configService)).run();
        System.out.println("Gestione modifiche completata.\n");

        // 3 RIAPERTURA RACCOLTA DISPONIBILITÀ
        System.out.println("[3] Riapertura della raccolta delle disponibilità per " + meseDopoProssimo + "...");
        try {
            configService.riapriRaccoltaDisponibilita(meseDopoProssimo);
            System.out.println("Raccolta disponibilità per " + meseDopoProssimo + " aperta con successo.\n");
        } catch (Exception e) {
            System.out.println("Errore durante la riapertura: " + e.getMessage());
        }

        System.out.println("=========================================================");
        System.out.println(" Tutte le operazioni del giorno 16 sono state completate.");
        System.out.println("=========================================================");
    }

    private void stampaPiano(List<Visita> piano, YearMonth meseProssimo) {
        // Recupera e mostra il piano appena generato
        if (piano.isEmpty()) {
            System.out.println(" Nessuna visita generata per " + meseProssimo + ".");
        } else {
            System.out.println(" Piano visite per " + meseProssimo + ":");
            for (Visita v : piano) {
                TipoVisita tipo = configService.getSnapshot().getTipoVisita(v.getTipoVisitaId());
                Luogo luogo = configService.getSnapshot().getLuogo(tipo.getLuogoId());
                System.out.printf(
                    "• %s – %s (%s) con %s%n",
                    v.getData(),
                    tipo.getTitolo(),
                    luogo.getNome(),
                    v.getVolontarioNickname()
                );
            }
        }
        System.out.println();
    }

}
    */
