package it.unibs.visite.security;

import it.unibs.visite.repository.CredentialsRepository;
import it.unibs.visite.model.LoginResult;

/**
 * Servizio di autenticazione e gestione credenziali.
 * - login
 * - cambio password
 * - creazione credenziali utenti con ruoli diversi (ADMIN, VOLUNTEER, FRUITORE)
 * - verifica ruoli
 * - rimozione credenziali utente
 * 
 * possibile unione di metodo per creare credeziali di volontario fruitore e configuratore in un unico metodo 
 * con parametro ruolo
 * 
 * !!! - verificare se è necessario aggiungere metodo che crei anche il fruitore nel FruitoreRepository quando si crea un nuovo fruitore (creazione credenziali + creazione fruitore)
 */

public class AuthService {
    private final CredentialsRepository repository;
    private final CredentialsStore creds;
    private static final String DEFAULT_VOLUNTEER_PASSWORD = "volontario";
    
    public AuthService(CredentialsRepository credsRepo, CredentialsStore creds) {
        this.repository  = credsRepo;
        this.creds = creds;
    }

    public LoginResult login(String username, char[] password) {
        if(!creds.verify(username, password)) {
            return LoginResult.FAILURE;
        } 
        if(mustChangePassword(username)) {
            return LoginResult.FIRST_ACCESS;
        }
        return LoginResult.SUCCESS;
    }

    public boolean mustChangePassword(String username) {
        CredentialsStore.Entry e = creds.getUsers().get(username);
        return e != null && e.mustChangePassword;
    }

    public void changePassword(String username, char[] pass1, char[] pass2) {
        if(!new String(pass1).equals(new String(pass2))) {
            throw new IllegalArgumentException("Password non corrispondenti");
        }
        creds.changePassword(username, pass1);
        repository.save(creds);
    }

    // crea un nuovo configuratore (amministratore)
    public void createConfigurator(String username, char[] password) {
        validateUsername(username);
        creds.putNewUser(username, password, false, "ADMIN");
        repository.save(creds);
    }

    // === NUOVO: crea un volontario ===
    public void createVolunteer(String username) {
        validateUsername(username);
        creds.putNewUser(username, DEFAULT_VOLUNTEER_PASSWORD.toCharArray(), true, "VOLUNTEER");
        repository.save(creds);
    }

    // === NUOVO: controlla ruolo admin ===
    public boolean isAdmin(String username) {
        CredentialsStore.Entry e = creds.getUsers().get(username);
        return e != null && "ADMIN".equalsIgnoreCase(e.role);
    }

    // === NUOVO: controlla ruolo volontario ===
    public boolean isVolunteer(String username) {
        CredentialsStore.Entry e = creds.getUsers().get(username);
        return e != null && "VOLUNTEER".equalsIgnoreCase(e.role);
    }

    public void rimuoviCredenziali(String username) {
        creds.rimuoviCredenziali(username);
        repository.save(creds);
    }

    // === versione 4 ===
    public void createFruitore(String username, char[] password) {
        validateUsername(username);
        creds.putNewUser(username, password, false, "FRUITORE");
        repository.save(creds);
    }

    // === NUOVO: controlla ruolo fruitore ===
    public boolean isFruitore(String username) {
        CredentialsStore.Entry e = creds.getUsers().get(username);
        return e != null && "FRUITORE".equalsIgnoreCase(e.role);
    }

    public String getUserRole(String username) {
        CredentialsStore.Entry e = creds.getUsers().get(username);
        return e != null ? e.role : null;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username non valido");
        }

        if (creds.getUsers().containsKey(username)) {
            throw new IllegalArgumentException("Username già esistente: " + username);
        }
    }
}
