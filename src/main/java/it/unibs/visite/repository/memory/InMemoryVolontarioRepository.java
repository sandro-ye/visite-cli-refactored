package it.unibs.visite.repository.memory;

import java.util.*;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.repository.VolontarioRepository;

public class InMemoryVolontarioRepository implements VolontarioRepository {
    private final Map<String, Volontario> storage = new HashMap<>();
    
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