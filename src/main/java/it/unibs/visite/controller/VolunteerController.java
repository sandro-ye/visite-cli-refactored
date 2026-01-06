package it.unibs.visite.controller;

import it.unibs.visite.model.DisponibilitaVolontario;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.service.DisponibilitaService;
import java.time.LocalDate;
import java.util.List;

public class VolunteerController {
    private final DisponibilitaService disponibilitaService;

    public VolunteerController(DisponibilitaService disponibilitaService) {
        this.disponibilitaService = disponibilitaService;
    }

    public List<DisponibilitaVolontario> visualizzaDisponibilita(String nickname) {
        return disponibilitaService.getDisponibilitaDi(nickname);
    }

    public void revocaDisponibilita(String nickname, LocalDate data) {
        disponibilitaService.rimuoviDisponibilita(nickname, data);
    }

    public void inserisciDisponibilita(String nickname, LocalDate data) {
        disponibilitaService.aggiungiDisponibilita(nickname, data);
    }

    public List<TipoVisita> visualizzaTipiVisita(String nickname) {
        return disponibilitaService.getAllTipiVisita(nickname);
    }
}
