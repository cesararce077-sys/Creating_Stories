package com.creatingstories.workshop.miapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EquipmentCompatibilityPolicyTest {
    @Test
    void ordinaryRangedVariantsCanEnterTheWorkshop() {
        assertEquals(EquipmentCompatibilityPolicy.Classification.ORDINARY,
            ranged("minecraft:bow"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.ORDINARY,
            ranged("expanded_combat:diamond_cross_bow"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.SPECIAL,
            ranged("expanded_combat:tipped_diamond_arrow"));
    }

    @Test
    void onlyMundaneArmorFamiliesAreAutomatic() {
        assertEquals(EquipmentCompatibilityPolicy.Classification.ORDINARY,
            armor("minecraft:netherite_chestplate"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.ORDINARY,
            armor("deeperdarker:warden_helmet"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.ORDINARY,
            armor("iceandfire:armor_silver_metal_boots"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.ORDINARY,
            armor("create_sa:brass_leggings"));
    }

    @Test
    void signatureArmorAndRangedItemsStayIntact() {
        assertEquals(EquipmentCompatibilityPolicy.Classification.EXCLUDED,
            armor("irons_spellbooks:pyromancer_chestplate"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.EXCLUDED,
            ranged("cataclysm:cursed_bow"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.SPECIAL,
            armor("iceandfire:dragonsteel_fire_helmet"));
        assertEquals(EquipmentCompatibilityPolicy.Classification.SPECIAL,
            armor("create_sa:brass_jetpack_chestplate"));
    }

    private static EquipmentCompatibilityPolicy.Classification ranged(String id) {
        String[] parts = id.split(":", 2);
        return EquipmentCompatibilityPolicy.classify(
            parts[0], parts[1], EquipmentCompatibilityPolicy.Kind.RANGED);
    }

    private static EquipmentCompatibilityPolicy.Classification armor(String id) {
        String[] parts = id.split(":", 2);
        return EquipmentCompatibilityPolicy.classify(
            parts[0], parts[1], EquipmentCompatibilityPolicy.Kind.ARMOR);
    }
}
