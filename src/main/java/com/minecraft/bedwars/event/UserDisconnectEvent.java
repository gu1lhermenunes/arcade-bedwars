package com.minecraft.bedwars.event;

import com.minecraft.bedwars.user.User;
import com.minecraft.core.bukkit.event.handler.ServerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDisconnectEvent extends ServerEvent {
    private User user;
    private boolean announce;
}
