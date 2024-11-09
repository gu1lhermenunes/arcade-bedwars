package com.minecraft.bedwars.util.generator;

import com.minecraft.bedwars.util.generator.type.GeneratorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@Getter @Setter @RequiredArgsConstructor
public class Generator {
    private final GeneratorType type;
    private final Location location;
}

