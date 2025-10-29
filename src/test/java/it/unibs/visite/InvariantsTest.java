package it.unibs.visite;

import it.unibs.visite.core.DomainException;
import it.unibs.visite.model.DataStore;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Volontario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InvariantsTest {
    
    @Test
    void testTipoVisitaDeveAvereAlmenoUnVolontario() {
        TipoVisita tv = new TipoVisita("luogo1", "titolo1", "descrizione1");
        assertThrows(DomainException.class, tv::ensureInvariants, "eccezione nessun volontario associato");
    }

    @Test
    void testAddVolontarioRispettandoInvariante() {
        TipoVisita tv = new TipoVisita("luogo1", "titolo1", "descrizione1");
        DataStore ds = new DataStore();
        ds.addVolontario(new Volontario("mario"));
        tv.addVolontario("mario", ds); // Passa null per DataStore per ora
        assertDoesNotThrow(tv::ensureInvariants, "invariante rispettata con almeno un volontario");
    }
}
