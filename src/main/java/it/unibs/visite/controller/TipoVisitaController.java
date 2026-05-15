package it.unibs.visite.controller;

import it.unibs.visite.service.TipoVisitaService;

public class TipoVisitaController {
    private final TipoVisitaService tipoVisitaService;

    public TipoVisitaController() {
        this.tipoVisitaService = new TipoVisitaService();
    }

    public void impostaParametri(String nomeTipoVisita, String puntoIncontro, 
                    String dataInizio, String dataFine, String inputGiorni, String oraInizio, int durataMinuti, 
                    boolean bigliettoRichiesto, int numeroMinimoPartecipanti, int numeroMassimoPartecipanti) {
        tipoVisitaService.impostaParametri(nomeTipoVisita, puntoIncontro, dataInizio, dataFine, inputGiorni, 
                            oraInizio, durataMinuti, bigliettoRichiesto, numeroMinimoPartecipanti, numeroMassimoPartecipanti);
    }
}
