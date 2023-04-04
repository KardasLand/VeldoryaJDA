package com.kardasland.discord.events;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.commands.prefix.PrefixReward;
import com.kardasland.discord.commands.prefix.PrefixVerify;
import com.kardasland.discord.commands.slash.SlashReward;
import com.kardasland.discord.commands.slash.SlashVerify;
import com.kardasland.discord.models.PrefixDynamic;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class MessageReact extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (event.isWebhookMessage() || event.getMember() == null){
            return;
        }
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            for (PrefixDynamic dynamic : VeldoryaJDA.instance.getEmbedCommandsList()){
                if (content.matches("^"+dynamic.getCommand() +"$")){
                    event.getChannel().asTextChannel().sendMessageEmbeds(dynamic.getEmbed().buildEmbed()).queue();
                    return;
                }
            }
        }else {
            // No permission
            if (ConfigManager.get("config.yml").getBoolean("modules.dailyRewardModule")){
                if (event.getChannel().getId().equals(ConfigManager.get("config.yml").getString("discord.channels.dailyreward"))){
                    return;
                }
            }
            if (ConfigManager.get("config.yml").getBoolean("modules.verifyModule")){
                if (event.getChannel().getId().equals(ConfigManager.get("config.yml").getString("discord.channels.verify"))){
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
}
