package it.unibs.visite.repository.memory;

import it.unibs.visite.repository.*;
import java.util.*;
import java.io.Serializable;
import it.unibs.visite.model.Visita;

public class InMemoryArchivioVisite implements VisitaRepository, Serializable {
    private final Map<String, Visita> archivio;

    public InMemoryArchivioVisite() {
        this.archivio = new HashMap<>();
    }

    public InMemoryArchivioVisite(Map<String, Visita> archivio) {
        this.archivio = archivio;
    }

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
