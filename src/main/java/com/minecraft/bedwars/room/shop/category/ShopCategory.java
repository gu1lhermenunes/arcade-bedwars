package com.minecraft.bedwars.room.shop.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter @RequiredArgsConstructor
public enum ShopCategory {

    SHOP("Compra rápida", Material.NETHER_STAR),
    BLOCK("Blocos", Material.STAINED_CLAY),
    MELEE("Combate", Material.DIAMOND_SWORD),
    ARMOR("Armaduras", Material.CHAINMAIL_LEGGINGS),
    TOOLS("Ferramentas", Material.GOLD_PICKAXE),
    RANGED("Artilharia", Material.BOW),
    POTIONS("Poções", Material.POTION),
    UTILITY("Utilitários", Material.TNT);

    private final String display;
    private final Material material;
}
