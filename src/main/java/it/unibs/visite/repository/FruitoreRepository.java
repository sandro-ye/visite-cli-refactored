package it.unibs.visite.repository;

import java.util.*;
import it.unibs.visite.model.Fruitore;

public interface FruitoreRepository {
    void save(Fruitore fruitore);
    Optional<Fruitore> findByUsername(String username);
    Collection<Fruitore> findAll();
    void delete(String username);
}
