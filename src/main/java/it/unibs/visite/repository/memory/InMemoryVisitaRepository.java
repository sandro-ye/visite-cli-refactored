package it.unibs.visite.repository.memory;

import java.io.Serializable;
import java.util.*;
import it.unibs.visite.model.Visita;
import it.unibs.visite.repository.VisitaRepository;

public class InMemoryVisitaRepository implements VisitaRepository, Serializable {
    private final Map<String, Visita> storage = new HashMap<>();
    
    @Override
    public void save(Visita visita) {
        storage.put(visita.getId(), visita);
    }

    @Override
    public Collection<Visita> findAll() {
        return Collections.unmodifiableCollection(storage.values());
    }

    @Override
    public Optional<Visita> findByCodiceIscrizione(String codiceIscrizione) {
        return Optional.ofNullable(storage.get(codiceIscrizione));
    }

    @Override
    public void deleteByCodiceIscrizione(String codiceIscrizione) {
        storage.remove(codiceIscrizione);
    }
}
