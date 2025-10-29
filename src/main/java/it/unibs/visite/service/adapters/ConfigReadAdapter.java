package it.unibs.visite.service.adapters;

import it.unibs.visite.service.ports.ConfigReadPort;
import it.unibs.visite.service.ConfigService; // la tua V1
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

/** Adatta la tua ConfigService all'interfaccia minima richiesta dalla V2. */
public final class ConfigReadAdapter implements ConfigReadPort {
    private final ConfigService config;

    public ConfigReadAdapter(ConfigService config) { this.config = config; }

    @Override
    public Set<LocalDate> precludedDates(YearMonth ym) {
        return config.getPrecludedDates(ym); // <--- adegua al tuo API
    }

    @Override
    public Set<DayOfWeek> programmableDaysForVolunteer(String nickname, YearMonth ym) {
        return config.getProgrammableDaysForVolunteer(nickname, ym); // <--- adegua
    }

    @Override
    public boolean existsVisitTypeProgrammableOn(String nickname, LocalDate date) {
        return config.existsVisitTypeProgrammableOn(nickname, date); // <--- adegua
    }

    @Override
    public Set<String> visitTypeDescriptionsForVolunteer(String nickname) {
        return config.visitTypeDescriptionsForVolunteer(nickname); // <--- adegua
    }
}
