package dev.reliclootcurator.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class WeightedPickerTest {
    @Test
    void respectsEntryBoundaries() {
        List<Entry> entries = List.of(new Entry("first", 1), new Entry("second", 3), new Entry("third", 2));
        assertEquals("first", WeightedPicker.pick(entries, Entry::weight, 0).name());
        assertEquals("second", WeightedPicker.pick(entries, Entry::weight, 1).name());
        assertEquals("second", WeightedPicker.pick(entries, Entry::weight, 3).name());
        assertEquals("third", WeightedPicker.pick(entries, Entry::weight, 4).name());
        assertEquals(6, WeightedPicker.totalWeight(entries, Entry::weight));
    }

    private record Entry(String name, int weight) {
    }
}
