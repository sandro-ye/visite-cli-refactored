package it.unibs.visite.repository.memory;

import java.io.Serializable;
import java.util.*;
import it.unibs.visite.repository.LuogoRepository;
import it.unibs.visite.model.Luogo;

public class InMemoryLuogoRepository implements LuogoRepository, Serializable {
    private final Map<String, Luogo> storage = new HashMap<>();

    @Override
    public void save(Luogo luogo) {
        storage.put(luogo.getId(), luogo);
    }

    @Override
    public Optional<Luogo> findLuogoById(String idString) {
        return Optional.ofNullable(storage.get(idString));
    }

    @Override
    public Collection<Luogo> findAllLuoghi() {
        return Collections.unmodifiableCollection(storage.values());
    }

    @Override
    public void deleteLuogo(String idString) {
        storage.remove(idString);
    }
}
