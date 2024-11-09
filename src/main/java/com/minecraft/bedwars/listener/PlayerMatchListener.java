package com.minecraft.bedwars.listener;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.user.User;
import com.minecraft.bedwars.util.Battle;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerShowEvent;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.event.protocol.PacketReceiveEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

public final class PlayerMatchListener implements Listener {

    private final Bedwars instance;

    public PlayerMatchListener(Bedwars bedwars) {
        this.instance = bedwars;
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING || room.getSpectators().contains(user) || user.getState() != User.State.PLAYING || !user.isPlaying() && !Vanish.getInstance().isVanished(player.getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) && !(event.getDamager() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        User victim = User.fetch(player.getUniqueId());

        Room room = victim.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING || room.getSpectators().contains(victim) || victim.getState() != User.State.PLAYING || !victim.isPlaying() && !Vanish.getInstance().isVanished(player.getUniqueId()))
            event.setCancelled(true);

        var attacker = User.fetch(((Player) event.getDamager()).getUniqueId());

        assert room != null;
        if (room.getPlayers().contains(attacker)) {
            makeCombatLog(attacker.getPlayer(), victim.getPlayer());
        } else {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING || !user.isPlaying())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING || !user.isPlaying())
            event.setCancelled(true);
    }

    @EventHandler
    private void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onAwardAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onVanishEnable(PlayerVanishEnableEvent event) {

        Player player = Bukkit.getPlayer(event.getAccount().getUniqueId());
        User user = User.fetch(event.getAccount().getUniqueId());
        Room room = user.getRoom();

        if (room != null) {
            if (!room.isSpectator(user)) {
                player.sendMessage("§aReentrando como espectador.");
                room.join(user, PlayMode.VANISH, true);
            }
        }
    }

    @EventHandler
    public void onVanishEnable(PlayerVanishDisableEvent event) {

        Player player = Bukkit.getPlayer(event.getAccount().getUniqueId());
        User user = User.fetch(event.getAccount().getUniqueId());
        Room room = user.getRoom();

        if (room != null) {
            if (room.isSpectator(user)) {
                player.sendMessage("§aReentrando como jogador.");
                room.join(user, PlayMode.PLAYER, true);
            }
        }
    }

    @EventHandler
    public void onShow(PlayerShowEvent event) {
        if (!event.getReceiver().getWorld().getUID().equals(event.getTohide().getWorld().getUID()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof PacketPlayInTabComplete) {
            Player player = event.getPlayer();
            User user = User.fetch(player.getUniqueId());
            Room room = user.getRoom();

            if (room == null || room.getStage() == RoomStage.WAITING || room.getStage() == RoomStage.STARTING)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeamAssign(PlayerTeamAssignEvent event) {
        Account account = event.getAccount();
        User user = User.fetch(account.getUniqueId());

        if (user.isPlaying()) {

            Room room = user.getRoom();

            if (room == null) {
                event.getTeam().setPrefix("§7§k");
            }

            if (user.getTeam() != null) {
                if (user.getState() == User.State.RESPAWNING) {
                    event.getTeam().setPrefix("§8§o");
                } else if (user.getState() == User.State.DEAD) {
                    event.getTeam().setPrefix("§8§o");
                } else {
                    event.getTeam().setPrefix(user.getTeam().getColor().getChatColor() + "");
                }
            }
        }
    }

    public void makeCombatLog(Player damage, Player damaged) {
        if (Battle.inCombat(damaged)) {
            Battle.setCombat(damage, damaged);
            System.out.println("[DEBUG] " + damage.getName() + " entrou novamente em combate com " + damaged.getName());
        } else {
            Battle.setCombat(damage, damaged);
            System.out.println("[DEBUG] " + damage.getName() + " entrou em combate com " + damaged.getName());
        }
    }
}