package com.kardasland.spigot.commands;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class VerifyCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayer(player);
            if (!dPlayer.getVerified()){
                int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
                VeldoryaJDA.instance.verifyMap.put(player.getUniqueId().toString(), code);
                Utils.noPrefix(player, ConfigManager.get("messages.yml").getString("server.createVerify").replaceAll("%code%", String.valueOf(code)));
                return true;
            }else {
                Utils.noPrefix(player, ConfigManager.get("messages.yml").getString("server.alreadyVerified"));
                return true;
            }
        }
        return true;
    }


}
