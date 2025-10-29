package it.unibs.visite.service.ports;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

/** Porta di sola lettura verso la configurazione prodotta dal configuratore (V1). */
public interface ConfigReadPort {
    /** Giorni del mese indicato globalmente preclusi a qualsiasi visita. */
    Set<LocalDate> precludedDates(YearMonth ym);

    /** Insieme (non vuoto) di giorni settimanali in cui il volontario ha almeno un tipo programmabile. */
    Set<DayOfWeek> programmableDaysForVolunteer(String nickname, YearMonth ym);

    /** Verifica se esiste almeno un tipo di visita, tra quelli associati al volontario,
     *  che sia programmabile in quella data (rispetta: periodo annuale + giorno settimanale + luogo). */
    boolean existsVisitTypeProgrammableOn(String nickname, LocalDate date);

    /** Stringhe descrittive dei tipi di visita associati al volontario, da mostrare a video. */
    Set<String> visitTypeDescriptionsForVolunteer(String nickname);
}
