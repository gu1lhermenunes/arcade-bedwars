package com.minecraft.bedwars.util;

import com.minecraft.core.Constants;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkAPI {

    public static void random(Location location) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = firework.getFireworkMeta();

        int rt = Constants.RANDOM.nextInt(4) + 1;

        FireworkEffect.Type type;
        if (rt == 1) {
            type = FireworkEffect.Type.BALL;
        } else if (rt == 2) {
            type = FireworkEffect.Type.BALL_LARGE;
        } else if (rt == 3) {
            type = FireworkEffect.Type.BURST;
        } else {
            type = FireworkEffect.Type.STAR;
        }
        FireworkEffect effect = FireworkEffect.builder().flicker(Constants.RANDOM.nextBoolean()).withColor(Color.WHITE)
                .withColor(Color.ORANGE).withFade(Color.FUCHSIA).with(type).trail(Constants.RANDOM.nextBoolean()).build();
        fwm.addEffect(effect);
        fwm.setPower(Constants.RANDOM.nextInt(2) + 1);
        firework.setFireworkMeta(fwm);
    }
}
