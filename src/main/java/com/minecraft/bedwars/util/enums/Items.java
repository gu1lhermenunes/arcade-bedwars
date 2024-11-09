/*
 * Copyright (C) Trydent, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.bedwars.util.enums;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.user.User;
import com.minecraft.core.bukkit.server.bedwars.BedwarsType;
import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.translation.Language;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Items {

    PORTUGUESE(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.PAPER).setName("§aProcurar nova partida §7(Clique Direito)").getStack(), execute(player -> {

        if (CooldownProvider.getGenericInstance().hasCooldown(player, "play_again"))
            return;

        User user = User.fetch(player.getUniqueId());

        if (!user.getRouteContext().hasDefinedGame()) {
            user.lobby();
        } else {
            BedwarsType bedwarsType = user.getRouteContext().getGame();
            Room room = Bedwars.getInstance().getRoomStorage().get(bedwarsType);

            if (room == null) {
                player.sendMessage(user.getAccount().getLanguage().translate("arcade.room.not_found"));
                user.lobby();
                return;
            }

            CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "play_again", 1, false);
            room.join(user, user.getRouteContext().getPlayMode(), true);

        }
    })), new InteractableItem(new ItemFactory(Material.BED).setName("§aRetornar ao lobbie §7(Clique Direito)").getStack(), execute(player -> User.fetch(player.getUniqueId()).lobby()))}, new Integer[]{0, 1}),
    ENGLISH(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.PAPER).setName("§aSearch a new match §7(Right Click)").getStack(), execute(player -> {

        if (CooldownProvider.getGenericInstance().hasCooldown(player, "play_again"))
            return;

        User user = User.fetch(player.getUniqueId());

        if (!user.getRouteContext().hasDefinedGame()) {
            user.lobby();
        } else {
            BedwarsType bedwarsType = user.getRouteContext().getGame();
            Room room = Bedwars.getInstance().getRoomStorage().get(bedwarsType);

            if (room == null) {
                player.sendMessage(user.getAccount().getLanguage().translate("arcade.room.not_found"));
                user.lobby();
                return;
            }

            CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "play_again", 1, false);
            room.join(user, user.getRouteContext().getPlayMode(), true);
        }

    })), new InteractableItem(new ItemFactory(Material.BED).setName("§aReturn to lobby §7(Right Click)").getStack(), execute(player -> User.fetch(player.getUniqueId()).lobby()))}, new Integer[]{0, 1});

    private final InteractableItem[] items;
    private final Integer[] slots;

    Items(InteractableItem[] items, Integer[] slots) {
        this.items = items;
        this.slots = slots;
    }

    @Getter
    private static final Items[] values;

    static {
        values = values();
    }

    public InteractableItem getItem(int id) {
        return id <= items.length - 1 ? items[id] : items[0];
    }

    private static InteractableItem.Interact execute(Consumer<Player> playerConsumer) {
        return new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                playerConsumer.accept(player);
                return true;
            }
        };
    }

    public void build(Player player) {
        User user = User.fetch(player.getUniqueId());

        if (user.getRoom().getStage().equals(RoomStage.WAITING)) {
            player.getInventory().setItem(8, getItem(1).getItemStack());
        } else if (user.getRoom().getStage().equals(RoomStage.ENDING))  {
            player.getInventory().setItem(0, getItem(0).getItemStack());
            player.getInventory().setItem(8, getItem(1).getItemStack());
        }
    }

    public void build(Player player, RoomStage stage) {
        User user = User.fetch(player.getUniqueId());

        if (stage == RoomStage.ENDING) {
            player.getInventory().setItem(0, getItem(0).getItemStack());
            player.getInventory().setItem(8, getItem(1).getItemStack());
        } else {
            player.getInventory().setItem(8, getItem(1).getItemStack());
        }
    }

    public static Items find(Language language) {
        return Arrays.stream(getValues()).filter(c -> c.name().equalsIgnoreCase(language.name())).findFirst().orElse(null);
    }

}