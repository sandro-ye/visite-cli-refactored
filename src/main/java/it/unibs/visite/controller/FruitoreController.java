package it.unibs.visite.controller;

import java.util.List;

import it.unibs.visite.model.Iscrizione;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Visita;
import it.unibs.visite.service.FruitoreService;

public class FruitoreController {
    private final FruitoreService fruitoreService;

    public FruitoreController(FruitoreService fruitoreService) {
        this.fruitoreService = fruitoreService;
    }

    public List<Visita> visualizzaVisiteDisponibili() {
        return fruitoreService.getVisiteDisponibili();
    }

    public TipoVisita getTipoVisitaById(String idTipoVisita) {
        return fruitoreService.getTipoVisitaById(idTipoVisita);
    }

    public void iscriviAVisita(String usernameFruitore, String codiceVisita, int numeroPersone) {
        fruitoreService.iscriviAVisita(usernameFruitore, codiceVisita, numeroPersone);
    }

    public List<Iscrizione> getIscrizioniDi(String usernameFruitore) {
        return fruitoreService.getIscrizioniDi(usernameFruitore);  
    }

    public void disdiciIscrizione(String usernameFruitore, String codiceIscrizione) {
        fruitoreService.disdiciIscrizione(usernameFruitore, codiceIscrizione);
    }

    public int getMaxPersonePerIscrizione() {
        return fruitoreService.getMaxPersonePerIscrizione();
    }

    public Visita getVisitaByCodiceIscrizione(String usernameFruitore, String codiceIscrizione) {
        return fruitoreService.getVisitaByCodiceIscrizione(usernameFruitore, codiceIscrizione);
    }
}
