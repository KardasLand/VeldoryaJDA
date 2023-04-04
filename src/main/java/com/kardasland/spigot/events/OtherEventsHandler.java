package com.kardasland.spigot.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OtherEventsHandler implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event){
        VeldoryaJDA.instance.getIsqlOperations().getDPlayerAsync(event.getPlayer(), (dPlayer) -> {
            VeldoryaJDA.instance.getDPlayerCache().add(dPlayer);
            dPlayer.update();
        });
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        VeldoryaJDA.instance.getDPlayerCache().findPlayer(event.getPlayer()).update();
        VeldoryaJDA.instance.getDPlayerCache().remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
        if (!VeldoryaJDA.instance.isFullyLoaded) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        } else {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
        }

    }
}
