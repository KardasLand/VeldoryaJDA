package com.kardasland.spigot.runnable;

import com.kardasland.veldoryadiscord.VeldoryaJDA;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveRunnable extends BukkitRunnable {
    @Override
    public void run() {
        VeldoryaJDA.instance.getDPlayerCache().update();
    }
}
