package it.unibs.visite.repository.memory;

import java.util.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.Collectors;

import it.unibs.visite.repository.PreclusioneRepository;

public class InMemoryPreclusioneRepository implements PreclusioneRepository, Serializable {
    private final Set<LocalDate> preclusioni = new HashSet<>();

    @Override
    public void add(LocalDate data) {
        preclusioni.add(data);
    }

    @Override
    public void remove(LocalDate data) {
        preclusioni.remove(data);
    }

    @Override
    public boolean contains(LocalDate data) {
        return preclusioni.contains(data);
    }

    @Override
    public Set<LocalDate> getAll() {
        return Collections.unmodifiableSet(preclusioni);
    }

    @Override
    public Set<LocalDate> getForMonth(YearMonth ym) {
        return preclusioni.stream()
                .filter(date -> date.getMonth() == ym.getMonth())
                .filter(date -> date.getYear() == ym.getYear())
                .collect(Collectors.toSet());
    }
}