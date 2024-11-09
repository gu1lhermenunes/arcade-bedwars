package com.minecraft.bedwars.listener;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.room.island.Island;
import com.minecraft.bedwars.user.User;
import com.minecraft.bedwars.util.HandleInfos;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public final class PlayerWorldListener implements Listener, BukkitInterface {

    private final Bedwars instance;

    public PlayerWorldListener(Bedwars bedwars) {
        this.instance = bedwars;
    }

    private List<Material> BLOCKED_DROPS = List.of(Material.WOOD_SWORD);

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null) return;

        if (room.getMode() == null) return;

        if (room.getStage() != RoomStage.PLAYING) return;

        if (BLOCKED_DROPS.contains(event.getItemDrop().getItemStack().getType())) {
            event.setCancelled(true);
            return;
        }

        if (event.getItemDrop().getItemStack().getType().name().contains("SWORD") && !BLOCKED_DROPS.contains(event.getItemDrop().getItemStack().getType())) {
            event.getItemDrop().setPickupDelay(30);
            player.getInventory().setItem(0, new ItemStack(Material.WOOD_SWORD));
        }
        event.setCancelled(false);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMapBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING) {
            event.setCancelled(true);
            return;
        }

        Island island = room.getMode().getIslandForTeam(room, user.getTeam());
        var blockType = event.getBlock().getType();
        var location = event.getBlock().getLocation();

        if (!user.isPlaying() && !Vanish.getInstance().isVanished(user.getUniqueId())) {
            event.setCancelled(true);
        } else if (blockType.equals(Material.BED_BLOCK)) {
            var startX = location.getBlockX() - 2;
            var startY = location.getBlockY() - 2;
            var startZ = location.getBlockZ() - 2;
            var endX = location.getBlockX() + 2;
            var endY = location.getBlockY() + 2;
            var endZ = location.getBlockZ() + 2;

            for (var team : room.getTeams()) {
                var bedLocation = team.getLocationList().get(1);
                var bedX = bedLocation.getBlockX();
                var bedY = bedLocation.getBlockY();
                var bedZ = bedLocation.getBlockZ();

                for (var blockX = startX; blockX < endX; blockX++) {
                    for (var blockY = startY; blockY < endY; blockY++) {
                        for (var blockZ = startZ; blockZ < endZ; blockZ++) {
                            if (bedX == blockX && bedY == blockY && bedZ == blockZ) {
                                if (team.isMember(user)) {
                                    player.sendMessage("§cVocê não pode quebrar a sua própria cama.");
                                    event.setCancelled(true);
                                    return;
                                }
                                new HandleInfos().BedBreakAnnounce(user, team);
                                var account = Account.fetch(team.getMembers().get(0).getUniqueId());
                                account.addInt(100, Columns.BEDWARS_SOLO_POINTS);

                                player.sendMessage("§e+§c" + 100 + "§e XP §8(Destruição de Cama)");

                                async(() -> account.getDataStorage().saveTable(Tables.BEDWARS));
                                team.setBedBroken(true);
                                event.getBlock().getDrops().clear();
                                event.getBlock().setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        } else if (!room.getRollback().contains(event.getBlock())) {
            event.setCancelled(true);
        }
        else {
            room.getRollback().remove(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        var block = event.getBlock();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING) {
            event.setCancelled(true);
            return;
        }

        for (var team : room.getTeams()) {
            var spawn = team.getLocationList().get(0);

            int minX = spawn.getBlockX() - 5;
            int maxX = spawn.getBlockX() + 5;
            int minY = spawn.getBlockY();
            int maxY = spawn.getBlockY() + 6;
            int minZ = spawn.getBlockZ() - 10;
            int maxZ = spawn.getBlockZ() + 6;

            Location blockLocation = block.getLocation();

            if (blockLocation.getX() >= minX && blockLocation.getX() <= maxX &&
                    blockLocation.getY() >= minY && blockLocation.getY() <= maxY &&
                    blockLocation.getZ() >= minZ && blockLocation.getZ() <= maxZ) {
                event.setCancelled(true);
                return;
            }
        }

        if (!user.isPlaying() && !Vanish.getInstance().isVanished(user.getUniqueId())) {
            event.setCancelled(true);
        } else {
            room.getRollback().add(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        World world = event.getToBlock().getWorld();

        Room room = instance.getRoomStorage().getRoom(world);

        if (room == null || room.getStage() != RoomStage.PLAYING) {
            event.setCancelled(true);
            return;
        }

        Set<Block> rollbackList = room.getRollback();

        rollbackList.add(event.getToBlock());
        rollbackList.add(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (!room.getMode().isCanBuild()) {
            event.setCancelled(true);
            return;
        }

        if (!user.isPlaying() && !Vanish.getInstance().isVanished(user.getUniqueId())) {
            event.setCancelled(true);
        } else {

            Block block = event.getBlockClicked().getRelative(event.getBlockFace());

            if (block.getType() != Material.AIR)
                return;

            room.getRollback().add(block);
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }
}
