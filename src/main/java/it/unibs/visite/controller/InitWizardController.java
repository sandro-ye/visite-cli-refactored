package it.unibs.visite.controller;

import it.unibs.visite.service.InitWizardService;
import java.util.List;

//completare

public class InitWizardController {
    private final InitWizardService service;

    public InitWizardController(InitWizardService service) {
        this.service = service;
    }

    public void avvia(String ambito, int maxPersonePerIscrizione) {
        service.esegui(ambito, maxPersonePerIscrizione);
    }

    public boolean isInitialized() {
        return service.isInitialized();
    }

    public void addLuogo(String nome, String descrizione) {
        service.addLuogo(nome, descrizione);
    }

    public boolean hasLuoghi() {
        return service.hasLuoghi();
    }

    public boolean hasVolontari() {
        return service.hasVolontari();
    }

    public boolean hasTipiVisita() {
        return service.hasTipiVisita();
    }

    public void aggiungiTipoVisitaALuogo(String nomeLuogo, String nomeTipoVisita, String descrizione) {
        service.aggiungiTipoVisitaALuogo(nomeLuogo, nomeTipoVisita, descrizione);
    }

    public List<String> getAllLuoghiNames() {
        return service.getAllLuoghiNames();
    }

    public List<String> getAllVolontariNicknames() {
        return service.getAllVolontariNicknames();
    }

    public List<String> getAllTipiVisitaNames() {
        return service.getAllTipiVisitaNames();
    }

    public void aggiungiVolontarioATipoVisita(String nickname, String nomeTipoVisita) {
        service.aggiungiVolontarioATipoVisita(nickname, nomeTipoVisita);
    }

    public void validateInvariants() {
        service.validateInvariants();
    }
}
