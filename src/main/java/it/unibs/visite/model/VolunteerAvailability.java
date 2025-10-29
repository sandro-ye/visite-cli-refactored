package it.unibs.visite.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class VolunteerAvailability implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final String nickname;       // username del volontario
    private final YearMonth month;       // mese a cui si riferiscono le date
    private final Set<LocalDate> dates;  // insieme di date disponibili (idempotente)

    public VolunteerAvailability(String nickname, YearMonth month, Set<LocalDate> dates) {
        this.nickname = Objects.requireNonNull(nickname);
        this.month = Objects.requireNonNull(month);
        this.dates = new HashSet<>(Objects.requireNonNull(dates));
    }

    public String nickname() { return nickname; }
    public YearMonth month() { return month; }
    public Set<LocalDate> dates() { return Collections.unmodifiableSet(dates); }

    public boolean add(LocalDate d) { return dates.add(Objects.requireNonNull(d)); }
    public boolean remove(LocalDate d) { return dates.remove(Objects.requireNonNull(d)); }

    @Override public String toString() {
        return "Availability[" + nickname + " " + month + " -> " + dates + "]";
    }
}
 
    

