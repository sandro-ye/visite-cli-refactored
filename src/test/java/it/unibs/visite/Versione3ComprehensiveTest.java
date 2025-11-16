package it.unibs.visite;

import it.unibs.visite.model.*;
import it.unibs.visite.service.*;
import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class Versione3ComprehensiveTest {

    private DataStore ds;
    private ConfigService cs;
    private FilePersistence fp;
    private PlannerService ps;
    private Path tempDir;

    @BeforeEach
    public void setup() throws IOException{
        tempDir = Files.createTempDirectory("visite-test");
        ds = new DataStore();
        fp = new FilePersistence(tempDir);
        AuthService auth = new AuthService(fp);
        cs = new ConfigService(ds, fp, auth);
        ps = new PlannerService(ds);
        // impostare parametri di sistema
        ParametriSistema p = new ParametriSistema();
        p.setAmbitoTerritorialeUnaTantum("Test");
        p.setMaxPersonePerIscrizione(10);
        ds.setParametriSistema(p);
    }

    @AfterEach
    public void cleanup() {
        java.io.File f = new java.io.File("test-data.ser");
        if (f.exists()) f.delete();
    }

    @Test
    public void testGenerazionePianoRispettaVincoli() {
        // Setup: un luogo, due tipi, due volontari
        Luogo l1 = new Luogo("L1", "Luogo1");
        ds.addLuogo(l1);

        Volontario v1 = new Volontario("v1");
        Volontario v2 = new Volontario("v2");
        ds.addVolontario(v1);
        ds.addVolontario(v2);

        // Tipi visita legati al luogo, con almeno un volontario
        TipoVisita tv1 = new TipoVisita(l1.getId(), "Titolo1", "Desc1");
        tv1.addVolontario("v1", ds);
        //tv1.setGiorniSettimana(Set.of(5)); // venerdì
        tv1.setNumeroMinimoPartecipanti(1);
        tv1.setNumeroMassimoPartecipanti(10);

        TipoVisita tv2 = new TipoVisita(l1.getId(), "Titolo2", "Desc2");
        tv2.addVolontario("v2", ds);
        //tv2.setGiorniSettimana(Set.of(5)); // venerdì
        tv2.setNumeroMinimoPartecipanti(1);
        tv2.setNumeroMassimoPartecipanti(10);

        ds.addTipoVisita(tv1);
        ds.addTipoVisita(tv2);

        // Disponibilità
        YearMonth mese = YearMonth.now().plusMonths(1);
        LocalDate d = mese.atDay(5);
        ds.setDisponibilita(mese, "v1", Set.of(d));
        ds.setDisponibilita(mese, "v2", Set.of(d));

        // Preclusione (nessuna)
        ds.setPreclusioni(mese, Collections.emptySet());

        // Esegui generazione piano
        List<Visita> piano = ps.generaPiano(mese);

        assertNotNull(piano);
        assertFalse(piano.isEmpty(), "Piano dovrebbe avere almeno una visita");

        // Verifica vincoli
        for (LocalDate day : Set.of(d)) {
            Set<String> tipi = new HashSet<>();
            Set<String> volontari = new HashSet<>();
            for (Visita v : piano) {
                if (v.getData().equals(day)) {
                    assertTrue(tipi.add(v.getTipoVisitaId()), "Tipo duplicato in giorno " + day);
                    assertTrue(volontari.add(v.getVolontarioNickname()), "Volontario occupato più volte in giorno " + day);
                }
            }
        }
    }


    @Test
    public void testRimozioneVolontarioPropagazione() {
        // Setup con volontario unico per tipo
        Luogo l1 = new Luogo("L1", "Luogo1");
        ds.addLuogo(l1);
        TipoVisita tv1 = new TipoVisita("T1", "Titolo1", "Desc1");
        ds.addTipoVisita(tv1);
        Volontario v1 = new Volontario("v1");
        ds.addVolontario(v1);

        // Associazione volontario -> tipo
        // (già gestita dal costruttore)
        // Rimuovi volontario
        ds.setFaseCorrente(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);
        cs.rimuoviVolontario("v1");
        assertFalse(ds.getVolontari().stream().anyMatch(v->v.getNickname().equals("v1")), "Volontario v1 dovrebbe essere rimosso");
        assertFalse(ds.getTipiVisita().stream().anyMatch(t->t.getId().equals("T1")), "TipoVisita T1 dovrebbe essere rimosso");
        assertFalse(ds.getLuoghi().stream().anyMatch(l->l.getId().equals("L1")), "Luogo L1 dovrebbe essere rimosso");
    }

    @Test
    public void testPreclusioniEscludonoDate() {
        // Setup semplice
        Luogo l1 = new Luogo("L1", "Luogo1");
        ds.addLuogo(l1);
        TipoVisita tv1 = new TipoVisita("T1", "Titolo1", "Desc1");
        ds.addTipoVisita(tv1);
        Volontario v1 = new Volontario("v1");
        ds.addVolontario(v1);
        YearMonth mese = YearMonth.now().plusMonths(1);
        LocalDate blocked = mese.atDay(10);
        ds.setDisponibilita(mese, "v1", Set.of(blocked.plusDays(1)));
        ds.setPreclusioni(mese, Set.of(blocked));
        List<Visita> piano = ps.generaPiano(mese);
        assertTrue(piano.stream().noneMatch(v-> v.getData().equals(blocked)), "Non devono esserci visite in data preclusa");
    }

    @Test
    public void testRiaperturaDisponibilita() {
        YearMonth mese = YearMonth.now().plusMonths(2);
        cs.riapriRaccoltaDisponibilita(mese);
        assertTrue(ds.getDisponibilitaPerMese().containsKey(mese), "Disponibilità per mese i+2 deve esserci");
        assertEquals(AppPhase.RACCOLTA_DISPONIBILITA, ds.getFaseCorrente(), "La fase dopo riapertura dovrebbe essere RACCOLTA_DISPONIBILITA");
    }
}
