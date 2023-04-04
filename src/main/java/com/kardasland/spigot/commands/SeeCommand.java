package com.kardasland.spigot.commands;

import com.kardasland.data.ConfigManager;
import com.kardasland.data.UUIDFetcher;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class SeeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (args.length == 1) {
               if (Utils.hasPerms(player, "veldoryadiscord.dinfo")){
                   Utils.noPrefix(player, "Biraz bekleyin..");
                   Player target = Bukkit.getPlayer(args[0]);
                   if (target != null && target.isOnline()){
                       VeldoryaJDA.instance.getIsqlOperations().getDPlayerAsync(target,dPlayer -> {
                           Utils.noPrefix(player, "User Info:");
                           Utils.noPrefix(player, "User: " + dPlayer.getPlayer().getName());
                           Utils.noPrefix(player, "UUID: " + dPlayer.getPlayer().getUniqueId());
                           Utils.noPrefix(player, "Verify status: " + dPlayer.getVerified());
                           Utils.noPrefix(player, "DiscordID: " + dPlayer.getDiscordID());
                           Utils.noPrefix(player, "Groups: " + dPlayer.getGroups());
                       });
                   }else {
                       UUID uuid = Bukkit.getOnlineMode() ? UUIDFetcher.getUUID(args[0]) : UUID.nameUUIDFromBytes(("OfflinePlayer:"+args[0]).getBytes(StandardCharsets.UTF_8));;
                       //System.out.println(uuid.toString());
                       VeldoryaJDA.instance.getIsqlOperations().getDPlayerAsync(uuid, dPlayer -> {
                           Utils.noPrefix(player, "User Info:");
                           Utils.noPrefix(player, "User: " + dPlayer.getOfflinePlayer().getName());
                           Utils.noPrefix(player, "UUID: " + dPlayer.getOfflinePlayer().getUniqueId());
                           Utils.noPrefix(player, "Verify status: " + dPlayer.getVerified());
                           Utils.noPrefix(player, "DiscordID: " + dPlayer.getDiscordID());
                           Utils.noPrefix(player, "Groups: " + dPlayer.getGroups());
                       });
                   }

               }
            }
        }
        return true;
    }
}
