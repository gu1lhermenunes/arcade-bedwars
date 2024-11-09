package com.minecraft.bedwars.util.visibility;

import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Visibility {

    public static void refresh(Player player) {

        User user = User.fetch(player.getUniqueId());
        Room room = user.getRoom();

        if (room == null) {
            hideAll(player);
            return;
        }

        boolean shouldHide = room.isSpectator(user);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.getWorld().getUID().equals(player.getWorld().getUID())) {
                player.hidePlayer(other);
                other.hidePlayer(player);
            } else {
                User otherUser = User.fetch(other.getUniqueId());

                if (shouldHide) {
                    other.hidePlayer(player);
                }

                if (room.isSpectator(otherUser)) {
                    player.hidePlayer(other);
                    continue;
                }

                player.showPlayer(other);

                if (!shouldHide)
                    other.showPlayer(player);
            }
        }
    }

    public static void refreshWorld(Player player) {

        User user = User.fetch(player.getUniqueId());
        Room room = user.getRoom();

        if (room == null) {
            hideAll(player);
            return;
        }

        boolean shouldHide = room.isSpectator(user);

        for (Player other : player.getWorld().getPlayers()) {
            User otherUser = User.fetch(other.getUniqueId());

            if (shouldHide) {
                other.hidePlayer(player);
            }

            if (room.isSpectator(otherUser)) {
                player.hidePlayer(other);
                continue;
            }

            player.showPlayer(other);

            if (!shouldHide)
                other.showPlayer(player);
        }
    }

    public static void hideAll(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.hidePlayer(player);
            player.hidePlayer(other);
        }
    }
}
