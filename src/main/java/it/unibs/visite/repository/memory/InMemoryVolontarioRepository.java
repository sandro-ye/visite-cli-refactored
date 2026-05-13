package it.unibs.visite.repository.memory;

import java.io.Serializable;
import java.util.*;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.repository.VolontarioRepository;

public class InMemoryVolontarioRepository implements VolontarioRepository, Serializable {
    private final Map<String, Volontario> storage;

    public InMemoryVolontarioRepository() {
        this.storage = new HashMap<>();
    }

    public InMemoryVolontarioRepository(HashMap<String, Volontario> volontari) {
        this.storage = new HashMap<>(volontari);
    }
    
    @Override
    public void save(Volontario volontario) {
        storage.put(volontario.getNickname(), volontario);
    }

    @Override
    public Optional<Volontario> findByNickname(String nickname) {
        return Optional.ofNullable(storage.get(nickname));
    }

    @Override
    public Collection<Volontario> findAll() {
        return Collections.unmodifiableCollection(storage.values());
    }

    @Override
    public void delete(String nickname) {
        storage.remove(nickname);
    }
}