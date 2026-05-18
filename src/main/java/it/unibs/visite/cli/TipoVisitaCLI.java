package it.unibs.visite.cli;

import java.util.Scanner;
import it.unibs.visite.controller.TipoVisitaController;

public class TipoVisitaCLI {
    private final TipoVisitaController controller;
    private final Scanner in;

    public TipoVisitaCLI(Scanner in, TipoVisitaController controller) {
        this.controller = controller;
        this.in = in;
    }

    public void completaCreazione(String nomeTipoVisita) {
        System.out.println("=== SETUP TIPO VISITA ===");

        String puntoIncontro = leggiStringa("Punto d'incontro: ");

        
        String dataInizio = leggiStringa("Data inizio (YYYY-MM-DD): ");
        String dataFine = leggiStringa("Data fine (YYYY-MM-DD): ");

        // giorni della settimana
        System.out.println("Inserisci i giorni della settimana in cui la visita è disponibile.");
        System.out.println("Scrivi i nomi separati da virgola (es: LUNEDI,MARTEDI,VENERDI).");
        System.out.println("Giorni validi: LUNEDI, MARTEDI, MERCOLEDI, GIOVEDI, VENERDI, SABATO, DOMENICA");
        String inputGiorni = leggiStringa("Giorni: ");

        // ora di inizio
        String oraInizio = leggiStringa("Ora di inizio (HH:MM): ");

        // durata
        int durataMinuti = leggiIntero("Durata (minuti): ");

        // biglietto
        String rispostaBiglietto = leggiStringa("È richiesto un biglietto d'ingresso? (s/n): ");
        boolean bigliettoRichiesto = rispostaBiglietto.equalsIgnoreCase("s");

        // numero minimo partecipanti
        int numeroMinimoPartecipanti = leggiIntero("Numero minimo partecipanti: ");

        // numero massimo partecipanti
        int numeroMassimoPartecipanti = leggiIntero("Numero massimo partecipanti: ");

        controller.impostaParametri(nomeTipoVisita, puntoIncontro, dataInizio, dataFine, inputGiorni, oraInizio, durataMinuti, 
                    bigliettoRichiesto, numeroMinimoPartecipanti, numeroMassimoPartecipanti);
    }

    private String leggiStringa(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private int leggiIntero(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero intero valido.");
            }
        }
    }
}
/*

    private void inizializzaParametri() {
        @SuppressWarnings("resource")
        Scanner in = new Scanner(System.in);
        System.out.println("\n== Impostazione parametri per il tipo di visita: " + titolo + " ==");

        // --- Punto d'incontro ---
        System.out.print("Punto d'incontro: ");
        setPuntoIncontro(in.nextLine().trim());

        while (true) {
            try {
                System.out.print("Data inizio (YYYY-MM-DD): ");
                setDataInizioProgrammazione(LocalDate.parse(in.nextLine().trim()));
                System.out.print("Data fine (YYYY-MM-DD): ");
                setDataFineProgrammazione(LocalDate.parse(in.nextLine().trim()));
                Preconditions.check(!dataFineProgrammazione.isBefore(dataInizioProgrammazione), "Data fine prima della data inizio");
                break;
            } catch (Exception e) {
                System.out.println("Formato non valido, riprova");
            }
        }

        // --- Giorni della settimana ---
        System.out.println("Inserisci i giorni della settimana in cui la visita è disponibile.");
        System.out.println("Scrivi i nomi separati da virgola (es: LUNEDI,MARTEDI,VENERDI).");
        System.out.println("Giorni validi: LUNEDI, MARTEDI, MERCOLEDI, GIOVEDI, VENERDI, SABATO, DOMENICA");
        System.out.print("Giorni: ");
        String inputGiorni = in.nextLine().trim();

        // Mappa ITA → ENGLISH per compatibilità con DayOfWeek
        Map<String, DayOfWeek> traduzione = Map.ofEntries(
            Map.entry("LUNEDI", DayOfWeek.MONDAY),
            Map.entry("MARTEDI", DayOfWeek.TUESDAY),
            Map.entry("MERCOLEDI", DayOfWeek.WEDNESDAY),
            Map.entry("GIOVEDI", DayOfWeek.THURSDAY),
            Map.entry("VENERDI", DayOfWeek.FRIDAY),
            Map.entry("SABATO", DayOfWeek.SATURDAY),
            Map.entry("DOMENICA", DayOfWeek.SUNDAY)
        );

        Set<DayOfWeek> giorni = EnumSet.noneOf(DayOfWeek.class);
        if (!inputGiorni.isEmpty()) {
            for (String s : inputGiorni.split(",")) {
                try {
                    String nome = s.trim().toUpperCase();
                    DayOfWeek giorno = traduzione.get(nome);
                    giorni.add(giorno);
                } catch (IllegalArgumentException e) {
                    System.out.println("Giorno non valido ignorato: " + s.trim());
                }
            }
        }
        if (giorni.isEmpty()) {
            System.out.println("Nessun giorno valido inserito, verrà impostato LUNEDI come predefinito.");
            giorni.add(DayOfWeek.MONDAY);
        }
        setGiorniSettimana(giorni);

        // --- Ora di inizio ---
        while(true) {
            try {
                System.out.print("Ora di inizio (HH:MM): ");
                setOraInizio(LocalTime.parse(in.nextLine().trim()));
                break;
            } catch (Exception e) {
                System.out.println("Formato non valido, riprova.");
            }
        }

        // --- Durata ---
        while (true) {
            try {
                System.out.print("Durata (minuti): ");
                setDurataMinuti(Integer.parseInt(in.nextLine().trim()));
                if (durataMinuti > 0) break;
            } catch (Exception e) {
                System.out.println("Inserisci un numero positivo.");
            }
        }

        //--- Biglietto ---
        System.out.print("È richiesto un biglietto d'ingresso? (s/n): ");
        String risposta = in.nextLine().trim();
        setBigliettoRichiesto(risposta.equalsIgnoreCase("s"));

        // --- Numero minimo partecipanti ---
        int min;
        while (true) {
            System.out.print("Numero minimo partecipanti: ");
            try {
                min = Integer.parseInt(in.nextLine().trim());
                if (min > 0) break;
            } catch (NumberFormatException e) {
                // ignora
            }
            System.out.println("Inserisci un numero intero positivo.");
        }
        setNumeroMinimoPartecipanti(min);

        // --- Numero massimo partecipanti ---
        int max;
        while (true) {
            System.out.print("Numero massimo partecipanti: ");
            try {
                max = Integer.parseInt(in.nextLine().trim());
                if (max >= min) break;
            } catch (NumberFormatException e) {
                // ignora
            }
            System.out.println("Inserisci un numero intero maggiore o uguale al minimo.");
        }
        setNumeroMassimoPartecipanti(max);

        System.out.println("Parametri impostati correttamente.\n");
    }
*/
