package com.minecraft.bedwars.user.loader;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.event.UserConnectEvent;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.user.User;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.bedwars.util.visibility.Visibility;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.server.route.BedwarsRouteContext;
import com.minecraft.core.bukkit.server.route.BedwarsRouteContext;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.translation.Language;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Bed;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import redis.clients.jedis.Jedis;

import java.util.*;

public class UserLoader implements Listener, BukkitInterface {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        User user = User.fetch(event.getPlayer().getUniqueId());

        player.resetTitle();

        if (user == null) {
            player.kickPlayer(Language.PORTUGUESE.translate("unexpected_error"));
            return;
        }

        if (user.getRoom() == null) {
            if (user.getRouteContext().getPlayMode().equals(PlayMode.MAP_CREATOR)) {

            }
            Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), user::lobby, 3L);
            return;
        }

        Visibility.refresh(player);

        if (Vanish.getInstance().isVanished(player.getUniqueId()))
            player.setGameMode(GameMode.CREATIVE);
        else
            player.setGameMode(GameMode.SURVIVAL);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerSpawnLocationEvent event) {

        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        if (user == null) {
            player.kickPlayer("§cRota inválida (Destino não encontrado) (fail join)");
            return;
        }

        event.setSpawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
        user.setPlayer(player);

        BedwarsRouteContext context = user.getRouteContext();
        UUID target = context.getTarget();

        if (target == null) { // Player is searching for the best room through the queue.

            Room room = Bedwars.getInstance().getRoomStorage().get(context.getGame());

            if (room == null) {
                event.setSpawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
                return;
            }

            event.setSpawnLocation(room.getMapConfiguration().getSpawnPoint());
            room.join(user, context.getPlayMode(), false);

        } else if (context.getPlayMode() == PlayMode.PLAYER) {

            List<Room> rooms = new ArrayList<>(Bedwars.getInstance().getRoomStorage().getRooms().values());

            Iterator<Room> iterator = rooms.iterator();
            Room room = null;

            while (iterator.hasNext()) {
                Room iteratorRoom = iterator.next();

                if (iteratorRoom.getStage() != RoomStage.WAITING || !iteratorRoom.getMode().getSupportedModes().contains(context.getGame())) {
                    iterator.remove();
                    continue;
                }

                if (iteratorRoom.getWorld().getPlayers().size() != 0) {
                    if (!iteratorRoom.isLock() || !iteratorRoom.getLock().equals(user.getUniqueId())) {
                        iterator.remove();
                        continue;
                    }
                }

                if (iteratorRoom.isLock()) {
                    if (iteratorRoom.getLock().equals(user.getUniqueId())) {
                        System.out.println(iteratorRoom.getCode() + " is locked! Fetched!");
                        room = iteratorRoom;
                        room.setCountStats(false);
                        break;
                    }
                }
            }

            if (room == null) {
                if (rooms.isEmpty()) {
                    System.out.println("Rooms is empty!");
                    Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), user::lobby, 3L);
                    return;
                }

                room = rooms.get(0);
                room.setLock(target);
            }

            System.out.println("Join: " + room.getCode());

            event.setSpawnLocation(room.getMapConfiguration().getSpawnPoint());
            room.join(user, PlayMode.PLAYER, false);

            if (room.getLock().equals(user.getUniqueId()))
                room.setLock(null);
        } else {

            Player targetPlayer = Bukkit.getPlayer(target);

            if (targetPlayer == null)
                return;

            User targetUser = User.fetch(target);
            Room room = targetUser.getRoom();

            if (room == null) {
                Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), user::lobby, 3L);
                return;
            }

            event.setSpawnLocation(room.getMapConfiguration().getSpawnPoint());
            room.join(user, PlayMode.VANISH, false);

            context.setGame(targetUser.getRouteContext().getGame());

            if (!Vanish.getInstance().isVanished(user.getUniqueId()))
                room.getWorld().getPlayers().forEach(other -> other.sendMessage(Account.fetch(other.getUniqueId()).getLanguage().translate("duels.spectator_join", targetUser.getName())));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        UUID uniqueId = event.getUniqueId();
        Account account = Account.fetch(uniqueId);

        if (account == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "unexpected_error");
            return;
        }

        String routeKey = "route:" + account.getUniqueId();

        try {
            try (Jedis redis = Constants.getRedis().getResource()) {

                String route = redis.get(routeKey);

                if (route == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, account.getLanguage().translate("arcade.route.not_found", "no route"));
                    return;
                }

                BedwarsRouteContext gameRouteContext;

                if (Constants.isUniqueId(route)) {

                    UUID targetUUID = UUID.fromString(route);

                    User user = User.fetch(targetUUID);

                    if (user == null || !user.isPlaying()) {
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cO alvo não está em jogo.");
                        return;
                    }

                    gameRouteContext = new BedwarsRouteContext();
                    gameRouteContext.setTarget(targetUUID);
                    gameRouteContext.setPlayMode(PlayMode.VANISH);

                } else {
                    gameRouteContext = Constants.GSON.fromJson(route, BedwarsRouteContext.class);

                    // logar colunas fazer sistema
                }

                redis.del(routeKey);
                Bedwars.getInstance().getUserStorage().store(event.getUniqueId(), new User(account, gameRouteContext));
            }
        } catch (Exception e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, account.getLanguage().translate("arcade.route.not_found", "parse failed"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        User user = User.fetch(event.getPlayer().getUniqueId());

        if (user == null)
            return;

        if (user.getRoom() != null)
            user.getRoom().getMode().quit(user);

        Bedwars.getInstance().getUserStorage().forget(event.getPlayer().getUniqueId());
    }
}