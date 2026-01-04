package it.unibs.visite.service;

import it.unibs.visite.persistence.AvailabilityRepository;
import it.unibs.visite.repository.PreclusioneRepository;
import java.time.*;
import java.util.Set;

public class DisponibilitaService {
    private final PreclusioneRepository preclusioneRepository;
    private final AvailabilityRepository availabilityRepository;

    public DisponibilitaService(PreclusioneRepository preclusioneRepository,
                                AvailabilityRepository availabilityRepository) {
        this.preclusioneRepository = preclusioneRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public void aggiungiDisponibilita(String nickname, YearMonth ym, Set<LocalDate> dates) {
        for (LocalDate date : dates) {
            if (preclusioneRepository.contains(date)) {
                throw new IllegalArgumentException("La data " + date + " è preclusa e non può essere selezionata.");
            }
        }
        try {
            availabilityRepository.addDates(nickname, ym, dates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isVolontarioDisponibile(String nickname, YearMonth ym, LocalDate date) {
        try {
            var availabilityOpt = availabilityRepository.find(nickname, ym);
            return availabilityOpt.map(av -> av.dates().contains(date)).orElse(false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // capire se serve e eventualmente implementare
    public void clearDisponibilita(YearMonth ym) {

    }
}
