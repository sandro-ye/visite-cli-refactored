package it.unibs.visite.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import it.unibs.visite.model.*;
import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.service.ConfigService;

public class SeedDemo {
    public static void main(String[] args) {
        Path baseDir = Paths.get(System.getProperty("user.home"), ".visite-cli");
        FilePersistence persistence = new FilePersistence(baseDir);
        ConfigService configService = new ConfigService(persistence);

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
        List<Visita> visiteDemo = Arrays.asList(
                new Visita(tv1.getId(), LocalDate.now().plusDays(5), StatoVisita.PROPOSTA),
                new Visita(tv2.getId(), LocalDate.now().plusDays(10), StatoVisita.CONFERMATA),
                new Visita(tv3.getId(), LocalDate.now().plusDays(15), StatoVisita.COMPLETA),
                new Visita(tv1.getId(), LocalDate.now().plusDays(20), StatoVisita.CANCELLATA)
        );

        visiteDemo.get(0).setVolontarioAssegnato("anna");
        visiteDemo.get(1).setVolontarioAssegnato("mario");
        visiteDemo.get(2).setVolontarioAssegnato("lucia");

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
