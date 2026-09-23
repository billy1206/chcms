package util;

import java.util.Comparator;
import java.util.List;

/**
 * Task 8 - sorting algorithms, hand written (no Collections.sort).
 * A Comparator is injected instead of hard-coding the sort key, so new sort
 * orders can be added without editing this class (Open/Closed Principle).
 */
public final class SortUtil {

    private SortUtil() { }

    /** Bubble Sort - O(n^2), stable, with an early exit when already sorted. */
    public static <T> void bubbleSort(List<T> items, Comparator<T> comparator) {
        int n = items.size();
        for (int pass = 0; pass < n - 1; pass++) {
            boolean swapped = false;
            for (int i = 0; i < n - 1 - pass; i++) {
                if (comparator.compare(items.get(i), items.get(i + 1)) > 0) {
                    swap(items, i, i + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                return;
            }
        }
    }

    /** Quick Sort - average O(n log n), in place, Lomuto partition. */
    public static <T> void quickSort(List<T> items, Comparator<T> comparator) {
        quickSort(items, comparator, 0, items.size() - 1);
    }

    private static <T> void quickSort(List<T> items, Comparator<T> comparator, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(items, comparator, low, high);
            quickSort(items, comparator, low, pivotIndex - 1);
            quickSort(items, comparator, pivotIndex + 1, high);
        }
    }

    private static <T> int partition(List<T> items, Comparator<T> comparator, int low, int high) {
        T pivot = items.get(high);
        int boundary = low - 1;
        for (int i = low; i < high; i++) {
            if (comparator.compare(items.get(i), pivot) <= 0) {
                boundary++;
                swap(items, boundary, i);
            }
        }
        swap(items, boundary + 1, high);
        return boundary + 1;
    }

    private static <T> void swap(List<T> items, int a, int b) {
        T temp = items.get(a);
        items.set(a, items.get(b));
        items.set(b, temp);
    }
}
