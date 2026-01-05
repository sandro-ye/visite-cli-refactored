package it.unibs.visite.controller;

import it.unibs.visite.service.InitWizardService;

public class InitWizardController {
    private final InitWizardService service;

    public InitWizardController(InitWizardService service) {
        this.service = service;
    }

    public void avvia(String ambito, int maxPersonePerIscrizione) {
        service.esegui(ambito, maxPersonePerIscrizione);
    }
}
