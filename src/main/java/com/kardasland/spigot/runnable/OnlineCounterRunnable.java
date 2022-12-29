package com.kardasland.spigot.runnable;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class OnlineCounterRunnable extends BukkitRunnable {
    @Override
    public void run() {
        VeldoryaJDA.jda.getPresence().setActivity(Activity.playing(ConfigManager.get("config.yml").getString("discord.status").replaceAll("%online%", String.valueOf(Bukkit.getOnlinePlayers().size())) ));
    }
}
