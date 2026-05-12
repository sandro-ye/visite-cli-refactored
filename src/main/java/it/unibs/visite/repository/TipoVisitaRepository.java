package it.unibs.visite.repository;

import java.util.*;
import it.unibs.visite.model.TipoVisita;

public interface TipoVisitaRepository {
    void save(TipoVisita tipoVisita);
    Optional<TipoVisita> findById(String id);
    Optional<TipoVisita> findByTitolo(String titolo);
    Collection<TipoVisita> findAll();
    void delete(String id);
}
