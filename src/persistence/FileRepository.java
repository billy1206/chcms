package persistence;

import exception.ValidationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import model.Identifiable;

/**
 * Generic file-backed repository (Task 7: ArrayList + File I/O).
 * The record format is injected through two functions, so one class serves
 * patients, doctors, appointments and treatments (Don't Repeat Yourself).
 */
public class FileRepository<T extends Identifiable> implements Repository<T> {

    private final Path file;
    private final Function<T, String> serialiser;
    private final Function<String, T> deserialiser;
    private final List<T> items = new ArrayList<>();

    public FileRepository(Path file,
                          Function<T, String> serialiser,
                          Function<String, T> deserialiser) {
        this.file = file;
        this.serialiser = serialiser;
        this.deserialiser = deserialiser;
    }

    @Override
    public void add(T item) throws ValidationException {
        if (item == null) {
            throw new ValidationException("Cannot add an empty record.");
        }
        if (findById(item.getId()).isPresent()) {
            throw new ValidationException("A record with id " + item.getId() + " already exists.");
        }
        items.add(item);
    }

    @Override
    public boolean update(T item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteById(String id) {
        return items.removeIf(item -> item.getId().equals(id));
    }

    @Override
    public Optional<T> findById(String id) {
        for (T item : items) {
            if (item.getId().equals(id)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /** Returns a copy so callers cannot bypass add()/delete() validation. */
    @Override
    public List<T> findAll() {
        return new ArrayList<>(items);
    }

    @Override
    public int count() {
        return items.size();
    }

    @Override
    public void save() throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (T item : items) {
                writer.write(serialiser.apply(item));
                writer.newLine();
            }
        }
    }

    @Override
    public void load() throws IOException {
        items.clear();
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    T item = deserialiser.apply(line);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (RuntimeException ex) {
                    System.err.println("Skipped corrupt record in " + file.getFileName()
                            + " line " + lineNumber + ": " + ex.getMessage());
                }
            }
        }
    }
}
