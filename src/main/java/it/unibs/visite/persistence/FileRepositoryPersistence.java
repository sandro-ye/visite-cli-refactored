package it.unibs.visite.persistence;

import java.io.*;

public class FileRepositoryPersistence {

    public static <T> T caricaOggetto(String filename, Class<T> clazz) {
        // Implementazione del caricamento da file
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            return clazz.cast(obj);
        } catch (Exception e) {
            throw new RuntimeException("errore durante caricamento oggetto da file: " + filename, e);
        }
    }

    public static void salvaOggetto(Object repository, String filename) {
        // Implementazione del salvataggio su file
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(repository);
        } catch (Exception e) {
            throw new RuntimeException("errore durante salvataggio oggetto su file: " + filename, e);
        }
    }
}
