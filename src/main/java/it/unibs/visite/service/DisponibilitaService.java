package it.unibs.visite.service;

import it.unibs.visite.core.DomainException;
import it.unibs.visite.model.DisponibilitaVolontario;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.persistence.FileRepositoryPersistence;
import it.unibs.visite.repository.PreclusioneRepository;
import it.unibs.visite.repository.VolontarioRepository;
import it.unibs.visite.repository.memory.InMemoryPreclusioneRepository;
import it.unibs.visite.repository.memory.InMemoryVolontarioRepository;

import java.nio.file.Paths;
import java.time.*;
import java.util.List;

public class DisponibilitaService {
    private final PreclusioneRepository preclusioneRepository;
    private final VolontarioRepository volontarioRepository;

    public DisponibilitaService(PreclusioneRepository preclusioneRepository, VolontarioRepository volontarioRepository) {
        this.preclusioneRepository = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "preclusioni.ser"),
            InMemoryPreclusioneRepository::new);
        this.volontarioRepository = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "volontari.ser"), 
            InMemoryVolontarioRepository::new);
    }

    public void aggiungiDisponibilita(String nickname, LocalDate data) {
        if (preclusioneRepository.contains(data)) {
            throw new DomainException("Data preclusa");
        }
        Volontario volontario = getVolontarioByNickname(nickname);
        if(!dataValidaPerVolontario(volontario, data)) {
            throw new DomainException("Data non valida");
        }
        volontario.addDisponibilita(data);
        volontarioRepository.save(volontario);

        FileRepositoryPersistence.salvaOggetto(volontarioRepository, Paths.get("data", "volontari.ser"));
    }

    public boolean verificaDisponibilita(String nickname, LocalDate data) {
        Volontario volontario = getVolontarioByNickname(nickname);
        return volontario.isDisponibile(data) && !preclusioneRepository.contains(data);
    }

    private boolean dataValidaPerVolontario(Volontario volontario, LocalDate data) {    
        YearMonth ym = nextMonth();
        if(!YearMonth.from(data).equals(ym)) return false;

        for (TipoVisita tipoVisita : volontario.getTipiVisitaCompetenza()) {
            DayOfWeek giornoVisita = data.getDayOfWeek();
            if (!tipoVisita.getGiorniSettimana().contains(giornoVisita) ||
                data.isBefore(tipoVisita.getDataInizioProgrammazione()) ||
                data.isAfter(tipoVisita.getDataFineProgrammazione())) {
                return false;
            }
        }
        return true;
    }

    public void rimuoviDisponibilita(String nickname, LocalDate data) {
        Volontario volontario = getVolontarioByNickname(nickname);
        volontario.removeDisponibilita(data);
        volontarioRepository.save(volontario);
        FileRepositoryPersistence.salvaOggetto(volontarioRepository, Paths.get("data", "volontari.ser"));
    }

    public List<DisponibilitaVolontario> getDisponibilitaDi(String nickname) {
        Volontario volontario = getVolontarioByNickname(nickname);
        return volontario.getDisponibilita().stream().toList();
    }

    public List<TipoVisita> getAllTipiVisita(String nickname) {
        Volontario volontario = getVolontarioByNickname(nickname);
        return volontario.getTipiVisitaCompetenza().stream().toList();
    }
    
    private YearMonth nextMonth() {
        LocalDate today = LocalDate.now();
        return YearMonth.from(today.plusMonths(1).withDayOfMonth(1));
    }

    private Volontario getVolontarioByNickname(String nickname) {
        return volontarioRepository.findByNickname(nickname)
                .orElseThrow(() -> new DomainException("Volontario non esistente"));
    }
}
