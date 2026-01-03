package it.unibs.visite.persistence;

import it.unibs.visite.model.VolunteerAvailability;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class FileAvailabilityRepository implements AvailabilityRepository {

    private final Path file;

    public FileAvailabilityRepository(Path file) {
        this.file = file;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<YearMonth, Set<LocalDate>>> loadAll() throws IOException {
        if (!Files.exists(file)) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            return (Map<String, Map<YearMonth, Set<LocalDate>>>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private void saveAll(Map<String, Map<YearMonth, Set<LocalDate>>> data) throws IOException {
        Files.createDirectories(file.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(data);
        }
    }

    @Override
    public Optional<VolunteerAvailability> find(String nickname, YearMonth ym) throws IOException {
        var data = loadAll();
        var set = data.getOrDefault(nickname, Map.of()).get(ym);
        return set == null ? Optional.empty() : Optional.of(new VolunteerAvailability(nickname, ym, set));
    }

    @Override
    public void upsert(VolunteerAvailability availability) throws IOException {
        var data = loadAll();
        data.computeIfAbsent(availability.nickname(), k -> new HashMap<>())
                .put(availability.month(), new HashSet<>(availability.dates()));
        saveAll(data);
    }

    @Override
    public void addDates(String nickname, YearMonth ym, Set<LocalDate> dates) throws IOException {
        var data = loadAll();
        data.computeIfAbsent(nickname, k -> new HashMap<>())
                .computeIfAbsent(ym, k -> new HashSet<>())
                .addAll(dates);
        saveAll(data);
    }

    @Override
    public void removeDate(String nickname, YearMonth ym, LocalDate date) throws IOException {
        var data = loadAll();
        var map = data.get(nickname);
        if (map != null && map.containsKey(ym)) {
            map.get(ym).remove(date);
            saveAll(data);
        }
    }

    @Override
    public Set<LocalDate> getDates(String nickname, YearMonth ym) throws IOException {
        return find(nickname, ym).map(VolunteerAvailability::dates).orElseGet(Set::of);
    }

    public static void clearAll(Path basePath) {
        File dataDir = basePath.toFile();
        if (dataDir.exists() && dataDir.isDirectory()) {
            int deleted = 0;
            for (File f : dataDir.listFiles()) {
                if (f.isFile() && f.getName().endsWith("_availabilities.ser")) {
                    if (f.delete()) deleted++;
                }
            }
            System.out.println("[INFO] Eliminati " + deleted + " file di disponibilità volontari dopo la generazione del piano.");
        } else {
        System.out.println("[INFO] Nessuna disponibilità trovata da eliminare.");
        }
    }

}
