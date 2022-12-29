package com.kardasland.spigot.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
    @EventHandler
    public void chat(AsyncPlayerChatEvent event){
        VeldoryaJDA.instance.getGuild()
                .getTextChannelById(ConfigManager.get("config.yml").getString("discord.channels.discordchat"))
                .sendMessage(ConfigManager.get("config.yml").getString("discordchat.format").replaceAll("%player%", event.getPlayer().getName()).replaceAll("%message%", event.getMessage())).queue();
    }
}
