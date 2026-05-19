package it.unibs.visite.cli;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import it.unibs.visite.model.*;
import it.unibs.visite.controller.RegimeController;

public class RegimeCLI {
    private final RegimeController regimeController;
    private final GiornoSediciCLI giornoSediciCLI;
    private final Scanner in;

    public RegimeCLI(Scanner in, RegimeController regimeController, GiornoSediciCLI giornoSediciCLI) {
        this.in = in;
        this.regimeController = regimeController;
        this.giornoSediciCLI = giornoSediciCLI;
    }

    public void run() {
        if (regimeController.checkGiornoSedici()) {
            giornoSediciCLI.run();
        }
        while (true) {
            System.out.println("\n=== MENU FUNZIONI A REGIME ===");
            System.out.println("1) Aggiungi preclusione per mese i+3");
            System.out.println("2) Visualizza preclusioni mese i+3");
            System.out.println("3) Modifica max persone per iscrizione");
            System.out.println("4) Elenco volontari con tipi");
            System.out.println("5) Elenco luoghi");
            System.out.println("6) Tipi di visita per luogo");
            System.out.println("7) Elenco visite per stato");
            System.out.println("0) Esci menu regime");
            System.out.print("Scelta: ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1": cmdAddPreclusione(); break;
                    case "2": cmdShowPreclusioni(); break;
                    case "3": cmdSetMax(); break;
                    case "4": cmdVolontariConTipi(); break;
                    case "5": cmdElencoLuoghi(); break;
                    case "6": cmdTipiPerLuogo(); break;
                    case "7": cmdVisitePerStato(); break;
                    case "0": return;
                    default: System.out.println("Scelta non valida."); break;
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void cmdAddPreclusione() {
        YearMonth target = YearMonth.now().plusMonths(3);
        System.out.println("Imposteremo una preclusione per il mese i+3: " + target);
        System.out.print("Inserisci data da escludere (YYYY-MM-DD): ");
        String ds = in.nextLine().trim();
        try {
            LocalDate d = LocalDate.parse(ds);
            regimeController.aggiungiPreclusione(d);
            System.out.println("Preclusione aggiunta: " + d);
        } catch (DateTimeParseException ex) {
            System.out.println("Formato data non valido.");
        }
    }

    private void cmdShowPreclusioni() {
        YearMonth target = YearMonth.now().plusMonths(3);
        System.out.println("Preclusioni per " + target + ":");
        List<LocalDate> preclusioni = regimeController.getPreclusioniPer(target);
        preclusioni.forEach(System.out::println);
    }

    private void cmdSetMax() {
        System.out.print("Nuovo valore max persone per iscrizione (>0): ");
        String s = in.nextLine().trim();
        try {
            int v = Integer.parseInt(s);
            regimeController.setMaxPersonePerIscrizione(v);
            System.out.println("Valore aggiornato a " + v);
        } catch (NumberFormatException e) {
            System.out.println("Valore non valido.");
        }
    }

    private void cmdVolontariConTipi() {
        for(Volontario v: regimeController.getElencoVolontari()) {
            System.out.println("\nVolontario: " + v.getNickname());
            List<TipoVisita> tipi = regimeController.getTipiVisitaDi(v);
            if(tipi.isEmpty()) {
                System.out.println("  (nessun tipo di visita associato)");
            } else {
                for(TipoVisita t: tipi) {
                    System.out.println(" - " + t.getTitolo() + " (id: " + t.getId() + ")");
                }
            }
        }
    }

    private void cmdElencoLuoghi() {
        List<Luogo> luoghi = regimeController.getElencoLuoghi();
        if (luoghi.isEmpty()) System.out.println("Nessun luogo presente.");
        luoghi.forEach(System.out::println);
    }

    private void cmdTipiPerLuogo() {
        System.out.print("Inserisci id luogo: ");
        String id = in.nextLine().trim();
        List<TipoVisita> tipi = regimeController.tipiPerLuogo(id);
        if (tipi.isEmpty()) System.out.println("Nessun tipo trovato per questo luogo.");
        tipi.forEach(t -> System.out.println(" - " + t.getTitolo() + " (id: " + t.getId() + ")"));
    }

    private void cmdVisitePerStato() {
        for(StatoVisita s : StatoVisita.values()) {
            System.out.println("\n== " + s + " ==");
            List<Visita> list = regimeController.getVisitePerStato(s);
            if (list.isEmpty()) System.out.println(" (nessuna)");
            else list.forEach(v -> System.out.println("  " + v));
        }
    }
}