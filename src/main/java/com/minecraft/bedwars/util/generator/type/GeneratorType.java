package com.minecraft.bedwars.util.generator.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum GeneratorType {
    IRON(Material.IRON_INGOT, 40),
    GOLD(Material.GOLD_INGOT, 90),
    DIAMOND(Material.DIAMOND, 450),
    EMERALD(Material.EMERALD,600);

    private final Material material;
    private final int tick;
}
