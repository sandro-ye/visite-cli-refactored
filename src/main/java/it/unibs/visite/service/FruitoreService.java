package it.unibs.visite.service;

import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import it.unibs.visite.model.*;
import it.unibs.visite.persistence.FileRepositoryPersistence;
import it.unibs.visite.repository.*;
import it.unibs.visite.repository.memory.*;
import it.unibs.visite.core.Preconditions;

/**
 * Servizio per la gestione delle operazioni relative a un fruitore specifico.
 * - restituisce visite a seconda dello stato della visita stessa 
 * - restituisce le visite disponibili per l'iscrizione
 * - iscrive un fruitore a una visita con vincoli su numero persone
 * - gestisce la disdetta di un'iscrizione con vincoli su stato visita e tempistiche
 * - restituisce le iscrizioni del fruitore
 */

public class FruitoreService {
    private final ParametriSistemaRepository parametriRepo;
    private final VisitaRepository visitaRepo;
    private final TipoVisitaRepository tipoVisitaRepo;
    private final FruitoreRepository fruitoreRepo;

    public FruitoreService() {
        this.visitaRepo = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "visite-repo.ser"),
            InMemoryVisitaRepository::new);
        this.tipoVisitaRepo = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "tipi-visita-repo.ser"),
            InMemoryTipoVisitaRepository::new);
        this.parametriRepo = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "parametri-sistema.ser"),
            InMemoryParametriSistemaRepository::new);
        this.fruitoreRepo = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "fruitori.ser"),
            InMemoryFruitoreRepository::new);
    }

    public List<Visita> getVisiteDisponibili() {
        return visitaRepo.findAll().stream()
            .filter(v -> v.getStato() == StatoVisita.PROPOSTA)
            .collect(Collectors.toList());
    }

    public TipoVisita getTipoVisitaById(String id) {
        return tipoVisitaRepo.findById(id).orElse(null);
    }

    public void iscriviAVisita(String usernameFruitore, String codiceVisita, int numeroPersone) {
        int maxSingolaIscr = parametriRepo.load().getMaxPersonePerIscrizione();
        Preconditions.check(numeroPersone >= 1 && numeroPersone <= maxSingolaIscr, "numPersone fuori range");

        Visita visita = visitaRepo.find(codiceVisita).orElseThrow(() ->
            new IllegalArgumentException("Visita non trovata: " + codiceVisita));
        TipoVisita tipoVisita = tipoVisitaRepo.findById(visita.getTipoVisitaId()).orElseThrow(() ->
            new IllegalArgumentException("Tipo visita non trovato: " + visita.getTipoVisitaId()));

        if(visita.getTotalePersone() + numeroPersone > tipoVisita.getNumeroMassimoPartecipanti()) {
            throw new IllegalStateException("Superamento numero massimo partecipanti");
        }

        Fruitore fruitore = fruitoreRepo.findByUsername(usernameFruitore).orElseThrow(() ->
            new IllegalArgumentException("Fruitore non trovato: " + usernameFruitore));

        Iscrizione iscrizione = visita.aggiungiIscrizione(fruitore, numeroPersone);
        fruitore.addIscrizione(iscrizione);
    }

    public List<Iscrizione> getIscrizioniDi(String usernameFruitore) {
        Fruitore fruitore = fruitoreRepo.findByUsername(usernameFruitore).orElseThrow(() ->
            new IllegalArgumentException("Fruitore non trovato: " + usernameFruitore));
        return fruitore.getMieIscrizioni().stream().collect(Collectors.toList());
    }

    public void disdiciIscrizione(String usernameFruitore, String codiceIscrizione) {
        Fruitore fruitore = fruitoreRepo.findByUsername(usernameFruitore).orElseThrow(() ->
            new IllegalArgumentException("Fruitore non trovato: " + usernameFruitore));
        Iscrizione iscrizione = fruitore.getIscrizione(codiceIscrizione);
        if(iscrizione == null) {
            throw new IllegalArgumentException("Iscrizione non trovata per questo fruitore: " + codiceIscrizione);
        }

        Visita visita = visitaRepo.find(iscrizione.getCodiceVisitaAssociato()).orElseThrow(() ->
            new IllegalArgumentException("Visita non trovata per l'iscrizione: " + iscrizione.getCodiceVisitaAssociato()));

        LocalDate today = LocalDate.now();
        if(visita.getStato() != StatoVisita.PROPOSTA) {
            throw new IllegalStateException("Disdetta ammessa solo su visite proposte");
        }
        if(today.isAfter(visita.getData().minusDays(3))) {
            throw new IllegalStateException("Disdetta oltre la chiusura iscrizioni");
        }

        visita.removeIscrizione(codiceIscrizione);
        fruitore.removeIscrizione(codiceIscrizione);
    }

    public int getMaxPersonePerIscrizione() {
        return parametriRepo.load().getMaxPersonePerIscrizione();
    }

    public Visita getVisitaByCodiceIscrizione(String usernameFruitore,String codiceIscrizione) {
        Fruitore fruitore = fruitoreRepo.findByUsername(usernameFruitore).orElseThrow(() ->
            new IllegalArgumentException("Fruitore non trovato: " + usernameFruitore));
        Iscrizione iscrizione = fruitore.getIscrizione(codiceIscrizione);
        if(iscrizione == null) {
            throw new IllegalArgumentException("Iscrizione non trovata per questo fruitore: " + codiceIscrizione);
        }

        return visitaRepo.find(iscrizione.getCodiceVisitaAssociato()).orElseThrow(() ->
            new IllegalArgumentException("Visita non trovata per l'iscrizione: " + iscrizione.getCodiceVisitaAssociato()));
    }

    /*
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
    */
}
