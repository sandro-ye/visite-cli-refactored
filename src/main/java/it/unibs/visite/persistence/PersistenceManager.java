package it.unibs.visite.persistence;

import java.nio.file.*;
import it.unibs.visite.repository.memory.*;

public class PersistenceManager {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path VOLONTARI_PATH = DATA_DIR.resolve("volontari.ser");
    private static final Path VISITE_REPOSITORY_PATH = DATA_DIR.resolve("visite_repository.ser");
    private static final Path ARCHIVIO_VISITE_REPOSITORY_PATH = DATA_DIR.resolve("archivio_visite_repository.ser");
    private static final Path PARAMETRI_SISTEMA_PATH = DATA_DIR.resolve("parametri_sistema.ser");
    private static final Path FRUITORI_PATH = DATA_DIR.resolve("fruitori.ser");
    private static final Path LUOGHI_PATH = DATA_DIR.resolve("luoghi.ser");
    private static final Path TIPI_VISITA_PATH = DATA_DIR.resolve("tipi_visita.ser");
    private static final Path PRECLUSIONI_PATH = DATA_DIR.resolve("preclusioni.ser");

    private final InMemoryVolontarioRepository volontarioRepository;
    private final InMemoryFruitoreRepository fruitoreRepository;
    private final InMemoryLuogoRepository luogoRepository;
    private final InMemoryTipoVisitaRepository tipoVisitaRepository;
    private final InMemoryVisitaRepository visitaRepository;
    private final InMemoryVisitaRepository archivioVisiteRepository;
    private final InMemoryPreclusioneRepository preclusioneRepository;
    private final InMemoryParametriSistemaRepository parametriSistemaRepository;

    public PersistenceManager() {
        creaCartellaData();

        this.volontarioRepository = FileRepositoryPersistence.caricaOggetto(VOLONTARI_PATH, InMemoryVolontarioRepository::new);
        this.visitaRepository = FileRepositoryPersistence.caricaOggetto(VISITE_REPOSITORY_PATH, InMemoryVisitaRepository::new);
        this.archivioVisiteRepository = FileRepositoryPersistence.caricaOggetto(ARCHIVIO_VISITE_REPOSITORY_PATH, InMemoryVisitaRepository::new);
        this.parametriSistemaRepository = FileRepositoryPersistence.caricaOggetto(PARAMETRI_SISTEMA_PATH, InMemoryParametriSistemaRepository::new);
        this.fruitoreRepository = FileRepositoryPersistence.caricaOggetto(FRUITORI_PATH, InMemoryFruitoreRepository::new);
        this.luogoRepository = FileRepositoryPersistence.caricaOggetto(LUOGHI_PATH, InMemoryLuogoRepository::new);
        this.tipoVisitaRepository = FileRepositoryPersistence.caricaOggetto(TIPI_VISITA_PATH, InMemoryTipoVisitaRepository::new);
        this.preclusioneRepository = FileRepositoryPersistence.caricaOggetto(PRECLUSIONI_PATH, InMemoryPreclusioneRepository::new);
    }

    public void saveAll() {
        FileRepositoryPersistence.salvaOggetto(volontarioRepository, VOLONTARI_PATH);
        FileRepositoryPersistence.salvaOggetto(visitaRepository, VISITE_REPOSITORY_PATH);
        FileRepositoryPersistence.salvaOggetto(archivioVisiteRepository, ARCHIVIO_VISITE_REPOSITORY_PATH);
        FileRepositoryPersistence.salvaOggetto(parametriSistemaRepository, PARAMETRI_SISTEMA_PATH);
        FileRepositoryPersistence.salvaOggetto(fruitoreRepository, FRUITORI_PATH);
        FileRepositoryPersistence.salvaOggetto(luogoRepository, LUOGHI_PATH);
        FileRepositoryPersistence.salvaOggetto(tipoVisitaRepository, TIPI_VISITA_PATH);
        FileRepositoryPersistence.salvaOggetto(preclusioneRepository, PRECLUSIONI_PATH);
    }

    //getter per i repository
    public InMemoryVolontarioRepository getVolontarioRepository() { return volontarioRepository; }
    public InMemoryVisitaRepository getVisitaRepository() { return visitaRepository; }
    public InMemoryVisitaRepository getArchivioVisiteRepository() { return archivioVisiteRepository; }
    public InMemoryParametriSistemaRepository getParametriSistemaRepository() { return parametriSistemaRepository; }
    public InMemoryFruitoreRepository getFruitoreRepository() { return fruitoreRepository; }
    public InMemoryLuogoRepository getLuogoRepository() { return luogoRepository; }
    public InMemoryTipoVisitaRepository getTipoVisitaRepository() { return tipoVisitaRepository; }
    public InMemoryPreclusioneRepository getPreclusioneRepository() { return preclusioneRepository; }

    private void creaCartellaData() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossibile creare cartella data", e);
        }
    }
}
