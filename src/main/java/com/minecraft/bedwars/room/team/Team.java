package com.minecraft.bedwars.room.team;

import com.minecraft.bedwars.room.Room;
import com.minecraft.bedwars.room.island.Island;
import com.minecraft.bedwars.room.team.column.TeamColor;
import com.minecraft.bedwars.user.User;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
@Setter
public class Team {

    private final List<User> members;
    private final String display;
    private final TeamColor color;
    private final List<Location> locationList;
    private final ItemStack[] armor;
    private boolean bedBroken;

    public Team(String display, TeamColor color) {
        this.display = color.getDisplay();
        this.color = color;
        this.members = new ArrayList<>();
        this.locationList = new ArrayList<>();
        this.bedBroken = false;
        this.armor = new ItemStack[]{ new ItemFactory().setType(Material.LEATHER_HELMET).setColor(color.getColor()).getStack(), new ItemFactory().setType(Material.LEATHER_CHESTPLATE).setColor(color.getColor()).getStack(), new ItemFactory().setType(Material.LEATHER_LEGGINGS).setColor(color.getColor()).getStack(), new ItemFactory().setType(Material.LEATHER_BOOTS).setColor(color.getColor()).getStack() };
    }

    public boolean isMember(User user) {
        return getMembers().contains(user);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Team otherTeam = (Team) obj;
        return this.display.equals(otherTeam.display) && this.color == otherTeam.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(display, color);
    }
}
