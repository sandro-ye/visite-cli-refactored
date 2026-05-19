package it.unibs.visite.service;

import it.unibs.visite.core.DomainException;
import it.unibs.visite.model.*;
import it.unibs.visite.repository.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class RegimeService {
    private final PreclusioneRepository preclusioneRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaRepository archivioRepository;
    private final ParametriSistemaRepository parametriSistemaRepository;
    private final VolontarioRepository volontarioRepository;
    private final LuogoRepository luogoRepository;
    private final TipoVisitaRepository tipoVisitaRepository;

    public RegimeService(PreclusioneRepository preclusioneRepository, VisitaRepository visitaRepository, VisitaRepository archivioRepository, 
                ParametriSistemaRepository parametriSistemaRepository, VolontarioRepository volontarioRepository, 
                LuogoRepository luogoRepository, TipoVisitaRepository tipoVisitaRepository) {
        this.preclusioneRepository = preclusioneRepository;
        this.visitaRepository = visitaRepository;
        this.archivioRepository = archivioRepository;
        this.parametriSistemaRepository = parametriSistemaRepository;
        this.volontarioRepository = volontarioRepository;
        this.luogoRepository = luogoRepository;
        this.tipoVisitaRepository = tipoVisitaRepository;
    }
    
    public void aggiungiPreclusione(LocalDate data) {
        if(YearMonth.from(data).isBefore(YearMonth.now().plusMonths(3))) {
            throw new IllegalArgumentException("Impossiibile aggiungere preclusione");
        }

        YearMonth now = YearMonth.from(LocalDate.now());
        LocalDate windowStart = LocalDate.of(now.getYear(), now.getMonth(), 16);
        LocalDate windowEnd = LocalDate.of(now.plusMonths(1).getYear(),
            now.plusMonths(1).getMonth(),
            15);
        if(data.isBefore(windowStart) || data.isAfter(windowEnd)) {
            throw new DomainException("Data fuori dalla finestra temporale consentita: dal " + windowStart + " al " + windowEnd);
        }
        
        preclusioneRepository.add(data);
    }
     
    public List<LocalDate> getPreclusioniPer(YearMonth mese) {
        return preclusioneRepository.getAll().stream()
            .filter(d -> YearMonth.from(d).equals(mese))
            .sorted()
            .collect(Collectors.toList());
    }

    public void setMaxPersonePerIscrizione(int max) {
        if(max < 1) {
            throw new IllegalArgumentException("Numero massimo di persone per iscrizione deve essere >= 1");
        }
        ParametriSistema parametri = parametriSistemaRepository.load();
        parametri.setMaxPersonePerIscrizione(max);
        parametriSistemaRepository.save(parametri);;
    }

    public List<Volontario> getElencoVolontari() {
        return volontarioRepository.findAll().stream()
            .sorted(Comparator.comparing(Volontario::getNickname))
            .collect(Collectors.toList());
    }

    public List<TipoVisita> getTipiVisitaDi(Volontario volontario) {
        return volontario.getTipiVisitaCompetenza().stream()
            .sorted(Comparator.comparing(TipoVisita::getTitolo))
            .collect(Collectors.toList());
    }

    public List<Luogo> getElencoLuoghi() {
        return luogoRepository.findAllLuoghi().stream()
            .sorted(Comparator.comparing(Luogo::getNome))
            .collect(Collectors.toList());
    }

    public List<TipoVisita> getTipiVisitaPerLuogo(String luogoId) {
        Optional<Luogo> l = luogoRepository.findLuogoById(luogoId);
        if (l.isPresent()) {
            List<String> tipiIds = l.get().getTipiVisitaIds().stream().collect(Collectors.toList());
            List<TipoVisita> tipi = new ArrayList<>();
            for (String tid : tipiIds) {
                Optional<TipoVisita> t = tipoVisitaRepository.findById(tid);
                t.ifPresent(tipi::add);
            }
        }
        return List.of();
    }

    public List<Visita> getVisitePerStato(StatoVisita stato) {
        if(stato == StatoVisita.EFFETTUATA) {
            return archivioRepository.findAll().stream()
                .filter(v -> v.getStato() == stato)
                .sorted(Comparator.comparing(Visita::getData))
                .collect(Collectors.toList());
        }
        return visitaRepository.findAll().stream()
            .filter(v -> v.getStato() == stato)
            .sorted(Comparator.comparing(Visita::getData))
            .collect(Collectors.toList());
    }

    public boolean checkGiornoSedici() {
        return LocalDate.now().getDayOfMonth() == 16;
    }

    /**
     * Aggiunge una preclusione per una data appartenente al mese target (YearMonth).
     * Controlla che:
     *  - il month target sia il mese = now + 3 mesi (requisito V1)
     *  - la data specificata appartenga al month target
     *  - la chiamata venga effettuata nel periodo consentito:
     *      dal giorno 16 del mese i  al giorno 15 del mese i+1 (inclusi),
     *    dove i = targetMonth.minusMonths(3)
     */

    /*
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

    */
}