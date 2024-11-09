package com.minecraft.bedwars.util;

import com.minecraft.bedwars.Bedwars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Nunes
 * Github: https://github.com/heynunes
 */
public class Battle {

    public static HashMap<UUID, UUID> combatLogs = new HashMap<>();

    public static void setCombat(Player player, Player target) {
        combatLogs.put(player.getUniqueId(), target.getUniqueId());
        combatLogs.put(target.getUniqueId(), player.getUniqueId());
        checkCombat(player);
        checkCombat(target);
        System.out.println(player.getName() + " adicionado em combate! [ALERT]");
    }

    public static void removeCombat(Player player) {
        if (inCombat(player)) {
            combatLogs.remove(player.getUniqueId());
            System.out.println(player.getName() + " removido do combate! [ALERT]");
        }
    }

    public static Player getOpponent(Player player) {
        UUID opponentUUID = combatLogs.get(player.getUniqueId());
        if (opponentUUID != null) {
            return Bukkit.getPlayer(opponentUUID);
        }
        return null;
    }

    public static void checkCombat(Player player) {
        new BukkitRunnable() {
            int i = 20;
            @Override
            public void run() {
                if (i > 0) {
                    if (i == 0) {
                        removeCombat(player);;
                        cancel();
                    }
                    i--;
                }

            }
        }.runTaskTimer(Bedwars.getInstance(), 20L, 20L);
    }

    public static boolean inCombat(Player player) {
        if (!combatLogs.containsKey(player.getUniqueId()))
            return false;
        System.out.println(player.getName() + " está em combate! [ALERT]");
        return true;
    }
}