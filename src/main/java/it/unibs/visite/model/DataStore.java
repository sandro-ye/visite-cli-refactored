package it.unibs.visite.model;

import java.io.Serializable;
import java.time.*;
import java.util.*;
import java.util.stream.*;

/**
 * DataStore principale dell'applicazione.
 * - Contiene tutti i dati persistenti relativi a luoghi, tipi di visita,
 * volontari, preclusioni e visite.
 */

public class DataStore implements Serializable {
    private  ParametriSistema parametri = new ParametriSistema();
    private final Map<String, Luogo> luoghi = new LinkedHashMap<>();
    private final Map<String, TipoVisita> tipiVisita = new LinkedHashMap<>();
    private final Map<String, Volontario> volontari = new LinkedHashMap<>();

    private final Map<YearMonth, Set<LocalDate>> preclusioni = new LinkedHashMap<>();
    private final Map<String, Visita> visite = new LinkedHashMap<>(); 

    /*
     * Solo per Testing, ricorda di aggiungere final a ParametriSistema parametri dopo il test
     */
    public void setParametriSistema(ParametriSistema p) {this.parametri = p;}

    // nuove strutture
    private final Map<YearMonth, Set<LocalDate>> preclusioniPerMese = new HashMap<>();
    private final Map<YearMonth, Map<String, VolunteerAvailability>> disponibilitaPerMese = new HashMap<>();
    private final Map<YearMonth, List<Visita>> visitePropostePerMese = new HashMap<>();
    private final List<Visita> archivioStorico = new ArrayList<>();
    private AppPhase faseCorrente = AppPhase.RACCOLTA_DISPONIBILITA;
    
    public AppPhase getFaseCorrente() { return faseCorrente; }
    public void setFaseCorrente(AppPhase fase) { this.faseCorrente = fase; }

    public Map<YearMonth, Map<String, VolunteerAvailability>> getDisponibilitaPerMese() {
        return disponibilitaPerMese;
    }

    // -- fruitori -- versione 4
    private final Map<String, Fruitore> fruitori = new HashMap<>();
    
    // ---- preclusioni ----
    public void setPreclusioni(YearMonth ym, Set<LocalDate> dates) {
        preclusioniPerMese.put(ym, new HashSet<>(dates));
    }
    public Set<LocalDate> getPreclusioni(YearMonth ym) {
        return preclusioniPerMese.getOrDefault(ym, Collections.emptySet());
    }
    // ---- disponibilità ----
    public void setDisponibilita(YearMonth ym, String nickname, Set<LocalDate> dates) {
        if (ym == null || nickname == null) throw new IllegalArgumentException("Parametri null");
        disponibilitaPerMese.computeIfAbsent(ym, k -> new HashMap<>());

        // costruttore che accetta già le date
        VolunteerAvailability d = new VolunteerAvailability(nickname, ym, dates);

        disponibilitaPerMese.get(ym).put(nickname, d);
    }

    public Optional<VolunteerAvailability> getDisponibilita(YearMonth ym, String nickname) {
        Map<String, VolunteerAvailability> m = disponibilitaPerMese.get(ym);
        if (m == null) {
            return Optional.empty();
        }

        VolunteerAvailability av = m.get(nickname);
        if (av == null || av.getDates() == null || av.getDates().isEmpty()) {
            return Optional.empty(); // Nessuna disponibilità reale registrata
        }

        return Optional.of(av);

    }

    public void clearDisponibilita(YearMonth ym) { disponibilitaPerMese.remove(ym); }


    public ParametriSistema getParametri() { return parametri; }
    public Collection<Luogo> getLuoghi() { return luoghi.values(); }
    public Collection<TipoVisita> getTipiVisita() { return tipiVisita.values(); }
    public Collection<Volontario> getVolontari() { return volontari.values(); }

    public Luogo getLuogo(String id) { return luoghi.get(id); }
    public TipoVisita getTipoVisita(String id) { return tipiVisita.get(id); }
    public Volontario getVolontario(String nick) { return volontari.get(nick); }

    public void addLuogo(Luogo l) { luoghi.put(l.getId(), l); }
    public void addTipoVisita(TipoVisita t) { tipiVisita.put(t.getId(), t); }
    public void addVolontario(Volontario v) {
        if (volontari.containsKey(v.getNickname())) throw new IllegalArgumentException("Nickname già presente");
        volontari.put(v.getNickname(), v);
    }

    public void addPreclusione(YearMonth ym, LocalDate data) {
        if(data == null) throw new IllegalArgumentException("data nulla");
        preclusioni.computeIfAbsent(ym, e -> new LinkedHashSet<>()).add(data);
    }

    public Set<LocalDate> getPreclusioniFor(YearMonth ym) {
        return Collections.unmodifiableSet(preclusioni.getOrDefault(ym, Collections.emptySet()));
    }

    public Map<YearMonth, Set<LocalDate>> getAllPreclusioni() {
        return Collections.unmodifiableMap(preclusioni);
    }

