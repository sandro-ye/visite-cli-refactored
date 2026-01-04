package it.unibs.visite.repository.memory;

import it.unibs.visite.repository.*;
import java.util.*;
import it.unibs.visite.model.Visita;

public class InMemoryArchivioVisite implements VisitaRepository {
    private final Map<String, Visita> archivio = new HashMap<>();


    @Override
    public void save(Visita visita) {
        archivio.put(visita.getId(), visita);
    }

    @Override
    public Collection<Visita> findAll() {
        return archivio.values();
    }

    @Override
    public Optional<Visita> find(String codiceIscrizione) {
        return null;
    }

    @Override
    public void delete(String codiceIscrizione) {

    }
}
