package it.unibs.visite.bootstrap;

import it.unibs.visite.repository.CredentialsRepository;
import it.unibs.visite.security.CredentialsStore;

public class AuthBootstrap {
    public static CredentialsStore initialize(CredentialsRepository credsRepo) {
        CredentialsStore creds = credsRepo.load();
        if (creds.getUsers().isEmpty()) {
            // Se non ci sono utenti, creiamo un admin di default
            creds.putNewUser("admin", "admin".toCharArray(), true, "ADMIN");
            creds.putNewUser("volontario", "volontario".toCharArray(), true, "VOLUNTEER");

            credsRepo.save(creds);
        }
        return creds;
    }
}