    public void addVisita(Visita v) {
        visite.put(v.getId(), v);
    }

    public Collection<Visita> getVisite() {
        return Collections.unmodifiableCollection(visite.values());
    }

    public Visita getVisita(String id) {
        return visite.get(id); 
    }

    public boolean volontarioEsiste(String nickname) {
        if(nickname == null || nickname.isBlank()) return false;
        return volontari.containsKey(nickname);
    }


    // ---- visite ----
    public void setVisiteProposte(YearMonth ym, List<Visita> v) { visitePropostePerMese.put(ym, v); }
    public List<Visita> getVisiteProposte(YearMonth ym) {
        return Collections.unmodifiableList(visitePropostePerMese.getOrDefault(ym, Collections.emptyList()));
    }

    public void addArchivio(Visita v) { archivioStorico.add(v); }
    public List<Visita> getArchivioStorico() { return Collections.unmodifiableList(archivioStorico); }

    // ---- rimozioni con chiusura transitiva ----
    public void rimuoviVolontario(String nickname) {
        Objects.requireNonNull(nickname, "Il nickname del volontario non può essere null");

        // Rimuovi il volontario da tutti i tipi di visita in cui compare
        for (TipoVisita tv : new ArrayList<>(tipiVisita.values())) {
            if (tv.getVolontariNicknames().contains(nickname)) {
                // Usa il metodo dedicato (non modificare la collezione immutabile)
                tv.removeVolontario(nickname);

                // Se dopo la rimozione non restano volontari -> elimina il tipo di visita
                // Se il luogo associato al tipo di visita rimosso non ha altri tipi di visita -> rimuovi il luogo
                if (tv.getVolontariNicknames().isEmpty()) {
                    rimuoviTipoVisita(tv.getId());
                }
            }
        }

        // Infine, rimuovi il volontario dal DataStore
        volontari.remove(nickname);
    }

    public void rimuoviTipoVisita(String id) {
        TipoVisita t = tipiVisita.remove(id);
        if (t == null) return;

        // Rimuovi il tipo di visita dal luogo associato
        Luogo l = luoghi.get(t.getLuogoId());
        if (l != null) {
            l.getTipiVisitaIds().remove(id);
            if (l.getTipiVisitaIds().isEmpty()) {
                luoghi.remove(l.getId());
                System.out.println("Rimosso anche il luogo orfano: " + l.getNome());
            }
        }

        // Rimuovi il tipo da ogni volontario associato
        Set<String> volontariAssociati = new HashSet<>(t.getVolontariNicknames());
        for (String nick : volontariAssociati) {
            t.removeVolontario(nick);
        }

            // Rimuovi volontari che non hanno più tipi di visita
            volontari.values().removeIf(vol -> getTipiVisitaPerVolontario(vol.getNickname()).isEmpty());
            System.out.println("Tipo di visita '" + t.getTitolo() + "' rimosso con successo.");
    }

    public void rimuoviLuogo(String id) {
        Luogo l = luoghi.remove(id);
        if (l == null) return;

        // Rimuovi tutti i tipi di visita associati al luogo
        for (String tipoId : new HashSet<>(l.getTipiVisitaIds())) {
            rimuoviTipoVisita(tipoId);
        }

        // Dopo la rimozione dei tipi, elimina i volontari orfani
        volontari.values().removeIf(v -> getTipiVisitaPerVolontario(v.getNickname()).isEmpty());
    }

    // ---- helper ----
    public boolean volontarioDisponibile(String nick, LocalDate data) {
        YearMonth ym = YearMonth.from(data);
        if (getPreclusioni(ym).contains(data)) return false;
        return getDisponibilita(ym, nick).map(d -> d.isDisponibile(data)).orElse(false);
    }

    public List<TipoVisita> tipiProgrammabili(LocalDate data) {
        DayOfWeek dow = data.getDayOfWeek(); // non usare int
        return tipiVisita.values().stream()
                .filter(t -> t.getGiorniSettimana().contains(dow))
                .collect(Collectors.toList());
    }

    private Set<String> getTipiVisitaPerVolontario(String nickname) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, TipoVisita> entry : tipiVisita.entrySet()) {
            TipoVisita tv = entry.getValue();
            if (tv != null && tv.getVolontariNicknames().contains(nickname)) {
                result.add(entry.getKey()); // o tv.getId() se esiste
            }
        }
        return result;
    }

    // ====== aggiunte versione 4 ========
    public void addFruitore(Fruitore f) {
        fruitori.put(f.getUsername(), f);
    }

    public Fruitore getFruitore(String username) {
        return fruitori.get(username);
    }

    public boolean existsFruitore(String username) {
        return fruitori.containsKey(username);
    }

    public Collection<Fruitore> getAllFruitori() {
        return List.copyOf(fruitori.values());
    }

    public Visita getVisitaByCodiceIscrizione(String codice) {
        return visite.values().stream()
            .filter(v -> v.getIscrizioni().stream().anyMatch(i -> i.getCodice().equals(codice)))
            .findFirst()
            .orElse(null);
    }
}


