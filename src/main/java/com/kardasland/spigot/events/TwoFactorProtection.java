package com.kardasland.spigot.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TwoFactorProtection implements Listener {

    public static Map<String, UUID> twofamap = new HashMap<>();
    public static Map<String, String> verifiedUUIDS = new HashMap<>();

    public static boolean ipRememberEnabled = false;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (twofamap.containsValue(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
            return;
        }
        List<Player> playerRecipients = new ArrayList<>();
        for (Player recipient : event.getRecipients()) {
            playerRecipients.add(recipient);
            for (Map.Entry<String, UUID> ad : twofamap.entrySet()){
                playerRecipients.remove(Bukkit.getPlayer(ad.getValue()));
            }
        }
        event.getRecipients().clear();
        event.getRecipients().addAll(playerRecipients);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakblock(BlockBreakEvent event){
        if (twofamap.containsValue(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void place(BlockPlaceEvent event){
        if (twofamap.containsValue(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent event){
        if (ConfigManager.get("config.yml").getStringList("2faPlayers").contains(event.getPlayer().getName())){
            if (ipRememberEnabled){
                if (verifiedUUIDS.containsKey(event.getPlayer().getUniqueId().toString())){
                    if (verifiedUUIDS.get(event.getPlayer().getUniqueId().toString()).equals(event.getPlayer().getAddress().getHostString())){
                        Utils.noPrefix(event.getPlayer(), ConfigManager.get("messages.yml").getString("server.2faSuccess"));
                        return;
                    }
                }
            }
            int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
            twofamap.put(String.valueOf(code), event.getPlayer().getUniqueId());
            List<String> stringList = ConfigManager.get("messages.yml").getStringList("server.2fa");
            for (String str : stringList){
                Utils.noPrefix(event.getPlayer(), str.replaceAll("%code%", String.valueOf(code)));
            }
            CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.2fa");
            VeldoryaJDA.instance.getIsqlOperations().getDPlayerAsync(event.getPlayer(), (dPlayer1) -> {
                if (!dPlayer1.getVerified()) return;
                if (dPlayer1.getDiscordID() == null) return;
                User user = VeldoryaJDA.jda.getUserById(dPlayer1.getDiscordID());
                user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(customEmbed.buildEmbed())).queue();
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void quit(PlayerQuitEvent event){
        if (twofamap.containsValue(event.getPlayer().getUniqueId())) {
            twofamap.values().removeIf(event.getPlayer().getUniqueId()::equals);
        }
    }

    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent event) {
        UUID playeruuid = event.getPlayer().getUniqueId();
        if (twofamap.containsValue(playeruuid)) {
            List<String> cmds = ConfigManager.get("config.yml").getStringList("2faWhitelist");
            for (String command : cmds) {
                if (!event.getMessage().startsWith(command)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
