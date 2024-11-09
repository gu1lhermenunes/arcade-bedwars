//package com.minecraft.bedwars.room.shop;
//
//import com.minecraft.bedwars.room.shop.build.ShopItem;
//import com.minecraft.bedwars.room.shop.build.config.MineBuy;
//import com.minecraft.bedwars.room.shop.category.ShopCategory;
//import com.minecraft.core.account.Account;
//import com.minecraft.core.bukkit.BukkitGame;
//import com.minecraft.core.bukkit.util.BukkitInterface;
//import com.minecraft.core.bukkit.util.inventory.Selector;
//import com.minecraft.core.bukkit.util.item.InteractableItem;
//import com.minecraft.core.bukkit.util.item.ItemFactory;
//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.Player;
//import org.bukkit.event.Listener;
//import org.bukkit.inventory.ItemStack;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class Shop implements Listener, BukkitInterface {
//    protected Player player;
//    protected Account account;
//    protected Selector inventory;
//    protected ShopCategory category;
//
//    public Shop(Player player, Account account, ShopCategory category) {
//        this.player = player;
//        this.account = account;
//        this.category = category;
//
//        this.inventory = build();
//    }
//
//    private Selector build() {
//        List<Integer> allowedSlots = Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
//        Selector.Builder response = Selector.builder().withSize(54).withName("Compra rápida");
//        int index = 0;
//
//        for (var c : ShopCategory.values()) {
//            InteractableItem i = new InteractableItem(new ItemFactory().setType(c.getMaterial()).setName("§b" + c.getDisplay()).getStack(), new InteractableItem.Interact() {
//                @Override
//                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
//                    new Shop(player, account, c).openInventory();
//                    return true;
//                }
//            });
//
//            response.withCustomItem(index, i.getItemStack());
//            index++;
//        }
//
//        int itemIndex = 0;
//        switch (category) {
//            case SHOP:
//                break;
//            case BLOCK:
//                List<ShopItem> blocks = List.of(
//                        new ShopItem("Lã", Material.WOOL, 4, 16, MineBuy.IRON),
//                        new ShopItem("Argila", Material.STAINED_CLAY, 12, 16, MineBuy.IRON),
//                        new ShopItem("Vidro à prova de explosões", Material.STAINED_GLASS, 12, 4, MineBuy.IRON),
//                        new ShopItem("Pedra do Fim", Material.ENDER_STONE, 24, 12, MineBuy.IRON),
//                        new ShopItem("Escadas", Material.LADDER, 4, 8, MineBuy.IRON),
//                        new ShopItem("Madeira", Material.WOOD, 4, 16, MineBuy.GOLD),
//                        new ShopItem("Obsidian", Material.OBSIDIAN, 4, 4, MineBuy.EMERALD)
//                );
//
//                for (var item : blocks) {
//                    item.register();
//
//                    response.withCustomItem(allowedSlots.get(itemIndex), item.getItem().getItemStack());
//                    itemIndex++;
//                }
//                break;
//            case MELEE:
//                var stone_sword = new ShopItem("Espada de Pedra", Material.STONE_SWORD, 10, 1, MineBuy.IRON);
//                stone_sword.register();
//
//                response.withCustomItem(allowedSlots.get(itemIndex), stone_sword.getItem().getItemStack());
//                itemIndex++;
//        }
//
//        return response.build();
//    }
//
//    public void openInventory() {
//        inventory.open(player);
//        Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
//    }
//}
