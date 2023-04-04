package com.kardasland.spigot.commands;

import com.kardasland.data.ConfigManager;
import com.kardasland.data.UUIDFetcher;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.kardasland.veldoryadiscord.models.PermRole;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UnlinkCommand implements CommandExecutor {

    List<Player> unlinkList = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            FileConfiguration cf = ConfigManager.get("messages.yml");
            Guild guild = VeldoryaJDA.jda.getGuildById(ConfigManager.get("config.yml").getString("discord.guild"));
            //System.out.println("pass 1");
            if (args.length == 0) {
                //System.out.println("pass 2");
                DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayer(player);
                if (!dPlayer.getVerified()){
                    Utils.noPrefix(player, cf.getString("server.unlink.notVerified"));
                    return true;
                }
                if (!unlinkList.contains(player)){
                    //System.out.println("pass 2-1");
                    unlinkList.add(player);
                    Utils.noPrefix(player, cf.getString("server.unlink.confirmation"));
                    Bukkit.getScheduler().runTaskLater(VeldoryaJDA.instance, () -> {
                        unlinkList.remove(player);
                    } ,20 * 10);
                }else {
                    //System.out.println("pass 2-2");
                    unlinkList.remove(player);

                    removeRoles(dPlayer.getDiscordID());

                    //System.out.println("pass 2-3");

                    VeldoryaJDA.instance.getDPlayerCache().remove(player);
                    VeldoryaJDA.instance.getIsqlOperations().deletePlayer(dPlayer);
                    DPlayer renew = new DPlayer();
                    renew.setPlayerUUID(player.getUniqueId().toString());
                    renew.setPlayer(player.getPlayer());
                    renew.setVerified(false);

                    VeldoryaJDA.instance.getDPlayerCache().add(renew);
                    Utils.noPrefix(player, cf.getString("server.unlink.successOwn"));
                }
            }else if (args.length == 1){
                if (Utils.hasPerms(player,"veldoryadiscord.unlink.others")){
                    //System.out.println("pass 3");
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null && target.isOnline()){
                        //System.out.println("pass 4");
                        DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayer(target);
                        if (!dPlayer.getVerified()){
                            Utils.noPrefix(player, cf.getString("server.unlink.notVerified"));
                            return true;
                        }
                        //System.out.println("pass 4-1");

                        removeRoles(dPlayer.getDiscordID());


                        //System.out.println("pass 5");

                        VeldoryaJDA.instance.getDPlayerCache().remove(target);
                        VeldoryaJDA.instance.getIsqlOperations().deletePlayer(dPlayer);
                        //Resetting the cache.
                        DPlayer renew = new DPlayer();
                        renew.setPlayerUUID(target.getUniqueId().toString());
                        renew.setPlayer(target.getPlayer());
                        renew.setVerified(false);
                        //System.out.println("pass 6");
                        VeldoryaJDA.instance.getDPlayerCache().add(renew);
                        Utils.noPrefix(player, cf.getString("server.unlink.successOther"));
                        return true;
                    }else {
                        UUID uuid = Bukkit.getOnlineMode() ? UUIDFetcher.getUUID(args[0]) : UUID.nameUUIDFromBytes(("OfflinePlayer:"+args[0]).getBytes(StandardCharsets.UTF_8));;
                        VeldoryaJDA.instance.getIsqlOperations().getDPlayerAsync(uuid, (dPlayer1) ->{
                            if (!dPlayer1.getVerified()){
                                Utils.noPrefix(player, cf.getString("server.unlink.notVerified"));
                                return;
                            }
                            removeRoles(dPlayer1.getDiscordID());
                            VeldoryaJDA.instance.getIsqlOperations().deletePlayer(dPlayer1);
                            Utils.noPrefix(player, cf.getString("server.unlink.successOther"));
                            // Resetting the cache.
                        });
                    }
                }
            }
        }
        return true;
    }
    private void removeRoles(String discordID){
        Guild guild = VeldoryaJDA.jda.getGuildById(ConfigManager.get("config.yml").getString("discord.guild"));
        new BukkitRunnable(){
            @Override
            public void run() {
                for (PermRole allroles : VeldoryaJDA.instance.getPermRoleCache().getPermRoles()){
                    Role group = guild.getRoleById(allroles.getRoleid());
                    if (group != null){
                        guild.removeRoleFromMember(Objects.requireNonNull(guild.getMemberById(discordID)), group).queue();
                    }
                }
            }
        }.runTaskAsynchronously(VeldoryaJDA.instance);
    }
}
