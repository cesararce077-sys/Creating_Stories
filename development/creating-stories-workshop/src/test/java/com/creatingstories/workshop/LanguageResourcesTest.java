package com.creatingstories.workshop;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LanguageResourcesTest {
    private static final Path EN_US = Path.of(
        "src/main/resources/assets/creating_stories_workshop/lang/en_us.json");

    @Test
    void keepsGameplayAndMenuTranslationsTogether() throws IOException {
        String language = Files.readString(EN_US);

        assertAll(
            () -> assertTrue(language.contains("creating_stories_workshop.salvaged_part_name")),
            () -> assertTrue(language.contains("creating_stories_workshop.salvaged_part_without_material")),
            () -> assertTrue(language.contains("creating_stories_workshop.salvaged_assembly_name")),
            () -> assertTrue(language.contains("creating_stories_workshop.complete_equipment_name")),
            () -> assertTrue(language.contains("creating_stories_workshop.workbench.use_part")),
            () -> assertTrue(language.contains("creating_stories_workshop.menu.changelog_tooltip"))
        );
    }
}
