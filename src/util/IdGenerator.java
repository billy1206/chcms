package util;

import java.util.List;
import model.Identifiable;

/** Generates the next sequential id such as P001, D004, A012, T007. */
public final class IdGenerator {

    private IdGenerator() { }

    public static String next(String prefix, List<? extends Identifiable> existing) {
        int max = 0;
        for (Identifiable item : existing) {
            String id = item.getId();
            if (id != null && id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                    // Ids that do not follow the pattern are skipped on purpose.
                }
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }
}
