package com.creatingstories.workshop.miapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolCompatibilityPolicyTest {
    @Test void ordinaryFamiliesAreNarrowlyAllowed() {
        assertEquals(ToolCompatibilityPolicy.Classification.ORDINARY, classify("deeperdarker:resonarium_pickaxe"));
        assertEquals(ToolCompatibilityPolicy.Classification.ORDINARY, classify("iceandfire:silver_sword"));
        assertEquals(ToolCompatibilityPolicy.Classification.ORDINARY, classify("create_sa:rose_quartz_hoe"));
        assertEquals(ToolCompatibilityPolicy.Classification.ORDINARY, classify("cataclysm:black_steel_axe"));
    }

    @Test void identityBearingEquipmentIsNeverAutomaticallyConverted() {
        assertEquals(ToolCompatibilityPolicy.Classification.SPECIAL, classify("iceandfire:dragonsteel_fire_sword"));
        assertEquals(ToolCompatibilityPolicy.Classification.SPECIAL, classify("malum:soul_stained_steel_sword"));
        assertEquals(ToolCompatibilityPolicy.Classification.EXCLUDED, classify("cataclysm:the_incinerator"));
        assertEquals(ToolCompatibilityPolicy.Classification.EXCLUDED, classify("mowziesmobs:wrought_axe"));
    }

    private static ToolCompatibilityPolicy.Classification classify(String id) {
        String[] parts = id.split(":", 2);
        return ToolCompatibilityPolicy.classify(parts[0], parts[1]);
    }
}
