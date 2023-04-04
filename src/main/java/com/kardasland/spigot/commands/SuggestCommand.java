package com.kardasland.spigot.commands;

import com.kardasland.data.ConfigManager;
import com.kardasland.data.CooldownHandler;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.Utils;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SuggestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (args.length == 0){
                String suggestUsage = ConfigManager.get("messages.yml").getString("server.commands.suggestUsage");
                Utils.noPrefix(player, suggestUsage);
                return true;
            }
            if (!CooldownHandler.isInCooldown(player.getUniqueId(), "suggest")) {
                FileConfiguration cf = ConfigManager.get("config.yml");
                TextChannel channel = VeldoryaJDA.jda.getGuildById(cf.getString("discord.guild")).getTextChannelById(cf.getString("discord.channels.suggestions"));
                StringBuilder sm = new StringBuilder();
                for (String arg : Arrays.stream(args).collect(Collectors.toList())){
                    sm.append(arg).append(" ");
                }
                CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.suggestion");
                customEmbed.setDesc(customEmbed.getDesc().replaceAll("%player%", player.getName()).replaceAll("%suggestion%", sm.toString()));
                channel.sendMessageEmbeds(customEmbed.buildEmbed()).queue(message -> {
                    message.addReaction(Emoji.fromFormatted("✅")).queue();
                    message.addReaction(Emoji.fromFormatted("❌")).queue();
                });

                CooldownHandler cooldownHandler = new CooldownHandler(player.getUniqueId(), "suggest", ConfigManager.get("config.yml").getInt("suggestionTime"));
                cooldownHandler.start();
                Utils.noPrefix(player, ConfigManager.get("messages.yml").getString("server.suggestSuccess"));
            }else {
                Utils.noPrefix(player, ConfigManager.get("messages.yml").getString("server.suggestCooldown").replaceAll("%time%", Utils.formatTime(CooldownHandler.getTimeLeft(player.getUniqueId(), "suggest"))));
            }
        }
        return true;
    }
}
