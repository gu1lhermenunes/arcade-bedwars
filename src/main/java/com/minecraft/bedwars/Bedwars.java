package com.minecraft.bedwars;

import com.minecraft.bedwars.commands.ConfigureCommand;
import com.minecraft.bedwars.listener.*;
import com.minecraft.bedwars.mode.list.Solo;
import com.minecraft.bedwars.room.storage.RoomStorage;
import com.minecraft.bedwars.user.UserStorage;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.server.ServerType;
import lombok.Getter;

@Getter
public class Bedwars extends BukkitGame {

    private static Bedwars instance;
    private UserStorage userStorage;
    private RoomStorage roomStorage;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (getPluginUpdater().isUpdated())
            return;

        userStorage = new UserStorage();
        userStorage.start(this);

        roomStorage = new RoomStorage(this);
        roomStorage.start();

        roomStorage.register(new Solo());

        Constants.setServerType(ServerType.BEDWARS);
        Constants.setLobbyType(ServerType.BEDWARS_LOBBY);

        getServerStorage().listen(ServerType.MAIN_LOBBY, ServerType.BEDWARS_LOBBY);

        getEngine().getBukkitFrame().registerCommands(new ConfigureCommand());

        getServer().getPluginManager().registerEvents(new PlayerDamageFixerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMatchListener(this), this);
        //getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);

        startServerDump();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        roomStorage.end();
    }

    public static Bedwars getInstance() {
        return instance;
    }
}