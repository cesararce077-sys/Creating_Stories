package dev.jearchaeologyintegrations;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ArchaeologyMappingTest {
    @Test
    void allKnownProcessorTablesAreMappedExactlyOnce() {
        assertEquals(11, ArchaeologyMapping.ALL.size());
        Set<String> ids = ArchaeologyMapping.ALL.stream().map(ArchaeologyMapping::lootTable).collect(Collectors.toSet());
        assertEquals(11, ids.size());
        Set<String> locationKeys = ArchaeologyMapping.ALL.stream().map(ArchaeologyMapping::locationKey).collect(Collectors.toSet());
        assertEquals(11, locationKeys.size(), "JEI requires a distinct internal location ID for every table");
    }

    @Test
    void mappingMetadataIsComplete() {
        ArchaeologyMapping.ALL.forEach(mapping -> {
            assertFalse(mapping.requiredMod().isBlank());
            assertFalse(mapping.lootTable().isBlank());
            assertFalse(mapping.locationKey().isBlank());
        });
    }
}
