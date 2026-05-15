package it.unibs.visite.service;

import it.unibs.visite.repository.TipoVisitaRepository;
import it.unibs.visite.persistence.FileRepositoryPersistence;
import it.unibs.visite.repository.memory.InMemoryTipoVisitaRepository;
import it.unibs.visite.model.TipoVisita;
import java.time.*;
import java.util.*;
import java.nio.file.Paths;

public class TipoVisitaService {
    private final TipoVisitaRepository tipoVisitaRepository;

    public TipoVisitaService() {
        this.tipoVisitaRepository = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "tipo_visita_repository.ser"),
            InMemoryTipoVisitaRepository::new
        );
    }

    public void impostaParametri(String nomeTipoVisita, String puntoIncontro, 
                    String dataInizio, String dataFine, String inputGiorni, String oraInizio, int durataMinuti, 
                    boolean bigliettoRichiesto, int numeroMinimoPartecipanti, int numeroMassimoPartecipanti) {
        Optional<TipoVisita> tipoVisita = tipoVisitaRepository.findByTitolo(nomeTipoVisita);
        if (tipoVisita.isPresent()) {
            tipoVisita.get().setPuntoIncontro(puntoIncontro);
            LocalDate dataInizioParsed = LocalDate.parse(dataInizio);
            LocalDate dataFineParsed = LocalDate.parse(dataFine);
            tipoVisita.get().setDataInizioProgrammazione(dataInizioParsed);
            tipoVisita.get().setDataFineProgrammazione(dataFineParsed);
            Set<DayOfWeek> giorniSettimana = parseGiorniSettimana(inputGiorni);
            tipoVisita.get().setGiorniSettimana(giorniSettimana);
            LocalTime oraInizioParsed = LocalTime.parse(oraInizio);
            tipoVisita.get().setOraInizio(oraInizioParsed);
            tipoVisita.get().setDurataMinuti(durataMinuti);
            tipoVisita.get().setBigliettoRichiesto(bigliettoRichiesto);
            tipoVisita.get().setNumeroMinimoPartecipanti(numeroMinimoPartecipanti);
            tipoVisita.get().setNumeroMassimoPartecipanti(numeroMassimoPartecipanti);
            tipoVisitaRepository.save(tipoVisita.get());
            FileRepositoryPersistence.salvaOggetto(tipoVisitaRepository, Paths.get("data", "tipo_visita_repository.ser"));
        } else {
            throw new IllegalArgumentException("TipoVisita con titolo " + nomeTipoVisita + " non trovato");
        }
    }

    private Set<DayOfWeek> parseGiorniSettimana(String input) {
        Set<DayOfWeek> giorniSettimana = new HashSet<>();
        String[] giorni = input.split(",");
        for (String giorno : giorni) {
            try {
                giorniSettimana.add(DayOfWeek.valueOf(giorno.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Giorno della settimana non valido: " + giorno);
            }
        }
        return giorniSettimana;
    }
}
