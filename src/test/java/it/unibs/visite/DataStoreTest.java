package it.unibs.visite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibs.visite.model.DataStore;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.model.Volontario;

public class DataStoreTest {
    private DataStore ds;

    @BeforeEach
    void setUp() throws Exception {
        ds = new DataStore();
    }

    @Test 
    void testInitialDataStoreIsEmpty() {
        assertNotNull(ds, "DataStore should be initialized");
        assertTrue(ds.getLuoghi().isEmpty(), "Luoghi map should be empty on initialization");
        assertTrue(ds.getTipiVisita().isEmpty(), "TipiVisita map should be empty on initialization");
        assertTrue(ds.getVolontari().isEmpty(), "Volontari map should be empty on initialization");
        assertTrue(ds.getAllPreclusioni().isEmpty(), "Preclusioni map should be empty on initialization");
        assertTrue(ds.getVisite().isEmpty(), "Visite list should be empty on initialization");
    }

    @Test
    void testAddAndGetLuogo() {
        Luogo luogo = new Luogo("Museo Storico", "Via Roma 1");
        ds.addLuogo(luogo);
        assertEquals(luogo, ds.getLuogo(luogo.getId()), "Retrieved Luogo should match the added one");
    }

    @Test
    void testUniqueVolontarioNickname() {
        Volontario v1 = new Volontario("volontario1");
        ds.addVolontario(v1);
        assertThrows(RuntimeException.class, () -> ds.addVolontario(new Volontario("volontario1")));
    }

    
}