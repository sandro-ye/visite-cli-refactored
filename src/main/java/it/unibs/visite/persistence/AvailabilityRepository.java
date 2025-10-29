package it.unibs.visite.persistence;

import it.unibs.visite.model.VolunteerAvailability;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.Set;

public interface AvailabilityRepository {
    Optional<VolunteerAvailability> find(String nickname, YearMonth ym) throws IOException;
    void upsert(VolunteerAvailability availability) throws IOException;
    void addDates(String nickname, YearMonth ym, Set<LocalDate> dates) throws IOException;
    void removeDate(String nickname, YearMonth ym, LocalDate date) throws IOException;
    Set<LocalDate> getDates(String nickname, YearMonth ym) throws IOException;
}
