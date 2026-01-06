package it.unibs.visite.security;

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
 */

public class AuthService {
    private final FilePersistence fp;
    private CredentialsStore creds;

    private static final String pswrdDefaultVolontario = "volontario";
    public String getPswrdDefaultVolontario() { return pswrdDefaultVolontario; }

    public AuthService(FilePersistence fp) {
        this.fp = fp;

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

    public CredentialsStore getCredentialsStore() { return creds; }

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
        creds.putNewUser(username, password, false, "ADMIN");
        fp.saveCredentials(creds);
    }

    // === NUOVO: crea un volontario ===
    public void createVolunteer(String username, char[] password) {
        // volontario NON è obbligato al cambio password al primo accesso? dipende da regole.
        // se vuoi forzarlo al primo accesso: metti true al posto di false
        creds.putNewUser(username, password, true, "VOLUNTEER");
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

    public void rimuoviCredenziali(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname non valido per la rimozione delle credenziali");
        }
        creds.rimuoviCredenziali(nickname);
    }

    // === versione 4 ===
    public void createFruitore(String username, char[] password) {
        creds.putNewUser(username, password, false, "FRUITORE");
        fp.saveCredentials(creds);
    }

    // === NUOVO: controlla ruolo fruitore ===
    public boolean isFruitore(String username) {
        CredentialsStore.Entry e = creds.getUsers().get(username);
        return e != null && "FRUITORE".equalsIgnoreCase(e.role);
    }
}
