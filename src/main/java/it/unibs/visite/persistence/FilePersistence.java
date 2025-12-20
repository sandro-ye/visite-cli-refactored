package it.unibs.visite.persistence;

import it.unibs.visite.model.DataStore;

import java.io.*;
import java.nio.file.*;

/*
classe per persistenza dati su file system
- garantisce l'esistenza della directory dati (datastore e credentials)
- salvataggio atomico(writeAtomic) per evitare corruzione dati (DataStore) e credenziali (CredentialsStore)
*/

public class FilePersistence {
    private final Path baseDir;
    private final Path dataFile;
    private final Path credFile;

    public FilePersistence(Path baseDir) {
        this.baseDir = baseDir;
        this.dataFile = baseDir.resolve("datastore.bin");
        this.credFile = baseDir.resolve("credentials.bin");
        ensureDirs(); // <-- garantisce subito la directory
    }

    public void ensureDirs() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile creare directory dati: " + baseDir, e);
        }
    }

    public void saveData(DataStore ds) { writeAtomic(dataFile, ds); }

    public DataStore loadDataOrNew() {
        if (!Files.exists(dataFile)) return new DataStore();
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(dataFile))) {
            return (DataStore) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Errore caricamento DataStore", e);
        }
    }

    // forse si può sostituire Object con CredentialsStore
    public void saveCredentials(Object creds) { writeAtomic(credFile, creds); }

    public Object loadCredentialsOrNull() {
        if (!Files.exists(credFile)) return null;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(credFile))) {
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Errore caricamento credenziali", e);
        }
    }

    private void writeAtomic(Path file, Object obj) {
        try {
            // garantisci SEMPRE il parent prima di scrivere
            Files.createDirectories(file.getParent());

            Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tmp))) {
                oos.writeObject(obj);
            }

            // prova mossa atomica; se non supportata, fai fallback
            try {
                Files.move(tmp, file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Salvataggio atomico fallito per " + file, e);
        }
    }
}
