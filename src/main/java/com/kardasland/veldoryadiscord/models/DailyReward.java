package com.kardasland.veldoryadiscord.models;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class DailyReward {
    @Getter @Setter
    String id;

    @Getter @Setter String group;
    @Getter @Setter
    List<String> commands;
    @Getter @Setter
    long cooldown;
    @Getter @Setter
    CustomEmbed embed;

    public DailyReward(String id){
        FileConfiguration cf = ConfigManager.get("dailyrewards.yml");
        this.id = id;
        this.commands = cf.getStringList("dailyrewards."+id+".commands");
        this.cooldown = cf.getLong("dailyrewards."+id+".cooldown");
        this.group = cf.getString("dailyrewards."+id+".group");


        String title = cf.getString("dailyrewards."+id+".message.title");
        String desc = cf.getString("dailyrewards."+id+".message.description");
        String color = cf.getString("dailyrewards."+id+".message.color");
        String footer = cf.getString("dailyrewards."+id+".message.footer");
        String thumbnail = cf.getString("dailyrewards."+id+".message.thumbnail");
        CustomEmbed customEmbed = new CustomEmbed(title, desc, Utils.getAWTColor(color));
        CustomEmbed.Footer footer1 = new CustomEmbed.Footer(footer, "");
        customEmbed.setThumbnail(thumbnail);
        customEmbed.setFooter(footer1);
        this.embed = customEmbed;
    }

    public boolean checkTime(long current, long old){
        return (current - old) >= cooldown;
    }
    public boolean checkTime(long current){
        return current >= cooldown;
    }

    public boolean checkGroup(DPlayer dPlayer){
        return dPlayer.getGroups().contains(group);
    }

    public void execute(Player player){
        for (String command : commands){
            new BukkitRunnable()
            {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", player.getName()));
                }
            }.runTask(VeldoryaJDA.instance);
        }
    }





}
