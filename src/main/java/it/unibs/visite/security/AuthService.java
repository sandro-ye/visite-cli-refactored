package it.unibs.visite.security;

import java.nio.file.Path;
import it.unibs.visite.persistence.FilePersistence;
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
    private final FilePersistence fp;
    private CredentialsStore creds;
    private static final String DEFAULT_VOLUNTEER_PASSWORD = "volontario";
    
    public AuthService(FilePersistence fp, CredentialsStore creds) {
        this.fp = fp;
        this.creds = creds;
    }

     
    public AuthService() {
        this.fp = new FilePersistence(Path.of("data"));

        Object saved = fp.loadCredentialsOrNull();
        if (saved == null) {
            this.creds = new CredentialsStore();
            // utente configuratore iniziale (default admin/admin)
            // true = deve cambiare password al primo login
            creds.putNewUser("admin", "admin".toCharArray(), true, "ADMIN");
            creds.putNewUser("volontario", "volontario".toCharArray(), true, "VOLUNTEER");
            fp.saveCredentials(creds);
        } else {
            this.creds = (CredentialsStore) saved;
        }
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
        fp.saveCredentials(creds);
    }

    // crea un nuovo configuratore (amministratore)
    public void createConfigurator(String username, char[] password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username non valido per la creazione del configuratore");
        }
        if (creds.getUsers().containsKey(username)) {
            throw new IllegalArgumentException("Username già esistente: " + username);
        }
        creds.putNewUser(username, password, false, "ADMIN");
        fp.saveCredentials(creds);
    }

    // === NUOVO: crea un volontario ===
    public void createVolunteer(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username non valido per la creazione del volontario");
        }
        if (creds.getUsers().containsKey(username)) {
            throw new IllegalArgumentException("Username già esistente: " + username);
        }
        creds.putNewUser(username, DEFAULT_VOLUNTEER_PASSWORD.toCharArray(), true, "VOLUNTEER");
        fp.saveCredentials(creds);
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
    }

    // === versione 4 ===
    public void createFruitore(String username, char[] password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username non valido per la creazione del fruitore");
        }
        if (creds.getUsers().containsKey(username)) {
            throw new IllegalArgumentException("Username già esistente: " + username);
        }
        creds.putNewUser(username, password, false, "FRUITORE");
        fp.saveCredentials(creds);
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
}
