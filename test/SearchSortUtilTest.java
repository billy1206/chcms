import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.SearchUtil;
import util.SortUtil;

/**
 * Test class 3 of 3 - util.SortUtil and util.SearchUtil
 * Objective: prove the hand-written Bubble Sort, Quick Sort, Linear Search and
 * Binary Search return correct results, including on empty and single-item lists.
 */
class SearchSortUtilTest {

    private List<Patient> patients;

    private Patient patient(String id, String name, int birthYear) {
        return new Patient(id, name, LocalDate.of(birthYear, 1, 1), "0411222333",
                name.toLowerCase().replace(" ", ".") + "@example.com",
                "212345670" + id.charAt(id.length() - 1), "Melbourne", "None");
    }

    @BeforeEach
    void setUp() {
        patients = new ArrayList<>(Arrays.asList(
                patient("P003", "Charlie Brown", 1990),
                patient("P001", "Alice Nguyen", 2000),
                patient("P004", "Dana White", 1975),
                patient("P002", "Bob Tran", 1985)));
    }

    @Test
    @DisplayName("TC-S01 Bubble Sort orders patients alphabetically by name")
    void bubbleSortOrdersByName() {
        SortUtil.bubbleSort(patients, Comparator.comparing(Patient::getFullName));
        assertEquals("Alice Nguyen", patients.get(0).getFullName());
        assertEquals("Dana White", patients.get(3).getFullName());
    }

    @Test
    @DisplayName("TC-S02 Quick Sort orders patients by id ascending")
    void quickSortOrdersById() {
        SortUtil.quickSort(patients, Comparator.comparing(Patient::getId));
        assertEquals("P001", patients.get(0).getId());
        assertEquals("P004", patients.get(3).getId());
    }

    @Test
    @DisplayName("TC-S03 both algorithms produce the identical order")
    void bothAlgorithmsAgree() {
        List<Patient> bubble = new ArrayList<>(patients);
        List<Patient> quick = new ArrayList<>(patients);
        Comparator<Patient> byName = Comparator.comparing(Patient::getFullName);
        SortUtil.bubbleSort(bubble, byName);
        SortUtil.quickSort(quick, byName);
        for (int i = 0; i < bubble.size(); i++) {
            assertEquals(bubble.get(i).getId(), quick.get(i).getId());
        }
    }

    @Test
    @DisplayName("TC-S04 sorting an empty or single-item list does not fail")
    void handlesEdgeCases() {
        List<Patient> empty = new ArrayList<>();
        List<Patient> single = new ArrayList<>(List.of(patient("P009", "Solo Person", 1999)));
        SortUtil.bubbleSort(empty, Comparator.comparing(Patient::getId));
        SortUtil.quickSort(empty, Comparator.comparing(Patient::getId));
        SortUtil.bubbleSort(single, Comparator.comparing(Patient::getId));
        SortUtil.quickSort(single, Comparator.comparing(Patient::getId));
        assertTrue(empty.isEmpty());
        assertEquals(1, single.size());
    }

    @Test
    @DisplayName("TC-S05 Linear Search returns every partial name match")
    void linearSearchFindsPartialMatches() {
        List<Patient> found = SearchUtil.linearSearch(patients,
                p -> p.getFullName().toLowerCase().contains("tran"));
        assertEquals(1, found.size());
        assertEquals("P002", found.get(0).getId());
    }

    @Test
    @DisplayName("TC-S06 Linear Search returns an empty list when nothing matches")
    void linearSearchReturnsEmptyList() {
        List<Patient> found = SearchUtil.linearSearch(patients,
                p -> p.getFullName().equals("Nobody Here"));
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("TC-S07 Binary Search finds an existing id in a sorted list")
    void binarySearchFindsExistingId() {
        SortUtil.quickSort(patients, Comparator.comparing(Patient::getId));
        int index = SearchUtil.binarySearch(patients, Patient::getId, "P003");
        assertEquals(2, index);
    }

    @Test
    @DisplayName("TC-S08 Binary Search returns -1 for a missing id")
    void binarySearchReturnsMinusOne() {
        SortUtil.quickSort(patients, Comparator.comparing(Patient::getId));
        assertEquals(-1, SearchUtil.binarySearch(patients, Patient::getId, "P999"));
    }

    @Test
    @DisplayName("TC-S09 findByKey sorts first, so it works on unsorted input")
    void findByKeyWorksOnUnsortedList() {
        assertNotNull(SearchUtil.findByKey(patients, Patient::getId, "P004"));
        assertNull(SearchUtil.findByKey(patients, Patient::getId, "P404"));
    }
}
