package it.unibs.visite;

import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    
    private Path tempDir;
    private AuthService auth;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("visite-test");
        auth = new AuthService(new FilePersistence(tempDir));
    }

    @Test
    void testLoginDefaultAdmin() {
        assertTrue(auth.login("admin", "admin".toCharArray()), "login con credenziali di default (admin/admin) dovrebbe riuscire");
        assertTrue(auth.mustChangePassword("admin"), "admin dovrebbe essere forzato a cambiare password al primo accesso");
    }

    @Test
    void testChangePassword() {
        auth.changePassword("admin", "nuovaPassword".toCharArray());

        assertTrue(auth.login("admin", "nuovaPassword".toCharArray()), "login con nuova password dovrebbe riuscire");
        assertFalse(auth.login("admin", "admin".toCharArray()), "login con vecchia password non dovrebbe riuscire");
        assertFalse(auth.mustChangePassword("admin"), "admin non dovrebbe essere più forzato a cambiare password");
    }
}
