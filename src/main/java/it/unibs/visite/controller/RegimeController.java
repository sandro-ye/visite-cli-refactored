package it.unibs.visite.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import it.unibs.visite.model.Luogo;
import it.unibs.visite.model.StatoVisita;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Visita;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.service.RegimeService;

public class RegimeController {
    private final RegimeService regimeService;

    public RegimeController() {
        this.regimeService = new RegimeService();
    }

    public void aggiungiPreclusione(LocalDate data) {
        regimeService.aggiungiPreclusione(data);
    }

    public List<LocalDate> getPreclusioniPer(YearMonth mese) {
        return regimeService.getPreclusioniPer(mese);
    }

    public void setMaxPersonePerIscrizione(int max) {
        regimeService.setMaxPersonePerIscrizione(max);
    }

    public List<Volontario> getElencoVolontari() {
        return regimeService.getElencoVolontari();
    }

    public List<TipoVisita> getTipiVisitaDi(Volontario volontario) {
        return regimeService.getTipiVisitaDi(volontario);
    }

    public List<Luogo> getElencoLuoghi() {
        return regimeService.getElencoLuoghi();
    }

    public List<TipoVisita> tipiPerLuogo(String luogoId) {
        return regimeService.getTipiVisitaPerLuogo(luogoId);
    }

    public List<Visita> getVisitePerStato(StatoVisita stato) {
        return regimeService.getVisitePerStato(stato);
    }
}
