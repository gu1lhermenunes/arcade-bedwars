/*
 * Copyright (C) Trydent, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.bedwars.util;

import com.minecraft.bedwars.Bedwars;
import org.bukkit.metadata.FixedMetadataValue;

public class GameMetadata extends FixedMetadataValue {

    public GameMetadata(Object value) {
        super(Bedwars.getInstance(), value);
    }

}