package com.minecraft.bedwars.util;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.room.island.Island;
import com.minecraft.bedwars.room.shop.category.ShopCategory;
import com.minecraft.bedwars.room.team.Team;
import com.minecraft.bedwars.user.User;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class HandleInfos {

    public void handleNPCs(User user, Island island) {
        Player player = user.getPlayer();

        var room = user.getRoom();
        var interact = room.getInteract();

        Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> {
            /* Island Shop NPC */
            NPC SHOP = new NPC(user.getPlayer(), island.getShopLocation(), getNPCShop());
            SHOP.setInteractExecutor(new NPC.Interact() {
                @Override
                public void handle(Player player, NPC npc, ClickType type) {
                //    new Shop(player, Account.fetch(player.getUniqueId()), ShopCategory.SHOP).openInventory();
                }
            });
            SHOP.clone(player).spawn(true);

            Hologram shop = new Hologram(player, SHOP.getLocation().clone().add(0, 2.1, 0), island.getTeam().getColor().getChatColor() + "§lLOJA", "§7Clique para ver mais.");
            shop.setInteract(interact);
            shop.show();

            /* Island Upgrade NPC */
            new NPC(user.getPlayer(), island.getUpgradeLocation(), getNPCShop()).spawn(true);


            new Hologram(user.getPlayer(), new Location(island.getUpgradeLocation().getWorld(), island.getUpgradeLocation().getX(), island.getUpgradeLocation().getY() + 2.1, island.getUpgradeLocation().getZ()), island.getTeam().getColor().getChatColor() + "§lMELHORIAS", "§7Clique para ver mais.").show();
        }, (user.getAccount().getVersion() >= 47 ? 0 : 5));
    }

    public Property getNPCShop() {
        return new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxMzE1ODAwNjU5NywKICAicHJvZmlsZUlkIiA6ICI1NjY3NWIyMjMyZjA0ZWUwODkxNzllOWM5MjA2Y2ZlOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVJbmRyYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZDllNGZkYmUyNjU5NWU2YmRjZDFlZjM0OTNlOTc2NmU4MDkzZjMwMmE1Y2E0MTdlM2U0YTBhZTg2ZTA4MGU2IgogICAgfQogIH0KfQ==", "v/l+NhrQrz04aYhPRlEqIhNAETWtccj3vLykNKz33XPpZ6prHQbVTl71js3M9mRfiIBRjSeRw6Y06qxuScdAdSr7YOAOIHVAc5I31ALJ+1FqQ7W7UIE68fQvbxyE5X4I+NCxcE9hRD7pXhHH8Z76bdLNry+KseRuE5vRF/KF9e1GYES3htHPMFbWs7q0re4aur9c1UHLUWEeX3XsVuaVuh4cf/QmAi+4F/Pz+SB5Luq8P11CimV/BkUlOR4NvDZil75TGueeTJmgD8ixoL4MFUo0GfSjGsN2KpuP8VpAmZpjDbs70xStODQZrNDrq03SUDvj45NXFksWydbHa4bxcogOkXzy8mKwJM57WcqbYNm1OyY0qntJCqao12Vuhj8i8W/IyDPICz1vJTsHsLgxfMAEaZ7oULNlzYisn5uMv1EHqu4j0VFWfyW44x+8TucdicFwMKm/c+x6WgLw5ePDiGQFuxJjJIlSqLPTon3LcgKLLzdtlhxFnEmzQRu0Q8arxCRO6sE7XzsxnTbLD7cBrmUgqKVcpElPV3Dcx0Ogk/h4/2Bi34HKBsyncQ24cp+fQElFRDdrqUoMEMzSnfuuvUEEaC+38bcfmV6IymYQSWZAv+nLdzQsAAVWpCoUW7X2YhJuHrwOBDR9cmKuU7xx2+wtvtmTjLKX8DMzKeDaCUg=");
    }

    public void BedBreakAnnounce(User user, Team teamBreaked) {
        Room room = user.getRoom();

        if (room == null)
            return;

        room.getWorld().getPlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
            player.sendMessage("");
            player.sendMessage("§f§lCAMA DESTRUIDA > §7A " + teamBreaked.getColor().getChatColor() + "cama do " + teamBreaked.getDisplay() + " §7foi destruida por " + user.getTeam().getColor().getChatColor() + user.getName() + "§7.");
            player.sendMessage("");
        });
    }

    public void teamElimininateAnnounce(User user, Team teamBreaked) {
        Room room = user.getRoom();

        if (room == null)
            return;

        room.getWorld().getPlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.WITHER_DEATH, 1.0F, 1.0F);
            player.sendMessage("");
            player.sendMessage("§f§lTIME ELIMINADO > §cO " + teamBreaked.getColor().getChatColor() + "Time " + teamBreaked.getDisplay() + "§c foi eliminado.");
            player.sendMessage("");
        });
    }

    public int countPlayerMaterial(Inventory inventory, Material value) {
        int count = 0;
        for (ItemStack i : inventory.getContents()) {
            if (i != null && i.getType() == value) {
                count += i.getAmount();
            }
        }
        return count;
    }

    public int removeItemOnQuantity(Inventory inventory, Material material, int quantity) {
        int r = quantity;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() != material) {
                continue;
            }

            if (r >= stack.getAmount()) {
                r -= stack.getAmount();
                inventory.clear(i);
            } else if (r > 0) {
                stack.setAmount(stack.getAmount() - r);
                r = 0;
            } else {
                break;
            }
        }
        return quantity - r;
    }
}
