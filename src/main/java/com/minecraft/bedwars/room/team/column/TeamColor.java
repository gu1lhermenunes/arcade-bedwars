package com.minecraft.bedwars.room.team.column;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

@Getter
@RequiredArgsConstructor
public enum TeamColor {
    RED("V", "Vermelho", ChatColor.RED, Color.fromRGB(255,0,0), DyeColor.RED),
    BLUE("A", "Azul", ChatColor.BLUE, Color.fromRGB(0,0,255), DyeColor.BLUE),
    YELLOW("A", "Amarelo", ChatColor.YELLOW, Color.fromRGB(255,255,0), DyeColor.YELLOW),
    GREEN("V", "Verde", ChatColor.GREEN, Color.fromRGB(0,255,0), DyeColor.GREEN),
    GRAY("C", "Cinza", ChatColor.DARK_GRAY, Color.fromRGB(105,105,105), DyeColor.GRAY),
    PINK("R", "Rosa", ChatColor.LIGHT_PURPLE, Color.fromRGB(255,192,203), DyeColor.PINK),
    WHITE("B", "Branco", ChatColor.WHITE, Color.fromRGB(255,255,255), DyeColor.WHITE),
    AQUA("C", "Ciano", ChatColor.AQUA, Color.fromRGB(173,216,230), DyeColor.CYAN);

    private final String charAt;
    private final String display;
    private final ChatColor chatColor;
    private final Color color;
    private final DyeColor dyeColor;
}
