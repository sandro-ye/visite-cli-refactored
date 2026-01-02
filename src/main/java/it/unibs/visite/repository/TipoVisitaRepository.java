package it.unibs.visite.repository;

import java.util.Optional;
import java.util.Collection;

import it.unibs.visite.model.TipoVisita;

public interface TipoVisitaRepository {
    void save(TipoVisita tipoVisita);
    Optional<TipoVisita> findById(String id);
    Collection<TipoVisita> findAll();
    void delete(String id);
}
