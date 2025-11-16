package it.unibs.visite.security;

import it.unibs.visite.persistence.FilePersistence;

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

    public boolean login(String username, char[] password) {
        return creds.verify(username, password);
    }

    public boolean mustChangePassword(String username) {
        CredentialsStore.Entry e = creds.getUsers().get(username);
        return e != null && e.mustChangePassword;
    }

    public void changePassword(String username, char[] newPassword) {
        creds.changePassword(username, newPassword);
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

    public boolean isFirstLoginFlagForVolunteer(String nickname) {
    // riutilizza la stessa informazione che già uso per forzare il cambio password
    return mustChangePassword(nickname);
    }

    public void rimuoviCredenziali(String nickname) {
    if (nickname == null || nickname.isBlank()) {
        throw new IllegalArgumentException("Nickname non valido per la rimozione delle credenziali");
    }

    creds.rimuoviCredenziali(nickname);
    }

}
