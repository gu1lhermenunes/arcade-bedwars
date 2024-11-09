package com.minecraft.bedwars.mode.list;

import com.minecraft.bedwars.mode.Mode;
import com.minecraft.bedwars.room.Room;
import com.minecraft.core.bukkit.server.bedwars.BedwarsType;
import com.minecraft.core.database.enums.Columns;

import java.io.File;
import java.util.List;
import java.util.Set;

public class Solo extends Mode {

    public Solo() {
        super(1, 2, BedwarsType.BEDWARS_SOLO);
        setWins(Columns.BEDWARS_SOLO_WINS);
        setGames(Columns.BEDWARS_SOLO_ROUNDS);
    }

    @Override
    public void start(Room room) {
        super.start(room);
    }
}
