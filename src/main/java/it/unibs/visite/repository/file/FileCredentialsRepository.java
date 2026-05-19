package it.unibs.visite.repository.file;

import java.nio.file.Path;
import it.unibs.visite.security.CredentialsStore;
import it.unibs.visite.repository.CredentialsRepository;
import it.unibs.visite.persistence.FileRepositoryPersistence;

public class FileCredentialsRepository implements CredentialsRepository {
    private final Path filePath;

    public FileCredentialsRepository(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public CredentialsStore load() {
        return FileRepositoryPersistence.caricaOggetto(
            filePath, 
            CredentialsStore::new
        );
    }

    @Override
    public void save(CredentialsStore creds) {
        FileRepositoryPersistence.salvaOggetto(
            creds, 
            filePath
        );
    }
}