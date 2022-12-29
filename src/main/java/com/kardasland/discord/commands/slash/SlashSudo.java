package com.kardasland.discord.commands.slash;

import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SlashSudo extends ListenerAdapter {



    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("sudo")) {
            event.deferReply().queue(); // Tell discord we received the command, send a thinking... message to the user
            String text = event.getOption("command").getAsString();
            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), text);
                    }
                }.runTask(VeldoryaJDA.instance);
                event.reply("Success!").setEphemeral(true).queue();
            }
        }
    }
}
