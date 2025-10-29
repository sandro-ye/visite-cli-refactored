package it.unibs.visite.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import it.unibs.visite.core.Preconditions;
import it.unibs.visite.model.DataStore;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.model.ParametriSistema;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.persistence.FilePersistence;

public class ConfigService {
    private final FilePersistence fp;
    private DataStore ds;

    public ConfigService(FilePersistence fp) {
        this.fp = fp;
        fp.ensureDirs();
        this.ds = fp.loadDataOrNew();
    }

    public DataStore getSnapshot() {
        return ds;
    }

    public void setAmbitoUnaTantum(String ambito) {
        Preconditions.notBlank(ambito, "Ambito non può essere vuoto");
        ParametriSistema p = ds.getParametri();
        p.setAmbitoTerritorialeUnaTantum(ambito);
        save();
    }

    public void setMaxPersone(int max) {
        ds.getParametri().setMaxPersonePerIscrizione(max);
        save();
    }

    public boolean isInitialized() {
        return ds.getParametri().isInitialized();
    }

    public void markInitialized() {
        ds.getParametri().markInitialized();
        save();
    }

    public void save() {
        fp.saveData(ds);
    }

    // Restituisce le date precluse per un certo mese
    public Set<LocalDate> getPrecludedDates(YearMonth ym) {
        return ds.getPreclusioniFor(ym);
    }

 public boolean existsVisitTypeProgrammableOn(String nickname, LocalDate date) {
    // 1. volontario deve esistere
    Volontario vol = ds.getVolontario(nickname);
    if (vol == null) return false;

    // 2. prendo tutti i tipiVisita
    for (TipoVisita tv : ds.getTipiVisita()) {

        // 3. se questo tipoVisita include il volontario...
        if (tv.getVolontariNicknames().contains(nickname)) {

            // 4. QUI sarebbe il punto in cui controlleremmo se il tipo è effettivamente
            //    programmabile proprio in quella data (giorno settimanale, periodo valido, ecc.)
            //    Ma al momento TipoVisita NON contiene queste informazioni nel tuo modello.
            //    Quindi, per ora, diciamo che "esiste un tipo che può teoricamente essere programmato".
            return true;
        }
    }

    return false;
}



    public Set<String> visitTypeDescriptionsForVolunteer(String nickname) {
    Volontario vol = ds.getVolontario(nickname);
    if (vol == null) return Set.of();

    Set<String> result = new LinkedHashSet<>();

    for (TipoVisita tv : ds.getTipiVisita()) {
        if (tv.getVolontariNicknames().contains(nickname)) {
            Luogo luogo = ds.getLuogo(tv.getLuogoId());
            String luogoNome = (luogo != null ? luogo.getNome() : "(luogo sconosciuto)");
            result.add(tv.getTitolo() + " @ " + luogoNome);
        }
    }

    return result;
}


    public Set<DayOfWeek> getProgrammableDaysForVolunteer(String nickname, YearMonth ym) {
    Volontario vol = ds.getVolontario(nickname);
    if (vol == null) return Set.of();

    // Al momento il dominio NON modella:
    // - giorni settimanali ammessi per ciascun tipo di visita
    // - validità nel mese
    // quindi non possiamo calcolare delle informazioni reali qui.

    return Set.of();
}


}
