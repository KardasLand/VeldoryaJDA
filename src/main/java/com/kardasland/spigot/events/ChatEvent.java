package com.kardasland.spigot.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.awt.*;

public class ChatEvent implements Listener {
    //TODO TO BE TESTED
    @EventHandler
    public void chat(AsyncPlayerChatEvent event){
        //simple yet powerful
        if (event.getMessage().contains("@")){
            return;
        }
        CustomEmbed customEmbed = new CustomEmbed(null, ConfigManager.get("config.yml").getString("discordchat.format").replaceAll("%player%", event.getPlayer().getName()).replaceAll("%message%", event.getMessage()), Color.GREEN);
        VeldoryaJDA.instance.getGuild()
                .getTextChannelById(ConfigManager.get("config.yml").getString("discord.channels.discordchat"))
                .sendMessageEmbeds(customEmbed.buildEmbed()).queue();
                //.sendMessage(ConfigManager.get("config.yml").getString("discordchat.format").replaceAll("%player%", event.getPlayer().getName()).replaceAll("%message%", event.getMessage())).queue();
    }
}
