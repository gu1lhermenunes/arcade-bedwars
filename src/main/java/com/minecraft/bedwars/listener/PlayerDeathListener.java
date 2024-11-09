package com.minecraft.bedwars.listener;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.event.UserConnectEvent;
import com.minecraft.bedwars.event.UserDeathEvent;
import com.minecraft.bedwars.event.UserDisconnectEvent;
import com.minecraft.bedwars.event.type.DeathType;
import com.minecraft.bedwars.mode.Mode;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.user.User;
import com.minecraft.bedwars.util.Battle;
import com.minecraft.bedwars.util.HandleInfos;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

import java.util.Random;

public final class PlayerDeathListener implements Listener, BukkitInterface {

    private final Bedwars instance;

    public PlayerDeathListener(Bedwars bedwars) {
        this.instance = bedwars;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onDeath(UserDeathEvent event) {
        User user = event.getUser();
        Player player = user.getPlayer();

        player.closeInventory();

        var room = user.getRoom();
        var mode = room.getMode();

        var account = user.getAccount();

        room.getWorld().getPlayers().forEach(p -> {
            if (event.getDamager() != null) {
                p.getPlayer().sendMessage(event.getType().getMessage().replace("{victim}", user.getTeam().getColor().getChatColor() + account.getDisplayName()).replace("{attacker}", event.getDamager().getTeam().getColor().getChatColor() + event.getDamager().getAccount().getDisplayName()) + (user.getTeam().isBedBroken() ? "§e. " + user.getTeam().getColor().getChatColor() + "§lKILL FINAL!" : "§e."));
            } else {
                p.getPlayer().sendMessage(event.getType().getMessage().replace("{victim}", user.getTeam().getColor().getChatColor() + account.getDisplayName()));
            }
        });

        switch (event.getType()) {
            case FALL:
            case VOID:
            case NULL:
                player.teleport(user.getTeam().getLocationList().get(0));
                break;
            case PLAYER:
            case VOID_FOR_ATTACKER:
            case FALL_FOR_ATTACKER:
                player.teleport((event.getDamager() != null ? event.getDamager().getPlayer().getLocation() : user.getTeam().getLocationList().get(0)));
                break;
        }

        if (event.isDefinitelyDeath()) {
            room.getPlayers().remove(user);
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.spigot().setCollidesWithEntities(false);
            player.setMaxHealth(20.0D);
            player.setHealth(20.0D);
            player.setNoDamageTicks(20 * 5);

            new HandleInfos().teamElimininateAnnounce(user, user.getTeam());

            player.sendTitle(new Title("§c§lELIMINAÇÃO FINAL!", "§eVocê não irá mais renascer.", 3, 20, 3));

            user.getTeam().getMembers().remove(user);
            user.setTeam(null);
            room.getSpectators().add(user);
            user.setState(User.State.DEAD);
            room.refreshTablist(user.getAccount());
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);
            mode.join(user, PlayMode.VANISH);
        } else {
            player.getInventory().clear();
            player.setAllowFlight(true);
            player.setFlying(true);
            player.getInventory().setArmorContents(null);

            player.setMaxHealth(20.0D);
            player.setHealth(20.0D);
            player.setNoDamageTicks(20 * 5);

            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.spigot().setCollidesWithEntities(false);

            new BukkitRunnable() {
                int seconds = 5;
                @Override
                public void run() {
                    if (user.getPlayer() == null) {
                        cancel();
                        return;
                    }

                    if (seconds == 0) {
                        cancel();

                        player.sendTitle(new Title("", "", 0, 0, 0));
                        mode.spawn(user);
                        player.setHealth(20.0D);

                        player.spigot().setCollidesWithEntities(true);

                        player.sendTitle(new Title("", "§eRenasceu!", 0, 4, 0));

                        for (PotionEffect effect : player.getActivePotionEffects())
                            player.removePotionEffect(effect.getType());

                        room.refreshTablist(user.getAccount());
                        room.getPlayers().stream().map(User::getPlayer).forEach(players -> players.showPlayer(player));
                        return;
                    }

                    if (player.isOnline()) {
                        user.setState(User.State.RESPAWNING);
                        room.getPlayers().forEach(u -> u.getPlayer().hidePlayer(player));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);
                        room.refreshTablist(user.getAccount());
                        player.sendTitle(new Title("§c§lELIMINADO!", "§eRessurgindo em §c" + seconds, 0, 20, 0));
                    }

                    seconds--;
                }
            }.runTaskTimer(Bedwars.getInstance(), 0, 20);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStopDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.isCancelled())
                return;

