/*
 * Copyright (C) Trydent, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.bedwars.user;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.user.loader.UserLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserStorage {

    private final Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    public void start(Bedwars bedwars) {
        bedwars.getServer().getPluginManager().registerEvents(new UserLoader(), bedwars);
    }

    public void store(UUID uuid, User account) {
        getUsers().put(uuid, account);
    }

    public void forget(UUID uniqueId) {
        getUsers().remove(uniqueId);
    }

    public User getUser(UUID uuid) {
        return getUsers().get(uuid);
    }

    public Map<UUID, User> getUsers() {
        return users;
    }

}