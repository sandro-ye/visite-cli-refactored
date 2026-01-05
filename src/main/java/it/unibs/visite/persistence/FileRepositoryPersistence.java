package it.unibs.visite.persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class FileRepositoryPersistence {

    public static <T extends Serializable> T caricaOggetto(Path filename, Supplier<T> defaultSupplier) {
        // Implementazione del caricamento da file
        try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filename))) {
            Object obj = ois.readObject();
            return (T) obj;
        } catch(NoSuchFileException e) { 
            T defaultObject = defaultSupplier.get();
            salvaOggetto(defaultObject, filename);
            return defaultObject;
        } catch (Exception e) {
            throw new RuntimeException("errore durante caricamento oggetto da file: " + filename, e);
        }
    }

    public static void salvaOggetto(Object repository, Path filename) {
        // Implementazione del salvataggio su file
        try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filename))) {
            oos.writeObject(repository);
        } catch (Exception e) {
            throw new RuntimeException("errore durante salvataggio oggetto su file: " + filename, e);
        }
    }
}