            Player p = (Player) event.getEntity();

            User user = User.fetch(p.getUniqueId());

            var team = user.getTeam();

            if (user.isRespawning() || user.isDead()) return;

            var cause = event.getCause();

            if (cause == EntityDamageEvent.DamageCause.VOID) {
                if (team.isBedBroken()) {
                    event.setCancelled(true);
                    p.setHealth(20.0D);
                    new UserDeathEvent(user, null, DeathType.NULL, true).fire();
                } else {
                    event.setCancelled(true);
                    p.setHealth(20.0D);
                    new UserDeathEvent(user, null, DeathType.NULL, false).fire();
                }
            } else if (p.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true);
                p.setHealth(20.0D);
                if (Battle.inCombat(p)) {
                    var opponent = Battle.getOpponent(p);

                    if (opponent != null) {
                        if (team.isBedBroken()) {
                            var attacker = User.fetch(opponent.getUniqueId());
                            var acc = Account.fetch(opponent.getUniqueId());

                            var dataKiller = acc.getDataStorage().getData(Columns.BEDWARS_SOLO_KILLS);

                            var xp = new Random().nextInt(50);

                            acc.addInt(1, Columns.BEDWARS_SOLO_KILLS);
                            acc.addInt(xp, Columns.BEDWARS_SOLO_POINTS);

                            opponent.sendMessage("§e+§c" + xp + "§e XP §8(Kill Final)");

                            async(() -> acc.getDataStorage().saveTable(Tables.BEDWARS));

                            new UserDeathEvent(user, attacker, DeathType.PLAYER, true).fire();
                        } else {
                            var attacker = User.fetch(opponent.getUniqueId());
                            var acc = Account.fetch(opponent.getUniqueId());
                            var dataKiller = acc.getDataStorage().getData(Columns.BEDWARS_SOLO_KILLS);

                            var xp = new Random().nextInt(10);

                            acc.addInt(1, Columns.BEDWARS_SOLO_KILLS);
                            acc.addInt(xp, Columns.BEDWARS_SOLO_POINTS);

                            opponent.sendMessage("§e+§c" + xp + "§e XP");

                            async(() -> acc.getDataStorage().saveTable(Tables.BEDWARS));

                            new UserDeathEvent(user, attacker, DeathType.PLAYER, false).fire();
                        }
                    } else {
                        if (team.isBedBroken()) {
                            new UserDeathEvent(user, null, DeathType.NULL, true).fire();
                        } else {
                            new UserDeathEvent(user, null, DeathType.NULL, false).fire();
                        }
                    }
                } else {
                    if (team.isBedBroken()) {
                        new UserDeathEvent(user, null, DeathType.NULL, true).fire();
                    } else {
                        new UserDeathEvent(user, null, DeathType.NULL, false).fire();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(UserDisconnectEvent event) {
        User user = event.getUser();
        Player player = user.getPlayer();

        Room room = user.getRoom();
        Mode mode = room.getMode();

        Account account = user.getAccount();

        room.getPlayers().remove(user);

        if (event.isAnnounce()) {
            player.getWorld().getPlayers().stream().map(Player::getPlayer)
                            .forEach(players -> players.sendMessage("§b" + user.getName() + " §esaiu da sala. (§b" + user.getRoom().getPlayers().size() + "§e/§b" + user.getRoom().getMaxPlayers() + "§e)"));
            //room.getWorld().getPlayers().forEach(action -> action.getPlayer().sendMessage("§b" + user.getName() + " §esaiu da sala. (§b" + user.getRoom().getPlayers().size() + "§e/§b" + user.getRoom().getMaxPlayers() + "§e)"));
        }
    }

    @EventHandler
    public void onConnect(UserConnectEvent event) {
        User user = event.getUser();
        Player player = user.getPlayer();

        Room room = user.getRoom();

        room.getPlayers().add(user);
        Mode mode = room.getMode();

        Account account = user.getAccount();

        if (event.isAnnounce()) {
            player.getWorld().getPlayers().stream().map(Player::getPlayer)
                    .forEach(players -> players.sendMessage("§b" + user.getName() + " §eentrou na sala. (§b" + user.getRoom().getPlayers().size() + "§e/§b" + user.getRoom().getMaxPlayers() + "§e)"));
            //room.getWorld().getPlayers().forEach(action -> action.getPlayer().sendMessage("§b" + user.getName() + " §eentrou na sala. (§b" + user.getRoom().getPlayers().size() + "§e/§b" + user.getRoom().getMaxPlayers() + "§e)"));
        }
    }
}