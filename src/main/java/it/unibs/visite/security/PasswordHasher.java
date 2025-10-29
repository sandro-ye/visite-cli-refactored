package it.unibs.visite.security;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_LEN = 16;
    private static final int ITER = 120_000;
    private static final int KEYLEN = 256;

    public static byte[] newSalt() {
        byte[] s = new byte[SALT_LEN]; RNG.nextBytes(s); return s;
    }
    public static String hash(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITER, KEYLEN);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            throw new RuntimeException("Hashing error", e);
        }
    }
}
