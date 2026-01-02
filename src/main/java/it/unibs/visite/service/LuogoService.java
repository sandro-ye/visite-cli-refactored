package it.unibs.visite.service;

import it.unibs.visite.core.DomainException;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.repository.LuogoRepository;

public class LuogoService {
    private final LuogoRepository repository;

    public LuogoService(LuogoRepository repository) {
        this.repository = repository;
    }

    public void addLuogo(Luogo luogo) {
        if(repository.findLuogoById(luogo.getId()).isPresent()) {
            throw new DomainException("luogo già esistente");
        }
        repository.save(luogo);
    }
}
