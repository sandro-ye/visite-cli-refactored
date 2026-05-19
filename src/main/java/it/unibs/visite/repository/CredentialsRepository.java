package it.unibs.visite.repository;

import it.unibs.visite.security.CredentialsStore;

public interface CredentialsRepository {
    CredentialsStore load();
    void save(CredentialsStore creds);
}
