package it.unibs.visite.controller;

import java.time.*;
import java.util.*;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.service.PlannerService;

public class GiornoSediciController {
    private final PlannerService plannerService;
    
    public GiornoSediciController() {
        this.plannerService = new PlannerService();
    }

    public List<LocalDate> giorniNonPreclusiIn(YearMonth mese) {
        return plannerService.giorniNonPreclusiIn(mese);
    }

    public List<Volontario> volontariDisponibiliInDataPerTipo(LocalDate data, TipoVisita tipo) {
        return plannerService.volontariDisponibiliInDataPerTipo(data, tipo);
    }

    public Map<LocalDate, List<TipoVisita>> visiteProgrammabiliPerData() {
        return plannerService.visiteProgrammabiliPerData();
    }

    public void salvaAssegnazioneVolontario(String volontario, TipoVisita tipo, LocalDate data) {
        plannerService.salvaAssegnazioneVolontario(volontario, tipo, data);
    }

    public List<Volontario> getTuttiVolontari() {
        return plannerService.getTuttiVolontari();
    }

    public void aggiungiVolontario(String nickname) {
        plannerService.aggiungiVolontario(nickname);
    }

    public void rimuoviVolontario(String nickname) {
        plannerService.rimuoviVolontario(nickname);
    }

    public List<Luogo> getTuttiLuoghi() {
        return plannerService.getTuttiLuoghi();
    }

    public void aggiungiLuogo(String nome, String descrizione) {
        plannerService.aggiungiLuogo(nome, descrizione);
    }

    public void rimuoviLuogo(String nome) {
        plannerService.rimuoviLuogo(nome);
    }

    public List<TipoVisita> getTuttiTipiVisita() {
        return plannerService.getTuttiTipiVisita();
    }

    public void aggiungiTipoVisita(String luogo, String titolo, String descrizione) {
        plannerService.aggiungiTipoVisita(luogo, titolo, descrizione);
    }

    public void rimuoviTipoVisita(String titolo) {
        plannerService.rimuoviTipoVisita(titolo);
    }

    public void aggiungiPreclusione(LocalDate data) {
        plannerService.aggiungiPreclusione(data);
    }

    public void riapriRaccoltaDisponibilita() {
        plannerService.riapriRaccoltaDisponibilita();
    }
}
