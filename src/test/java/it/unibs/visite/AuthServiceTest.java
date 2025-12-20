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

    @Test
    void testCreateAndLoginVolunteer() {
        auth.createVolunteer("volontario1", "passVolontario".toCharArray());

        assertTrue(auth.login("volontario1", "passVolontario".toCharArray()), "login con credenziali del volontario dovrebbe riuscire");
        assertTrue(auth.mustChangePassword("volontario1"), "volontario dovrebbe essere forzato a cambiare password al primo accesso");
        assertTrue(auth.isVolunteer("volontario1"), "volontario1 dovrebbe essere riconosciuto come VOLUNTEER");
    }

    @Test
    void testRemoveCredentials() {
        auth.createVolunteer("volontario2", "passVolontario2".toCharArray());
        assertTrue(auth.login("volontario2", "passVolontario2".toCharArray()), "login con credenziali del volontario dovrebbe riuscire");

        auth.rimuoviCredenziali("volontario2");
        assertFalse(auth.login("volontario2", "passVolontario2".toCharArray()), "login dopo rimozione credenziali non dovrebbe riuscire");
    }

    @Test
    void testInvalidRemoveCredentials() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            auth.rimuoviCredenziali(null);
        });
        assertEquals("Nickname non valido per la rimozione delle credenziali", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> {
            auth.rimuoviCredenziali("");
        });
        assertEquals("Nickname non valido per la rimozione delle credenziali", exception.getMessage());
    }
}
