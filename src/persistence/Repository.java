package persistence;

import exception.ValidationException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Dependency Inversion Principle: the Model and Controller layers depend on this
 * abstraction. Swapping file storage for a database later means writing a new
 * implementation, not editing the controllers.
 */
public interface Repository<T> {

    void add(T item) throws ValidationException;

    boolean update(T item);

    boolean deleteById(String id);

    Optional<T> findById(String id);

    List<T> findAll();

    int count();

    void save() throws IOException;

    void load() throws IOException;
}
