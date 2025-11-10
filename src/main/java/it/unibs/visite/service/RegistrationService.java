package it.unibs.visite.service;

import it.unibs.visite.core.Preconditions;
import it.unibs.visite.model.DataStore;
import it.unibs.visite.model.Fruitore;
import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;

/**
 * Coordina la registrazione dei fruitori tra dominio (DataStore) e credenziali (AuthService).
 * Non duplica logica di hashing/ruoli: delega ad AuthService.
 */
public class RegistrationService {

    private final DataStore ds;
    private final FilePersistence fp;
    private final AuthService auth;

    public RegistrationService(DataStore ds, FilePersistence fp, AuthService auth) {
        this.ds   = Preconditions.notNull(ds,   "DataStore nullo");
        this.fp   = Preconditions.notNull(fp,   "FilePersistence nulla");
        this.auth = Preconditions.notNull(auth, "AuthService nullo");
    }

    /**
     * Username disponibile se:
     *  - NON esiste già tra le CREDENZIALI (qualsiasi ruolo)
     *  - NON esiste già tra i FRUITORI nel dominio
     */
    public boolean isUsernameAvailable(String username) {
        Preconditions.notBlank(username, "username obbligatorio");
        boolean giaNelleCredenziali = auth.getCredentialsStore().getUsers().containsKey(username);
        boolean giaFruitoreDominio  = ds.existsFruitore(username);
        return !giaNelleCredenziali && !giaFruitoreDominio;
    }

    /**
     * Registra un fruitore:
     *  1) valida l'unicità
     *  2) crea le credenziali con ruolo FRUITORE
     *  3) aggiunge l'entità Fruitore nel DataStore
     *  4) persiste il DataStore
     */
    public Fruitore registerFruitore(String username, char[] password) {
        Preconditions.notBlank(username, "username obbligatorio");
        Preconditions.notNull(password, "password nulla");
        Preconditions.check(isUsernameAvailable(username), "Username già in uso");

        // 1) Credenziali
        auth.createFruitore(username, password);

        // 2) Dominio
        Fruitore f = new Fruitore(username);
        ds.addFruitore(f);

        // 3) Persistenza del dominio
        fp.saveData(ds);

        return f;
    }
}
