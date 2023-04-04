package com.kardasland.discord.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.spigot.events.TwoFactorProtection;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

import java.util.UUID;

public class TwoFactorFeedback extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        if (!event.isFromType(ChannelType.PRIVATE)) return;
        if ((event.getAuthor() == null)) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (content.length() != 6) return;
        if (TwoFactorProtection.twofamap.containsKey(content)){
            UUID target = TwoFactorProtection.twofamap.get(content);
            DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayerbyUUID(target.toString());
            if (dPlayer.getDiscordID().equals(event.getAuthor().getId())){
                if (!dPlayer.getVerified()) return;
                if (dPlayer.getDiscordID() == null) return;
                TwoFactorProtection.twofamap.remove(content, target);
                Utils.noPrefix(Bukkit.getPlayer(target), ConfigManager.get("messages.yml").getString("server.2faSuccess"));
                TwoFactorProtection.verifiedUUIDS.put(Bukkit.getPlayer(target).getUniqueId().toString(), Bukkit.getPlayer(target).getAddress().getHostString());
            }
        }
    }
}
