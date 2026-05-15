package it.unibs.visite.service;

import it.unibs.visite.model.*;
import java.time.*;
import java.util.*;
import java.nio.file.Paths;

import it.unibs.visite.repository.VisitaRepository;
import it.unibs.visite.persistence.FileRepositoryPersistence;
import it.unibs.visite.repository.memory.InMemoryArchivioVisite;
import it.unibs.visite.repository.memory.InMemoryVisitaRepository;

// da sistemare

public class VisitBatchService {
    private final VisitaRepository archivioVisiteRepository;
    private final VisitaRepository visiteRepository;

    public VisitBatchService(VisitaRepository archivioVisiteRepository, VisitaRepository visiteRepository) {
        this.archivioVisiteRepository = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "archivio_visite_repository.ser"),
            InMemoryArchivioVisite::new
        );
        this.visiteRepository = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "visite_repository.ser"),
            InMemoryVisitaRepository::new
        );
    }

    public void run(LocalDate today) {
        for (Visita v : new ArrayList<>(visiteRepository.findAll())) {
            // [T-3] chiusura iscrizioni e avanzamento stati
            if ((v.getStato() == StatoVisita.PROPOSTA || v.getStato() == StatoVisita.COMPLETA) &&
                !today.isBefore(v.getData().minusDays(3))) {
                if (v.getTotalePersone() >= v.getNumeroMinimoPartecipanti()) v.setStato(StatoVisita.CONFERMATA);
                else v.setStato(StatoVisita.CANCELLATA);
                visiteRepository.save(v);
                FileRepositoryPersistence.salvaOggetto(visiteRepository, Paths.get("data", "visite_repository.ser"));
            }
            // Giorno di svolgimento (incluso)
            if (!today.isBefore(v.getData())) {
                if (v.getStato() == StatoVisita.CONFERMATA) {
                    v.setStato(StatoVisita.EFFETTUATA);
                    archivioVisiteRepository.save(v);
                    FileRepositoryPersistence.salvaOggetto(archivioVisiteRepository, Paths.get("data", "archivio_visite_repository.ser"));
                    visiteRepository.delete(v.getId());
                    FileRepositoryPersistence.salvaOggetto(visiteRepository, Paths.get("data", "visite_repository.ser"));
                }
                visiteRepository.delete(v.getId());
                FileRepositoryPersistence.salvaOggetto(visiteRepository, Paths.get("data", "visite_repository.ser"));
            }
        }
    }
/* 
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
*/
}
