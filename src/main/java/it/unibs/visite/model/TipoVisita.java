package it.unibs.visite.model;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import it.unibs.visite.core.Preconditions;

public class TipoVisita implements Serializable {
    private final String id;
    private final String luogoId;
    private String titolo;
    private String descrizione;
    private String puntoIncontro;
    private LocalDate dataInizioProgrammazione;
    private LocalDate dataFineProgrammazione; 
    private final Set<DayOfWeek>  giorniSettimana = new HashSet<>();
    private LocalTime oraInizio;
    private int durataMinuti;
    private boolean bigliettoRichiesto;
    private int numeroMinimoPartecipanti;
    private int numeroMassimoPartecipanti;
    private final Set<String> volontariNicknames = new HashSet<>();

    // Costruttore con inizializzazione completa dei parametri
    public TipoVisita(String luogoId, String titolo, String descrizione) {
        this.id = UUID.randomUUID().toString();
        this.luogoId = Objects.requireNonNull(luogoId);
        this.titolo = Objects.requireNonNull(titolo);
        this.descrizione = descrizione == null ? "" : descrizione;
        inizializzaParametri();
    }

    // Costruttore leggero per test (salta inizializzazione parametri non essenziali)
    public TipoVisita(String luogoId, String titolo, String descrizione, boolean skipInput) {
        this.id = UUID.randomUUID().toString();
        this.luogoId = Objects.requireNonNull(luogoId);
        this.titolo = Objects.requireNonNull(titolo);
        this.descrizione = descrizione == null ? "" : descrizione;
        if (!skipInput) inizializzaParametri();
    }

    // Getters e Setters
    public String getId() { return id; }
    public String getLuogoId() { return luogoId; }
    public String getTitolo() { return titolo; }
    public String getDescrizione() { return descrizione; }
    public String getPuntoIncontro() { return puntoIncontro; }
    public LocalDate getDataInizioProgrammazione() { return dataInizioProgrammazione; }
    public LocalDate getDataFineProgrammazione() { return dataFineProgrammazione; }
    public Set<DayOfWeek> getGiorniSettimana() { return giorniSettimana; }
    public LocalTime getOraInizio() { return oraInizio; }
    public int getDurataMinuti() { return durataMinuti; }
    public boolean getBigliettoRichiesto() { return bigliettoRichiesto; }
    public int getNumeroMinimoPartecipanti() { return this.numeroMinimoPartecipanti; }
    public int getNumeroMassimoPartecipanti() { return this.numeroMassimoPartecipanti; }
    public Set<String> getVolontariNicknames() { return Collections.unmodifiableSet(volontariNicknames); }

    public void setPuntoIncontro(String puntoIncontro) { this.puntoIncontro = puntoIncontro;}
    public void setDataInizioProgrammazione(LocalDate dataInizioProgrammazione) { this.dataInizioProgrammazione = dataInizioProgrammazione;}
    public void setDataFineProgrammazione(LocalDate dataFineProgrammazione) { this.dataFineProgrammazione = dataFineProgrammazione; }
    public void setGiorniSettimana (Set<DayOfWeek> giorni) {
        if (giorni != null) this.giorniSettimana.addAll(giorni);
        else giorniSettimana.add(DayOfWeek.MONDAY);
    }
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }
    public void setDurataMinuti(int durataMinuti) {this.durataMinuti = durataMinuti; }
    public void setBigliettoRichiesto(boolean bigliettoRichiesto) { this.bigliettoRichiesto = bigliettoRichiesto;}
    public void setNumeroMinimoPartecipanti(int num) { this.numeroMinimoPartecipanti = num; }
    public void setNumeroMassimoPartecipanti(int num) { this.numeroMassimoPartecipanti = num; }


    public void addVolontario(String nickname, DataStore ds) {
        Preconditions.notBlank(nickname, "nickname volontario obbligatorio");
        Preconditions.notNull(ds, "DataStore non puo essere null");
        Preconditions.check(ds.volontarioEsiste(nickname), "Volontario specificato non esiste");

        volontariNicknames.add(nickname);

        assert volontariNicknames.size() > 0 : "Invariante violato: ogni TipoVisita deve avere almeno un volontario";
    }

    public void removeVolontario(String nickname) {
        Preconditions.notBlank(nickname, "nickname volontario obbligatorio");
        volontariNicknames.remove(nickname);
    } 

    public void ensureInvariants() {
        Preconditions.notBlank(luogoId, "luogoId obbligatorio");
        Preconditions.notBlank(titolo, "titolo obbligatorio");
        Preconditions.notBlank(descrizione, "descrizione obbligatoria");
        Preconditions.notNull(dataInizioProgrammazione, "dataInizio obbligatoria");
        Preconditions.notNull(dataFineProgrammazione, "dataFine obbligatoria");
        Preconditions.check(!dataFineProgrammazione.isBefore(dataInizioProgrammazione), "dataFine deve essere dopo dataInizio");
        Preconditions.check(!volontariNicknames.isEmpty(), "invariante violato: ogni TipoVisita deve avere almeno un volontario");
        Preconditions.check(numeroMassimoPartecipanti >= numeroMinimoPartecipanti, "Numero massimo di partecipanti deve essere maggiore del numero minimo");
    }

    // Metodo per completare il costruttore
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

}
