package com.kardasland.spigot.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveHandler implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event){
        DPlayer dPlayer = new DPlayer(event.getPlayer());
        VeldoryaJDA.instance.getDPlayerCache().add(dPlayer);
        send(event.getPlayer(), "join");
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        VeldoryaJDA.instance.getDPlayerCache().remove(event.getPlayer());
        send(event.getPlayer(), "leave");

    }


    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
        if (!VeldoryaJDA.instance.isFullyLoaded) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        } else {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
        }

    }

    /**
     * To prevent the code mess.
     * @param player target player
     * @param action the path. has to be join or leave
     */
    private void send(Player player, String action){
        CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds."+action);
        customEmbed.setDesc(customEmbed.getDesc().replaceAll("%player%", player.getName()));
        VeldoryaJDA.instance.getGuild()
                .getTextChannelById(ConfigManager.get("config.yml").getString("discord.channels.joinleave"))
                .sendMessageEmbeds(customEmbed.buildEmbed()).queue();
    }
}
