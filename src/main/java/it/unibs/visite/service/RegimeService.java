package it.unibs.visite.service;

import it.unibs.visite.core.Preconditions;
import it.unibs.visite.model.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class RegimeService {
    private final ConfigService configService;
    private final ZoneId zone = ZoneId.of("Europe/Rome");

    public RegimeService(ConfigService configService) {
        this.configService = configService;
    }

    public ConfigService getConfigService() { return configService; }

    /**
     * Aggiunge una preclusione per una data appartenente al mese target (YearMonth).
     * Controlla che:
     *  - il month target sia il mese = now + 3 mesi (requisito V1)
     *  - la data specificata appartenga al month target
     *  - la chiamata venga effettuata nel periodo consentito:
     *      dal giorno 16 del mese i  al giorno 15 del mese i+1 (inclusi),
     *    dove i = targetMonth.minusMonths(3)
     */
    public void addPreclusioneForMonth(YearMonth targetMonth, LocalDate dateToExclude) {
        Preconditions.notNull(targetMonth, "targetMonth non può essere null");
        Preconditions.notNull(dateToExclude, "dateToExclude non può essere null");
        Preconditions.check(YearMonth.from(dateToExclude).equals(targetMonth),
                "data deve appartenere al mese target");

        LocalDate now = LocalDate.now(zone);
        YearMonth expectedTarget = YearMonth.from(now).plusMonths(3);
        Preconditions.check(expectedTarget.equals(targetMonth), "preclusioni possono essere impostate solo per il mese " + expectedTarget);

        // calcolo finestra consentita:
        YearMonth i = targetMonth.minusMonths(3);
        LocalDate windowStart = LocalDate.of(i.getYear(), i.getMonth(), 16);
        YearMonth iPlus1 = i.plusMonths(1);
        LocalDate windowEnd = LocalDate.of(iPlus1.getYear(), iPlus1.getMonth(), 15);

        Preconditions.check(!LocalDate.now(zone).isBefore(windowStart) && !LocalDate.now(zone).isAfter(windowEnd), 
            "fuori dalla finestra temporale consentita: dal " + windowStart + " al " + windowEnd);

        configService.getSnapshot().addPreclusione(targetMonth, dateToExclude);
        configService.save();
    }

    public Set<LocalDate> getPreclusioniFor(YearMonth targetMonth) {
        return configService.getSnapshot().getPreclusioniFor(targetMonth);
    }

    // modifica max persone (fa già save tramite ConfigService)
    public void setMaxPersone(int newMax) {
        configService.getSnapshot().getParametri().setMaxPersonePerIscrizione(newMax);
    }

    // Visualizzazioni richieste ===================================================
    public List<String> elencoVolontariConTipi() {
        DataStore ds = configService.getSnapshot();
        List<String> out = new ArrayList<>();
        for (Volontario v : ds.getVolontari()) {
            List<String> tipi = ds.getTipiVisita().stream()
                    .filter(t -> t.getVolontariNicknames().contains(v.getNickname()))
                    .map(TipoVisita::getTitolo)
                    .collect(Collectors.toList());
            out.add(v.getNickname() + " -> " + tipi);
        }
        return out;
    }

    public List<String> elencoLuoghi() {
        return configService.getSnapshot().getLuoghi().stream()
                .map(l -> String.format("%s (id=%s) - %s", l.getNome(), l.getId(), l.getDescrizione()))
                .collect(Collectors.toList());
    }

    public List<String> tipiPerLuogo(String luogoId) {
        Preconditions.notBlank(luogoId, "luogoId obbligatorio");
        return configService.getSnapshot().getTipiVisita().stream()
                .filter(t -> t.getLuogoId().equals(luogoId))
                .map(t -> String.format("%s (id=%s) - %s - volontari=%s",
                        t.getTitolo(), t.getId(), t.getDescrizione(), t.getVolontariNicknames()))
                .collect(Collectors.toList());
    }

    public Map<StatoVisita, List<Visita>> visitePerStato() {
        return configService.getSnapshot()
                .getVisite()
                .stream()
                .collect(Collectors.groupingBy(Visita::getStato,
                        () -> new EnumMap<>(StatoVisita.class),
                        Collectors.toList()));
    }
}