package com.minecraft.bedwars.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class MapConfiguration {

    private String name;
    private Location spawnPoint;
    private int size, height;

    @Override
    public String toString() {
        return "MapConfiguration{" +
                "name='" + name + '\'' +
                ", spawnPoint=" + spawnPoint +
                ", size=" + size +
                ", height=" + height +
                '}';
    }
}
