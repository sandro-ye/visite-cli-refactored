package it.unibs.visite.repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

public interface PreclusioneRepository {
    void add(LocalDate data);
    void remove(LocalDate data);
    boolean contains(LocalDate data);
    Set<LocalDate> getAll();
    Set<LocalDate> getForMonth(YearMonth ym);
}
