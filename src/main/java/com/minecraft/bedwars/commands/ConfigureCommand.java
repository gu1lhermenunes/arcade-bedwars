/*
 * Copyright (C) Trydent, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.bedwars.commands;

import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.room.shop.category.ShopCategory;
import com.minecraft.bedwars.user.User;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

public class ConfigureCommand implements BukkitInterface {

    @Command(name = "getblock", platform = Platform.PLAYER, rank = Rank.DEVELOPER_ADMIN)
    public void getblock(Context<Player> context) {
        Player player = context.getSender();
        Location location = player.getLocation();

        World world = location.getWorld();

        int maxDistance = 100;
        Block targetBlock = player.getTargetBlock((Set<Material>) null, maxDistance);

        if (targetBlock != null) {
            Location blockLocation = targetBlock.getLocation();
            double x = blockLocation.getX();
            double y = blockLocation.getY();
            double z = blockLocation.getBlockZ();

            float yaw = Math.round(location.getYaw());

            String loc = "" + world.getName() + ", " + x + ", " + y + ", " + z + ", " + yaw + ", 0";

            TextComponent textComponent = new TextComponent("§ePara copiar a localização do bloco, clique ");
            TextComponent textComponent2 = createTextComponent("§b§lAQUI", HoverEvent.Action.SHOW_TEXT, "§7Clique para copiar.", ClickEvent.Action.SUGGEST_COMMAND, loc);

            player.spigot().sendMessage(textComponent, textComponent2);
        }
    }

    @Command(name = "minplayers", platform = Platform.PLAYER, rank = Rank.DEVELOPER_ADMIN)
    public void minplayers(Context<Player> context, int value) {
        Player player = context.getSender();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null) return;

        room.setMinPlayers(value);
        player.sendMessage("§c* §eValor de 'minplayers' alterado para §f" + value);
    }

    @Command(name = "start", platform = Platform.PLAYER, rank = Rank.DEVELOPER_ADMIN)
    public void start(Context<Player> context) {
        Player player = context.getSender();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null) return;

        room.setMinPlayers(room.getWorld().getPlayers().size());
    }

    @Command(name = "shop", platform = Platform.PLAYER, rank = Rank.DEVELOPER_ADMIN)
    public void shop(Context<Player> context) {
        Player player = context.getSender();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null) return;

      //  new Shop(player, user.getAccount(), ShopCategory.SHOP).openInventory();
    }
}