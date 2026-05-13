package it.unibs.visite.repository.memory;

import java.io.Serializable;
import it.unibs.visite.repository.FruitoreRepository;
import it.unibs.visite.model.Fruitore;
import java.util.*;

public class InMemoryFruitoreRepository implements FruitoreRepository, Serializable {
    private final Map<String, Fruitore> storage;

    public InMemoryFruitoreRepository() {
        this.storage = new HashMap<>();
    }

    public InMemoryFruitoreRepository(Map<String, Fruitore> initialData) {
        this.storage = new HashMap<>(initialData);
    }

    @Override
    public void save(Fruitore fruitore) {
        storage.put(fruitore.getUsername(), fruitore);
    }

    @Override
    public Optional<Fruitore> findByUsername(String username) {
        return Optional.ofNullable(storage.get(username));
    }

    @Override
    public Collection<Fruitore> findAll() {
        return storage.values();
    }

    @Override
    public void delete(String username) {
        storage.remove(username);
    }
}