package it.unibs.visite.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import it.unibs.visite.model.*;
import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.security.AuthService;
import it.unibs.visite.service.ConfigService;

public class SeedDemo {
    public static void main(String[] args) {
        Path baseDir = Paths.get(System.getProperty("user.home"), ".visite-cli");
        FilePersistence persistence = new FilePersistence(baseDir);
        AuthService auth = new AuthService(persistence);
        ConfigService configService = new ConfigService(persistence, auth);

        DataStore ds = configService.getSnapshot();

        System.out.println("=== SEED DATI DEMO - VISITE CLI === ");

        // Parametri globali
        ds.getParametri().setAmbitoTerritorialeUnaTantum("Comune di Palermo");
        ds.getParametri().setMaxPersonePerIscrizione(10);

        //volontari
        Volontario v1 = new Volontario("anna");
        Volontario v2 = new Volontario("mario");
        Volontario v3 = new Volontario("lucia");

        ds.addVolontario(v1);
        ds.addVolontario(v2);
        ds.addVolontario(v3);

        //luoghi
        Luogo l1 = new Luogo("Cattedrale di Palermo", "Monumento arabo-normanno");
        Luogo l2 = new Luogo("Teatro Massimo", "Storico teatro dell'opera");
        Luogo l3 = new Luogo("Orto Botanico", "Giardino storico e didattico");

        ds.addLuogo(l1);
        ds.addLuogo(l2);
        ds.addLuogo(l3);

        //tipi visita
        TipoVisita tv1 = new TipoVisita(l1.getId(), "Visita Cattedrale", "Visita guidata alla Cattedrale di Palermo");
        TipoVisita tv2 = new TipoVisita(l2.getId(), "Visita Teatro", "Tour interno del Teatro Massimo");
        TipoVisita tv3 = new TipoVisita(l3.getId(), "Visita Botanica", "Visita didattica all’Orto Botanico");

        //associa volontari
        tv1.addVolontario("anna", ds);
        tv1.addVolontario("mario", ds);

        tv2.addVolontario("mario", ds);
        tv2.addVolontario("lucia", ds);

        tv3.addVolontario("lucia", ds);

        ds.addTipoVisita(tv1);
        ds.addTipoVisita(tv2);
        ds.addTipoVisita(tv3);

        //visite demo con stati diversi
        Visita visita1 = new Visita("visita1", tv1.getId(), LocalDate.now().plusDays(5), 5, 40);
        visita1.setStato(StatoVisita.PROPOSTA);
        Visita visita2 = new Visita("visita2", tv2.getId(), LocalDate.now().plusDays(5), 5, 40);
        visita2.setStato(StatoVisita.CONFERMATA);
        Visita visita3 = new Visita("visita3", tv3.getId(), LocalDate.now().plusDays(5), 5, 40);
        visita3.setStato(StatoVisita.COMPLETA);
        Visita visita4 = new Visita("visita4", tv1.getId(), LocalDate.now().plusDays(5), 5, 40);
        visita4.setStato(StatoVisita.CANCELLATA);
        List<Visita> visiteDemo = Arrays.asList(
                visita1, visita2, visita3, visita4
        );

        visiteDemo.get(0).setVolontarioNickname("anna");
        visiteDemo.get(1).setVolontarioNickname("mario");
        visiteDemo.get(2).setVolontarioNickname("lucia");

        for (Visita v : visiteDemo) {
            ds.addVisita(v);
        }

        //preclusioni demo
        var targetMonth = java.time.YearMonth.now().plusMonths(3);
        ds.addPreclusione(targetMonth, LocalDate.of(targetMonth.getYear(), targetMonth.getMonth(), 10));
        ds.addPreclusione(targetMonth, LocalDate.of(targetMonth.getYear(), targetMonth.getMonth(), 15));

        //persistenza
        configService.save();

        System.out.println("Seed dati demo completato con successo!");
        System.out.println("Cartella dati: " + baseDir.toAbsolutePath());
    }
}
