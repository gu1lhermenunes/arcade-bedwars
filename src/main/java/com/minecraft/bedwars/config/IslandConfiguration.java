package com.minecraft.bedwars.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class IslandConfiguration {

    private String teamColor;
    private Location spawnPoint;
    private Location shopLocation;
    private Location upgradeLocation;
    private Location bedLocation;
    private Location generatorLocation;
    private int size;
    private int height;

    @Override
    public String toString() {
        return "IslandConfiguration{" +
                "teamColor='" + teamColor + '\'' +
                ", spawnPoint=" + spawnPoint +
                ", shopLocation=" + shopLocation +
                ", upgradeLocation=" + upgradeLocation +
                ", bedLocation=" + bedLocation +
                ", generatorLocation=" + generatorLocation +
                ", size=" + size +
                ", height=" + height +
                '}';
    }
}
