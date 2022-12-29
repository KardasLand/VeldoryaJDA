package com.kardasland.discord.commands.prefix;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import static com.kardasland.veldoryadiscord.Utils.deleteAfter;


public class PrefixSudo extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String prefix = ConfigManager.get("config.yml").getString("discord.prefix");
        if (content.length() > 6 && content.startsWith(prefix+"sudo")){
            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                event.getMessage().delete().queue();
                String text = content.substring(6);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), text);
                    }
                }.runTask(VeldoryaJDA.instance);
                event.getChannel().sendMessage("Success!").queue(queue -> deleteAfter(queue, 10));
            }else {
                event.getChannel().sendMessage("Bunun için \"Yönetici\" yetkisine sahip olma gerek!").queue(queue -> deleteAfter(queue, 10));
            }

        }
    }
}

// .queue(queue -> deleteAfter(queue, 10));