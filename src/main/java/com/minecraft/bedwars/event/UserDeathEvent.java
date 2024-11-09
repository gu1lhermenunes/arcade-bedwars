package com.minecraft.bedwars.event;

import com.minecraft.bedwars.event.type.DeathType;
import com.minecraft.bedwars.user.User;
import com.minecraft.core.bukkit.event.handler.ServerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeathEvent extends ServerEvent {
    private User user;
    private User damager;
    private DeathType type;
    private boolean definitelyDeath;
}
