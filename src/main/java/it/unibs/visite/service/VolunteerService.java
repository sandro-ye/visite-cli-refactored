package it.unibs.visite.service;

import it.unibs.visite.persistence.AvailabilityRepository;
import it.unibs.visite.service.ports.ConfigReadPort;
import it.unibs.visite.core.Preconditions;

import java.io.IOException;
import java.time.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class VolunteerService {

    private final ConfigReadPort config;                // adapter su ConfigService
    private final AvailabilityRepository repo;          // nuovo per V2
    private final Clock clock;                          // per testabilità

    public VolunteerService(ConfigReadPort config, AvailabilityRepository repo, Clock clock) {
        this.config = Preconditions.notNull(config, "config");
        this.repo = Preconditions.notNull(repo, "repo");
        this.clock = Preconditions.notNull(clock, "clock");
    }

    /** Mese entrante rispetto ad "oggi" (orologio applicativo). */
    public YearMonth nextMonth() {
        LocalDate today = LocalDate.now(clock);
        return YearMonth.from(today.plusMonths(1).withDayOfMonth(1));
    }

    /** Tipi di visita del volontario (stringhe leggibili). */
    public Set<String> visitTypesOf(String nickname) {
        return new TreeSet<>(config.visitTypeDescriptionsForVolunteer(nickname));
    }

    /** Ritorna le date dichiarate finora per il mese entrante. */
    public Set<LocalDate> myDatesForNextMonth(String nickname) throws IOException {
        return repo.getDates(nickname, nextMonth());
    }

    /** Inserisce nuove date (si uniscono a quelle già registrate). */
    public void declareAvailability(String nickname, List<LocalDate> dates) throws IOException {
        Preconditions.notBlank(nickname, "nickname obbligatorio");

        YearMonth target = nextMonth();
        Set<LocalDate> cleaned = new LinkedHashSet<>();
        for (LocalDate d : dates) {
            validateSingleDate(nickname, target, d);
            cleaned.add(d);
        }
        repo.addDates(nickname, target, cleaned);
    }

    /** Rimuove una singola data precedentemente inserita. */
    public void revokeAvailability(String nickname, LocalDate date) throws IOException {
        YearMonth target = nextMonth();
        validateSingleDateBelongsToMonth(target, date);
        repo.removeDate(nickname, target, date);
    }

    // ----------------- Validazioni richieste da specifica -----------------

    private void validateSingleDate(String nickname, YearMonth ym, LocalDate d) {
        Preconditions.notNull(d, "data nulla");
        validateSingleDateBelongsToMonth(ym, d);

        // (b) non preclusa
        Set<LocalDate> precluded = config.precludedDates(ym);
        Preconditions.check(!precluded.contains(d), "Data " + d + " preclusa a ogni visita");

        // (c) esiste almeno un tipo del volontario programmabile in quel giorno
        Preconditions.check(config.existsVisitTypeProgrammableOn(nickname, d),
                "Nessun tipo di visita del volontario è programmabile il giorno " + d.getDayOfWeek());
    }

    private static void validateSingleDateBelongsToMonth(YearMonth ym, LocalDate d) {
        Preconditions.check(YearMonth.from(d).equals(ym), "La data " + d + " non appartiene al mese " + ym);
    }
}
