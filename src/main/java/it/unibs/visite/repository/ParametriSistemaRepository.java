package it.unibs.visite.repository;

import it.unibs.visite.model.ParametriSistema;

public interface ParametriSistemaRepository {
    ParametriSistema load();
    void save(ParametriSistema parametri);
}
