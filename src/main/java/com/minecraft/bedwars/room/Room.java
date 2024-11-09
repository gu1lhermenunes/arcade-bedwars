package com.minecraft.bedwars.room;

import com.minecraft.bedwars.config.MapConfiguration;
import com.minecraft.bedwars.event.UserConnectEvent;
import com.minecraft.bedwars.mode.Mode;
import com.minecraft.bedwars.mode.list.Solo;
import com.minecraft.bedwars.room.island.Island;
import com.minecraft.bedwars.room.team.Team;
import com.minecraft.bedwars.room.team.column.TeamColor;
import com.minecraft.bedwars.user.User;
import com.minecraft.bedwars.util.enums.Items;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class Room implements BukkitInterface {

    private final Mode mode;
    private final String code;
    private final World world;
    private final Set<User> players;
    private final Set<User> spectators;
    private final Set<Block> rollback;
    private final List<Team> teams;
    private final Set<Island> islands;
    private int minPlayers;
    private int maxPlayers;
    private int win;
    private UUID lock;

    private RoomStage stage;
    private boolean countStats;
    private MapConfiguration mapConfiguration;
    private int time;
    private final String worldName;

    public Room(int id, Mode mode, World world) {
        this.mode = mode;
        this.code = "56c" + id;
        this.teams = new ArrayList<>();
        this.islands = new HashSet<>();
        this.spectators = new HashSet<>();
        this.rollback = new HashSet<>();
        this.players = new HashSet<>();
        this.stage = RoomStage.WAITING;
        this.worldName = world.getName();
        this.minPlayers = 4;
        this.maxPlayers = (mode instanceof Solo ? 8 : 16);
        this.time = -1;
        this.win = -1;
        this.countStats = true;
        this.world = world;
    }

    public boolean isSpectator(User user) {
        return spectators.contains(user);
    }

    public void rollback() {
        Bukkit.getConsoleSender().sendMessage("§f[DEBUG] Rollbacking room: §7" + getMode().getClass().getSimpleName() + "§f(§7" + getCode() + "§f), please wait...");
        setStage(RoomStage.ROLLBACKING);
        this.rollback.forEach(block -> block.setType(Material.AIR));
        this.rollback.clear();
        getSpectators().clear();
        getPlayers().clear();
        setTime(-1);
        setStage(RoomStage.WAITING);
        getWorld().getEntitiesByClasses(Item.class).forEach(Entity::remove);
    }

    public List<User> getAlivePlayers() { // TODO: Change this
        return getPlayers().stream().filter(user -> user.getUniqueId() != null).collect(Collectors.toList());
    }

    public boolean isFull() {
        return (getPlayers().size() == maxPlayers);
    }

    public boolean isMinPlayersToStart() {
        return (getPlayers().size() == minPlayers);
    }

    public void join(User user, PlayMode playMode, boolean teleport) {

        if (user.getRoom() != null)
            user.getRoom().getMode().quit(user);

        Player player = user.getPlayer();
        Account account = user.getAccount();

        if (playMode == PlayMode.PLAYER) {

            getSpectators().remove(user);

            if (isFull()) {
                player.sendMessage(account.getLanguage().translate("arcade.room.full", getCode()));
                user.lobby();
            } else if (getStage() != RoomStage.WAITING) {
                player.sendMessage(account.getLanguage().translate("arcade.room.already_started", getCode()));
                user.lobby();
            } else {
                getPlayers().add(user);
            }
        } else {
            getPlayers().remove(user);
            getSpectators().add(user);
        }

        if (teleport) {
            player.teleport(getMapConfiguration().getSpawnPoint());
        }

        user.setRoom(this);
        getMode().join(user, playMode);
    }

    public void start() {
        this.stage = RoomStage.PLAYING;
        this.time = 1;
        mode.start(this);
        world.getPlayers().forEach(player -> player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 1F));
    }

    public void win(Team team) {
        getWorld().getEntitiesByClasses(Item.class).forEach(Entity::remove);
        setStage(RoomStage.ENDING);
        setWin(getTime());
        getAlivePlayers().forEach(user -> {
            Player player = user.getPlayer();
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setHeldItemSlot(2);
            Items.find(user.getAccount().getLanguage()).build(player);

            Account account = user.getAccount();

            Mode mode = getMode();

            if (countStats) {
                account.addInt(1, mode.getWins());
                account.addInt(1, mode.getWinstreak());
                if (account.getData(mode.getWinstreak()).getAsInt() > account.getData(mode.getWinstreakRecord()).getAsInt())
                    account.getData(mode.getWinstreakRecord()).setData(account.getData(mode.getWinstreak()).getAsInt());
                async(() -> account.getDataStorage().saveTable(mode.getWins().getTable()));
            }
        });

        getSpectators().forEach(user -> {
            Player player = user.getPlayer();
            Items.find(user.getAccount().getLanguage()).build(player);
        });
        setCountStats(true);
    }

    @Override
    public String toString() {
        int limit = getMaxPlayers() / 2;
        return "(" + getCode() + ") " + getMode().getClass().getSimpleName() + " " + limit + "v" + limit;
    }

    public boolean isOutside(Location location) {
        MapConfiguration mapConfiguration = getMapConfiguration();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return absolute(x) >= mapConfiguration.getSize() || y >= mapConfiguration.getHeight() || absolute(z) >= mapConfiguration.getSize();
    }

    public boolean isLock() {
        return lock != null;
    }

    public final Hologram.Interact interact = (player, hologram, line, type) -> {
        List<NPC> npcs = new ArrayList<>(BukkitGame.getEngine().getNPCProvider().getPlayerHumans(player));
        npcs.removeIf(NPC::isHidden);

        if (npcs.isEmpty())
            return;

        npcs.sort(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(hologram.getLocation())));
        NPC.Interact interact = npcs.get(0).getInteractExecutor();
        if (interact != null)
            interact.handle(player, npcs.get(0), NPC.Interact.ClickType.RIGHT);
    };
}