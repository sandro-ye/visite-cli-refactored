package it.unibs.visite.repository.memory;

import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.repository.TipoVisitaRepository;
import java.util.*;

public class InMemoryTipoVIsitaRepository implements TipoVisitaRepository {
    private final Map<String, TipoVisita> storage = new HashMap<>();
    
    @Override
    public void save(TipoVisita tipoVisita) {
        storage.put(tipoVisita.getId(), tipoVisita);
    }

    @Override
    public Optional<TipoVisita> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Collection<TipoVisita> findAll() {
        return Collections.unmodifiableCollection(storage.values());
    }

    @Override
    public void delete(String id) {
        storage.remove(id);
    }
}
