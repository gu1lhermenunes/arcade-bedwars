package com.minecraft.bedwars.user;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.room.team.Team;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.server.route.BedwarsRouteContext;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.reflection.Info;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class User implements BukkitInterface {

    private final Account account;

    private BedwarsRouteContext routeContext;
    private GameScoreboard scoreboard;

    private Room room;
    private Player player;

    private State state;

    private Team team;

    public User(Account account, BedwarsRouteContext context) {
        this.account = account;
        this.routeContext = context;

        this.team = null;
        this.state = State.PLAYING;
    }

    public UUID getUniqueId() {
        return account.getUniqueId();
    }

    public String getName() {
        return player.getName();
    }

    public static User fetch(UUID uuid) {
        return Bedwars.getInstance().getUserStorage().getUser(uuid);
    }

    public boolean isPlaying() {
        return room != null && !room.isSpectator(this);
    }

    @Override
    public String toString() {
        return "User{" +
                "name=" + account.getDisplayName() +
                '}';
    }

    public void lobby() {
        Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.BEDWARS_LOBBY);

        if (server == null)
            server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY);

        if (server != null) {
            account.connect(server);
        } else {
            Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> getPlayer().kickPlayer("§c" + account.getLanguage().translate("no_server_available", "lobby")), 1);
        }
    }

    @Info(fancyName = "Estado")
    private String state() {
        if (getRoom() == null)
            return null;
        return getRoom().isSpectator(this) ? "Espectador" : "Vivo";
    }

    public boolean isRespawning() {
        return this.getState() == State.RESPAWNING;
    }

    public boolean isDead() {
        return this.getState() == State.DEAD;
    }

    @Getter
    public enum State {
        RESPAWNING, DEAD, PLAYING
    }
}