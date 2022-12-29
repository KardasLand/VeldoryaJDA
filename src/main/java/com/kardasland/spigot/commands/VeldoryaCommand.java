package com.kardasland.spigot.commands;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VeldoryaCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("yenile") || args[0].equalsIgnoreCase("reload")) {
                    if (Utils.hasPerms(player, "veldoryadiscord.reload")) {
                        VeldoryaJDA.instance.reloadConfigurations();
                        Utils.noPrefix(player, ConfigManager.get("messages.yml").getString("server.reloadedSuccessfully"));
                    }
                    return true;
                }
            }
            List<String> suggestList = ConfigManager.get("messages.yml").getStringList("server.commands.veldoryaDiscordHelp");
            for (String suggestion : suggestList){
                Utils.noPrefix(player, suggestion);
            }
        }
        return true;
    }
}
