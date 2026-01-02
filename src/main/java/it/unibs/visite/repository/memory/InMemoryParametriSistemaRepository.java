package it.unibs.visite.repository.memory;

import it.unibs.visite.repository.ParametriSistemaRepository;
import it.unibs.visite.model.ParametriSistema;

public class InMemoryParametriSistemaRepository implements ParametriSistemaRepository{
    private ParametriSistema parametri;

    @Override
    public ParametriSistema load() {
        return parametri;
    }

    @Override
    public void save(ParametriSistema parametri) {
        this.parametri = parametri;
    }
}