package it.unibs.visite.service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import it.unibs.visite.model.*;
import it.unibs.visite.core.Preconditions;

public class FruitoreService {
    private final ConfigService config;
    private final Fruitore fruitore;

    public FruitoreService(ConfigService config, Fruitore fruitore){
        this.config = config; this.fruitore = fruitore;
        // registra il fruitore nel sistema (persistenza su file)
        config.getSnapshot().addFruitore(fruitore);
        config.save();
    }

    public Map<StatoVisita, List<Visita>> visitePerStato(){
        return config.getSnapshot().getVisite().stream()
            .collect(Collectors.groupingBy(Visita::getStato, () -> new java.util.EnumMap<>(StatoVisita.class), Collectors.toList()));
    }

    public List<Visita> visiteDisponibili(){
        return visitePerStato().getOrDefault(StatoVisita.PROPOSTA, java.util.List.of());
    }

    // [V4] iscrizione con vincoli + codice prenotazione univoco
    public Iscrizione iscriviFruitoreAllaVisita(String idVisita, int numPersone){
        Visita v = config.getSnapshot().getVisita(idVisita);
        Preconditions.notNull(v, "Visita inesistente");

        int maxSingolaIscr = config.getSnapshot().getParametri().getMaxPersonePerIscrizione();
        Preconditions.check(numPersone >= 1 && numPersone <= maxSingolaIscr, "numPersone fuori range");

        Iscrizione is = v.aggiungiIscrizione(fruitore, numPersone);
        fruitore.addIscrizione(is);
        config.save();
        return is;
    }

    // [V4] disdetta: solo PROPOSTA e prima di T-3, e solo il proprietario del codice
    public void disdiciIscrizione(String codice){
        Visita v = config.getSnapshot().getVisitaByCodiceIscrizione(codice);
        Preconditions.notNull(v, "Iscrizione inesistente");

        LocalDate today = LocalDate.now();
        Preconditions.check(v.getStato()==StatoVisita.PROPOSTA, "Disdetta ammessa solo su visite proposte");
        Preconditions.check(!today.isAfter(v.getData().minusDays(3)), "Disdetta oltre la chiusura iscrizioni");

        Iscrizione i = fruitore.getIscrizione(codice);
        Preconditions.notNull(i, "Disdetta non autorizzata per questo utente");

        v.removeIscrizione(codice);
        fruitore.removeIscrizione(codice);
        config.save();
    }

    public List<Iscrizione> mieIscrizioni(){
        return new java.util.ArrayList<>(fruitore.getMieIscrizioni());
    }
}
