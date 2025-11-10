package it.unibs.visite.service;

import it.unibs.visite.model.*;
import java.time.*;
import java.util.*;

public class VisitBatchService {
    private final ConfigService config;
    public VisitBatchService(ConfigService config){ this.config = config; }

    public void run(LocalDate today){
        for(Visita v : new ArrayList<>(config.getSnapshot().getVisite())){
            // [T-3] chiusura iscrizioni e avanzamento stati
            if((v.getStato()==StatoVisita.PROPOSTA || v.getStato()==StatoVisita.COMPLETA) &&
               !today.isBefore(v.getData().minusDays(3))){
                if(v.getTotalePersone() >= v.getNumeroMinimoPartecipanti()) v.setStato(StatoVisita.CONFERMATA);
                else v.setStato(StatoVisita.CANCELLATA);
            }
            // Giorno di svolgimento (incluso)
            if(!today.isBefore(v.getData())){
                if(v.getStato()==StatoVisita.CONFERMATA) {
                    v.setStato(StatoVisita.EFFETTUATA);
                    config.getSnapshot().addArchivio(v);
                } else if(v.getStato()==StatoVisita.CANCELLATA){
                    // Non archiviare
                }
            }
        }
        config.save();
    }
}
