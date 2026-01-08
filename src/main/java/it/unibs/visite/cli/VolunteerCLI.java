package it.unibs.visite.cli;

import it.unibs.visite.controller.VolunteerController;
import it.unibs.visite.model.TipoVisita;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class VolunteerCLI {
    private final VolunteerController controller;
    private final Scanner in;
    private final String nickname; // utente già autenticato
    
    public VolunteerCLI(VolunteerController controller, String nickname, Scanner in) {
        this.controller = controller;
        this.nickname = nickname;
        this.in = in;
    }

    public void run() {
        System.out.println("=== Area VOLONTARIO ===");
        try {
            mainMenu();
        } finally {
            System.out.println("Logout eseguito.\n");
        }
    }

    private void mainMenu() {
        while (true) {
            System.out.printf("\n--- Menu Volontario %s ---\n", nickname);
            System.out.println("1) I miei tipi di visita");
            System.out.println("2) Inserisci disponibilità per il mese entrante");
            System.out.println("3) Visualizza / revoca disponibilità (mese entrante)");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String s = in.nextLine().trim();
            switch (s) {
                case "1" -> mostraTipiVisita();
                case "2" -> inserisciDisponibilita();
                case "3" -> revocaDisponibilita();
                case "0" -> { return; }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void mostraTipiVisita() {
        List<TipoVisita> tipi = controller.visualizzaTipiVisita(nickname);
        if (tipi.isEmpty()) {
            System.out.println("Nessun tipo di visita associato.");
            return;
        }
        for (TipoVisita t : tipi) {
            System.out.printf(" - %f | %f", t.getTitolo(),  t.getDescrizione());
        }
    }

    private void inserisciDisponibilita() {
        YearMonth ym = controller.meseSuccessivo();
        System.out.println("Inserisci date disponibili nel mese entrante: " + ym);
        while(in.hasNextLine()) { 
            System.out.println("Formato: YYYY-MM-DD (vuoto per terminare)");
            System.out.print("> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) break;
            try {
                LocalDate date = LocalDate.parse(line);
                controller.inserisciDisponibilita(nickname, date);
                System.out.println("Disponibilità salvata.");
            } catch (DateTimeParseException ex) {
                System.out.println("Formato non valido.");
            } catch (Exception ex) {
                System.out.println("Errore: " + ex.getMessage());
            }
        }
    }

    private void revocaDisponibilita() {
        YearMonth ym = controller.meseSuccessivo();
        List<LocalDate> dates = new ArrayList<>();
        try {
            var disponibilita = controller.visualizzaDisponibilita(nickname, ym);
            if (disponibilita.isEmpty()) {
                System.out.println("Nessuna disponibilità registrata per " + ym);
                return;
            }
            System.out.println("Disponibilità registrate per " + ym + ":");
            int i = 1;
            for (var d : disponibilita) {
                System.out.println(" " + i + ") " + d.getData() + " (" + d.getData().getDayOfWeek() + ")");
                dates.add(d.getData());
                i++;
            }
            System.out.println("Digita il numero per revocare, oppure vuoto per uscire:");
            System.out.print("> ");
            String s = in.nextLine().trim();
            if (!s.isEmpty()) {
                int idx = Integer.parseInt(s);
                if (idx >= 1 && idx <= dates.size()) {
                    controller.revocaDisponibilita(nickname, dates.get(idx - 1));
                    System.out.println("Data rimossa.");
                }
            }
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }
/*
    public VolunteerCLI(ConfigService config, String nickname) {
        this.config = config;
        this.nickname = nickname;
        this.service = new VolunteerService(
                new ConfigReadAdapter(config),
                new FileAvailabilityRepository(Paths.get("data", nickname + "_availabilities.ser")),
                Clock.systemDefaultZone()
        );
    }

    private void showMyVisitTypes(String nick) {
        var types = service.visitTypesOf(nick);
        if (types.isEmpty()) {
            System.out.println("Nessun tipo di visita associato.");
            return;
        }
        System.out.println("Tipi di visita associati:");
        types.forEach(t -> System.out.println(" - " + t));
    }

    private void insertAvailabilities(String nick) {
        YearMonth ym = service.nextMonth();
        System.out.println("Inserisci date disponibili nel mese entrante: " + ym);
        System.out.println("Formato: YYYY-MM-DD (una per riga, vuoto per terminare)");
        List<LocalDate> toAdd = new ArrayList<>();
        while (true) {
            System.out.print("> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) break;
            try {
                toAdd.add(LocalDate.parse(line));
            } catch (DateTimeParseException ex) {
                System.out.println("Formato non valido.");
            }
        }
        try {
            service.declareAvailability(nick, toAdd);
            System.out.println("Disponibilità salvate.");
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private void listAndRevoke(String nick) {
        YearMonth ym = service.nextMonth();
        try {
            var dates = new TreeSet<>(service.myDatesForNextMonth(nick));
            if (dates.isEmpty()) {
                System.out.println("Nessuna disponibilità registrata per " + ym);
                return;
            }
            System.out.println("Disponibilità registrate per " + ym + ":");
            int i = 1;
            List<LocalDate> list = new ArrayList<>(dates);
            for (LocalDate d : list) {
                System.out.println(" " + i + ") " + d + " (" + d.getDayOfWeek() + ")");
                i++;
            }
            System.out.println("Digita il numero per revocare, oppure vuoto per uscire:");
            System.out.print("> ");
            String s = in.nextLine().trim();
            if (!s.isEmpty()) {
                int idx = Integer.parseInt(s);
                if (idx >= 1 && idx <= list.size()) {
                    service.revokeAvailability(nick, list.get(idx - 1));
                    System.out.println("Data rimossa.");
                }
            }
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }
    */
}
