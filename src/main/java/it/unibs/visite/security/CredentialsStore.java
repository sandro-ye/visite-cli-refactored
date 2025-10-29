package it.unibs.visite.security;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
}
