package com.minecraft.bedwars.room.island;

import com.minecraft.bedwars.room.team.Team;
import com.minecraft.bedwars.util.generator.Generator;
import com.minecraft.bedwars.util.generator.task.GeneratorTask;
import com.minecraft.bedwars.util.generator.type.GeneratorType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.*;

@Getter @Setter
public class Island {
    /* Nomeclature island */
    private final UUID uuid;
    private final Team team;

    /* Configure pre-set locations island */
    private Location spawnLocation;
    private Location shopLocation;
    private Location upgradeLocation;
    private Location generatorLocation;

    private List<GeneratorTask> generators;

    private Location bedLocation;

    public Island(UUID uuid, Team team) {
        this.uuid = uuid;
        this.team = team;
        this.generators = new ArrayList<>();
    }
}
