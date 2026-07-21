package com.creatingstories.workshop.miapi;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalvagedPartNamingTest {
    @Test
    void recognizesOnlyWorkshopOwnedCustomNameKeys() {
        assertTrue(SalvagedPartNameRules.isWorkshopManagedTranslationKey(
            "creating_stories_workshop.salvaged_part_name"));
        assertTrue(SalvagedPartNameRules.isWorkshopManagedTranslationKey(
            "creating_stories_workshop.complete_equipment_name"));
        assertFalse(SalvagedPartNameRules.isWorkshopManagedTranslationKey("item.miapi.modular_sword"));
        assertFalse(SalvagedPartNameRules.isWorkshopManagedTranslationKey("minecraft.diamond_sword"));
    }

    @Test
    void givesCommonPartsFunctionalNames() {
        assertEquals("Sword Handle", SalvagedPartNameRules.partName("tm_arsenal:handle/sword"));
        assertEquals("Sword Blade", SalvagedPartNameRules.partName("tm_arsenal:blade/normal"));
        assertEquals("Pickaxe Head", SalvagedPartNameRules.partName("tm_arsenal:tool/pickaxe_front"));
        assertEquals("Round Pommel", SalvagedPartNameRules.partName("tm_arsenal:pommel/round"));
    }

    @Test
    void derivesReadableFallbackFromUntranslatedMaterialId() {
        assertEquals("Wood", SalvagedPartNameRules.materialName("miapi:wood/wood"));
        assertEquals("Rose Quartz", SalvagedPartNameRules.materialName("creating_stories_workshop:crystal/rose_quartz"));
    }

    @Test
    void namesAHandleRootedSwordTreeAsASwordAssembly() {
        assertEquals("Sword", SalvagedPartNameRules.equipmentName(List.of(
            "tm_arsenal:handle/sword",
            "tm_arsenal:guard/normal",
            "tm_arsenal:blade/normal")));
    }

    @Test
    void preservesSpecializedEquipmentIdentity() {
        assertEquals("Katana", SalvagedPartNameRules.equipmentName(List.of(
            "tm_arsenal:handle/sword",
            "tm_arsenal:blade/katana")));
        assertEquals("Pickaxe", SalvagedPartNameRules.equipmentName(List.of(
            "tm_arsenal:handle/tool",
            "tm_arsenal:tool/pickaxe_front")));
    }

    @Test
    void recognizesCompleteToolsButNotPartialAssemblies() {
        assertEquals("Sword", SalvagedPartNameRules.completeEquipmentName(List.of(
            "tm_arsenal:handle/sword",
            "tm_arsenal:guard/normal",
            "tm_arsenal:blade/normal")));
        assertEquals("Pickaxe", SalvagedPartNameRules.completeEquipmentName(List.of(
            "tm_arsenal:handle/tool",
            "tm_arsenal:tool/pickaxe_front")));
        assertNull(SalvagedPartNameRules.completeEquipmentName(List.of(
            "tm_arsenal:handle/sword",
            "tm_arsenal:guard/normal")));
        assertNull(SalvagedPartNameRules.completeEquipmentName(List.of(
            "tm_arsenal:guard/normal",
            "tm_arsenal:blade/normal")));
    }
}
