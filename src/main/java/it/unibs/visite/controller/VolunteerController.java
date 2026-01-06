package it.unibs.visite.controller;

import it.unibs.visite.service.VolunteerService;

public class VolunteerController {
    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    public void visualizzaDisponibilita() {
        // Implementazione del metodo per visualizzare le disponibilità del volontario
    }

    public void revocaDisponibilita() {
        // Implementazione del metodo per revocare le disponibilità del volontario
    }

    public void inserisciDisponibilita() {
        // Implementazione del metodo per inserire nuove disponibilità del volontario
    }

    public void visualizzaTipiVisita() {
        // Implementazione del metodo per visualizzare i tipi di visita del volontario
    }
}
