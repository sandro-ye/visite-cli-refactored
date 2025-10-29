package it.unibs.visite.model;

import java.io.Serializable;
import java.time.*;
import java.util.*;

public class DataStore implements Serializable {
    private final ParametriSistema parametri = new ParametriSistema();
    private final Map<String, Luogo> luoghi = new LinkedHashMap<>();
    private final Map<String, TipoVisita> tipiVisita = new LinkedHashMap<>();
    private final Map<String, Volontario> volontari = new LinkedHashMap<>();

    private final Map<YearMonth, Set<LocalDate>> preclusioni = new LinkedHashMap<>();
    private final Map<String, Visita> visite = new LinkedHashMap<>(); 

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
}
