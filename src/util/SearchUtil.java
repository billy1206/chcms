package util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Task 8 - searching algorithms, hand written (no Collections.binarySearch).
 * Generic so the same code searches patients, doctors or appointments (DRY).
 */
public final class SearchUtil {

    private SearchUtil() { }

    /**
     * Linear Search - O(n). Checks every element, works on unsorted data.
     * Used for keyword search where several records may match.
     */
    public static <T> List<T> linearSearch(List<T> items, Predicate<T> matcher) {
        List<T> results = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            T current = items.get(i);
            if (matcher.test(current)) {
                results.add(current);
            }
        }
        return results;
    }

    /**
     * Binary Search - O(log n). Requires the list to be sorted by the same key.
     * Returns the index of the match, or -1 when not found.
     */
    public static <T, K extends Comparable<K>> int binarySearch(List<T> sortedItems,
                                                                Function<T, K> keyExtractor,
                                                                K target) {
        int low = 0;
        int high = sortedItems.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            K midKey = keyExtractor.apply(sortedItems.get(mid));
            int cmp = midKey.compareTo(target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    /** Convenience wrapper: sorts a copy by the key, then binary searches it. */
    public static <T, K extends Comparable<K>> T findByKey(List<T> items,
                                                           Function<T, K> keyExtractor,
                                                           K target) {
        List<T> copy = new ArrayList<>(items);
        SortUtil.quickSort(copy, Comparator.comparing(keyExtractor));
        int index = binarySearch(copy, keyExtractor, target);
        return index == -1 ? null : copy.get(index);
    }
}
