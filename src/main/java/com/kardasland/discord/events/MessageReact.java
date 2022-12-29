package com.kardasland.discord.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.PrefixDynamic;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReact extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (!event.getChannel().getId().equals(ConfigManager.get("config.yml").getString("discord.channels.dailyreward"))){
                return;
            }
            if (!event.getChannel().getId().equals(ConfigManager.get("config.yml").getString("discord.channels.verify"))){
                return;
            }
        }
        for (PrefixDynamic dynamic : VeldoryaJDA.instance.getEmbedCommandsList()){
            if (content.matches("^"+dynamic.getCommand() +"$")){
                event.getChannel().asTextChannel().sendMessageEmbeds(dynamic.getEmbed().buildEmbed()).queue();
                return;
            }
        }
    }
}
