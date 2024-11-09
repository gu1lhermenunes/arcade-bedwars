package com.minecraft.bedwars.room.storage;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.mode.Mode;
import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.bedwars.util.generator.Generator;
import com.minecraft.bedwars.util.generator.task.GeneratorTask;
import com.minecraft.bedwars.util.generator.type.GeneratorType;
import com.minecraft.core.bukkit.server.bedwars.BedwarsType;
import com.minecraft.core.bukkit.server.duels.DuelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class RoomStorage {

    private final Map<UUID, Room> rooms = new HashMap<>();
    private final Bedwars bedwars;

    public Room getRoom(World world) {
        return rooms.get(world.getUID());
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(bedwars, () -> rooms.values().forEach(c -> c.getMode().tick(c)), 20, 20);
    }

    public void register(Mode... modes) {
        for (Mode mode : modes) {
            mode.load();
        }
    }

    public void end() {
        delete(new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata"));
        rooms.forEach((c, a) -> delete(a.getWorld().getWorldFolder()));
        rooms.clear();
        Bukkit.getWorlds().forEach(w -> {
            Bukkit.unloadWorld(w, false);
            delete(w.getWorldFolder());
        });
    }

    public void register(Room room) {
        rooms.put(room.getWorld().getUID(), room);
    }

    public void delete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                delete(new File(file, child));
            }
        }
        if (file.exists())
            file.delete();
    }

    public Room get(BedwarsType bedwarsType) {
        return getRooms().values().stream().filter(room -> {

            if (room.isLock())
                return false;

            if (room.getStage() == RoomStage.WAITING && room.getMode().getSupportedModes().contains(bedwarsType)) {
                return !room.isFull();
            }

            return false;
        }).min((a, b) -> Integer.compare(b.getAlivePlayers().size(), a.getAlivePlayers().size())).orElse(null);
    }

    public List<Room> getRooms(BedwarsType bedwarsType) {
        return getRooms().values().stream().filter(room -> room.getMode().getSupportedModes().contains(bedwarsType)).collect(Collectors.toList());
    }

    public List<Room> getBusy(BedwarsType bedwarsType) {
        return getRooms().values().stream().filter(room -> room.getMode().getSupportedModes().contains(bedwarsType) && room.getStage() != RoomStage.WAITING && room.getStage() != RoomStage.STARTING).collect(Collectors.toList());
    }

    public List<Room> getBusy() {
        return getRooms().values().stream().filter(room -> room.getStage() != RoomStage.WAITING && room.getStage() != RoomStage.STARTING).collect(Collectors.toList());
    }

    public Bedwars getBedwars() {
        return bedwars;
    }
}
