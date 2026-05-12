package it.unibs.visite.repository;

import java.util.Collection;
import java.util.Optional;

import it.unibs.visite.model.Luogo;

public interface LuogoRepository {
    void save(Luogo luogo);
    Optional<Luogo> findLuogoById(String idString);
    Optional<Luogo> findByNome(String nome);
    Collection<Luogo> findAllLuoghi();
    void deleteLuogo(String idString);
}
