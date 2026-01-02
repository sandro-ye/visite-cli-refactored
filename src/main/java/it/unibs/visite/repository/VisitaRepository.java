package it.unibs.visite.repository;

import java.util.*;
import it.unibs.visite.model.Visita;

public interface VisitaRepository {
    void save(Visita visita);
    Collection<Visita> findAll();
    Optional<Visita> findByCodiceIscrizione(String codiceIscrizione);
    void deleteByCodiceIscrizione(String codiceIscrizione);
}
