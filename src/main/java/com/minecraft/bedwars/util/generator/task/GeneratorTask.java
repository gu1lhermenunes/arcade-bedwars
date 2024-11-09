package com.minecraft.bedwars.util.generator.task;

import com.minecraft.bedwars.Bedwars;
import com.minecraft.bedwars.util.enums.RoomStage;
import com.minecraft.bedwars.util.generator.Generator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@Getter @Setter
public final class GeneratorTask {
    private final Generator generator;

    public GeneratorTask(Generator generator) {
        this.generator = generator;
    }

    public void task() {
        var location = generator.getLocation();
        var type = generator.getType();

        new BukkitRunnable() {
            @Override
            public void run() {
                Item item = location.getWorld().dropItem(location, new ItemStack(type.getMaterial()));
                item.setVelocity(new Vector().setY(0.06));

                task();
            }
        }.runTaskLater(Bedwars.getInstance(), type.getTick());
    }
}
