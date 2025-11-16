package it.unibs.visite.cli;

import it.unibs.visite.Main;
import it.unibs.visite.persistence.FileAvailabilityRepository;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.VolunteerService;
import it.unibs.visite.service.adapters.ConfigReadAdapter;
import it.unibs.visite.service.ConfigService;  //già in V1

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.Clock;
import java.util.*;

public final class VolunteerCLI {

    private final Scanner in = new Scanner(System.in);
    private final AuthService auth;
    private final ConfigService config;
    private final String nickname; // utente già autenticato
    private final VolunteerService service;

    public VolunteerCLI(AuthService auth, ConfigService config, String nickname) {
        this.auth = auth;
        this.config = config;
        this.nickname = nickname;
        this.service = new VolunteerService(
                auth,
                new ConfigReadAdapter(config),
                new FileAvailabilityRepository(Paths.get("data", nickname + "_availabilities.ser")),
                Clock.systemDefaultZone()
        );

    }

    public void run() {
        System.out.println("=== Area VOLONTARIO ===");
        //String nick = loginFlow();
        try {
            mainMenu(nickname);
        } finally {
            System.out.println("Logout eseguito.\n");
        }
    }

    //  private String loginFlow() {
    //     while (true) {
    //         System.out.print("Nickname: ");
    //         String nick = in.nextLine().trim();
    //         System.out.print("Password: ");
    //         char[] pwd = System.console() != null ?
    //                 System.console().readPassword() :
    //                 in.nextLine().toCharArray();
    //         if (auth.login(nick, pwd) && auth.isVolunteer(nick)) {
    //             return nick;
    //         } else {
    //             System.out.println("Credenziali non valide o utente non è un volontario. Riprova.");
    //         }
    //     }
    // }

    private void mainMenu(String nick) {
        while (true) {
            System.out.println("\n--- Menu Volontario ---");
            System.out.println("1) I miei tipi di visita");
            System.out.println("2) Inserisci disponibilità per il mese entrante");
            System.out.println("3) Visualizza / revoca disponibilità (mese entrante)");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String s = in.nextLine().trim();
            switch (s) {
                case "1" -> showMyVisitTypes(nick);
                case "2" -> insertAvailabilities(nick);
                case "3" -> listAndRevoke(nick);
                case "0" -> { return; }
                default -> System.out.println("Scelta non valida.");
            }
        }
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
}
