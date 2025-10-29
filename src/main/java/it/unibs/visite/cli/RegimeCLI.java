package it.unibs.visite.cli;

import it.unibs.visite.service.RegimeService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import it.unibs.visite.model.StatoVisita;
import it.unibs.visite.model.Visita;

public class RegimeCLI {
    private final Scanner in;
    private final RegimeService regime;

    public RegimeCLI(Scanner in, RegimeService regime) {
        this.in = in;
        this.regime = regime;
    }

    public void run() {
        while (true) {
            System.out.println("\n=== MENU FUNZIONI A REGIME (V1) ===");
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
            regime.addPreclusioneForMonth(target, d);
            System.out.println("Preclusione aggiunta: " + d);
        } catch (DateTimeParseException ex) {
            System.out.println("Formato data non valido.");
        }
    }

    private void cmdShowPreclusioni() {
        YearMonth target = YearMonth.now().plusMonths(3);
        System.out.println("Preclusioni per " + target + ": " + regime.getPreclusioniFor(target));
    }

    private void cmdSetMax() {
        System.out.print("Nuovo valore max persone per iscrizione (>0): ");
        String s = in.nextLine().trim();
        try {
            int v = Integer.parseInt(s);
            regime.setMaxPersone(v);
            System.out.println("Valore aggiornato a " + v);
        } catch (NumberFormatException e) {
            System.out.println("Valore non valido.");
        }
    }

    private void cmdVolontariConTipi() {
        List<String> rows = regime.elencoVolontariConTipi();
        if (rows.isEmpty()) System.out.println("Nessun volontario presente.");
        rows.forEach(System.out::println);
    }

    private void cmdElencoLuoghi() {
        List<String> rows = regime.elencoLuoghi();
        if (rows.isEmpty()) System.out.println("Nessun luogo presente.");
        rows.forEach(System.out::println);
    }

    private void cmdTipiPerLuogo() {
        System.out.print("Inserisci id luogo: ");
        String id = in.nextLine().trim();
        List<String> rows = regime.tipiPerLuogo(id);
        if (rows.isEmpty()) System.out.println("Nessun tipo trovato per questo luogo.");
        rows.forEach(System.out::println);
    }

    private void cmdVisitePerStato() {
        Map<StatoVisita, List<Visita>> map = regime.visitePerStato();
        for (StatoVisita s : StatoVisita.values()) {
            System.out.println("\n== " + s + " ==");
            List<Visita> list = map.getOrDefault(s, List.of());
            if (list.isEmpty()) System.out.println(" (nessuna)");
            else list.forEach(v -> System.out.println("  " + v));
        }
    }
}