package it.unibs.visite.security;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * classe per memorizzazione credenziali utenti
 * - permette inserimento nuovo utente con password
 * - verifica credenziali
 * - cambio password
 * - rimozione credenziali utente
 */


/*
sistema metodo rimuovi credenziali utente per single responsibility principle (si sta occupando anche di interazione 
con utente, che non dovrebbe fare)
*/

public class CredentialsStore implements Serializable {
    public static class Entry implements Serializable {
        public String username;
        public String saltB64;
        public String hashB64;
        public boolean mustChangePassword;
        public String role;
    }
    private final Map<String, Entry> users = new HashMap<>();

    public Map<String, Entry> getUsers() { return users; }

    public void putNewUser(String username, char[] password, boolean mustChange, String role) {
        if (users.containsKey(username)) throw new IllegalArgumentException("Username già esistente");
        byte[] salt = PasswordHasher.newSalt();
        Entry e = new Entry();
        e.username = username;
        e.saltB64 = Base64.getEncoder().encodeToString(salt);
        e.hashB64 = PasswordHasher.hash(password, salt);
        e.mustChangePassword = mustChange;
        e.role = role;
        users.put(username, e);
    }

    public boolean verify(String username, char[] password) {
        Entry e = users.get(username);
        if (e == null) return false;
        byte[] salt = Base64.getDecoder().decode(e.saltB64);
        String hash = PasswordHasher.hash(password, salt);
        return hash.equals(e.hashB64);
    }

    public void changePassword(String username, char[] newPassword) {
        Entry e = users.get(username);
        if (e == null) throw new IllegalArgumentException("Utente inesistente");
        byte[] salt = PasswordHasher.newSalt();
        e.saltB64 = Base64.getEncoder().encodeToString(salt);
        e.hashB64 = PasswordHasher.hash(newPassword, salt);
        e.mustChangePassword = false;
    }

    //Per rimuovere delle credenziali:
    public void rimuoviCredenziali(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username non valido per la rimozione delle credenziali");
        }

        if (!users.containsKey(username)) {
            System.out.println("Nessun utente con username '" + username + "' trovato, nessuna rimozione eseguita.");
            return;
        }

        users.remove(username);
        System.out.println("Credenziali dell'utente '" + username + "' rimosse con successo.");
    }
}
