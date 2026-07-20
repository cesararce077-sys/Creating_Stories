package dev.jearchaeologyintegrations;

import java.util.List;

public record ArchaeologyMapping(String requiredMod, String lootTable, String locationKey, Ground ground) {
    public enum Ground { SAND, GRAVEL }

    public static final List<ArchaeologyMapping> ALL = List.of(
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_sand_desert_pyramid", "idas_desert_pyramid_sand", Ground.SAND),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_gravel_desert_pyramid", "idas_desert_pyramid_gravel", Ground.GRAVEL),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_sand_dig_site", "idas_dig_site_sand", Ground.SAND),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_gravel_dig_site", "idas_dig_site_gravel", Ground.GRAVEL),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_sand_labyrinth", "idas_labyrinth", Ground.SAND),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_gravel_train_ruins", "idas_train_ruins", Ground.GRAVEL),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_gravel_mining", "idas_mining", Ground.GRAVEL),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_gravel_snifferhenge", "idas_snifferhenge", Ground.GRAVEL),
        new ArchaeologyMapping("idas", "idas:archeology/suspicious_gravel_surface", "idas_surface", Ground.GRAVEL),
        new ArchaeologyMapping("integrated_villages", "integrated_villages:archeology/suspicious_sand_oasis_village", "integrated_villages_oasis", Ground.SAND),
        new ArchaeologyMapping("integrated_villages", "integrated_villages:archeology/suspicious_gravel_surface", "integrated_villages_surface", Ground.GRAVEL)
    );
}
