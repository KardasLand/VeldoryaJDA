package com.kardasland.discord.commands.slash;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.sun.corba.se.impl.presentation.rmi.ExceptionHandlerImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class SlashVerify extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("verify") || event.getName().equals("doğrula") || event.getName().equals("eşle")) {
            event.deferReply(true).queue();

            if (!event.getChannel().getId().equals(ConfigManager.get("config.yml").getString("discord.channels.verify"))){
                CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.cannotUseInChannel");
                event.getHook().setEphemeral(true).editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                return;
            }
            String text = event.getOption("code").getAsString();
            try {
                DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayer(event.getMember().getId());
                if (dPlayer != null && dPlayer.getVerified()){
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.verify.alreadyVerified");
                    event.getHook().setEphemeral(true).editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                    return;
                }
            }catch (Exception ex){
                ex.printStackTrace();
                return;
            }
            //System.out.println("pass 1");
            for (Map.Entry<String, Integer> map : VeldoryaJDA.instance.verifyMap.entrySet()){
                if (text.equals(map.getValue().toString())){
                    //System.out.println("pass 2: " + text);
                    String targetuuid = map.getKey();
                    DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayerbyUUID(targetuuid);
                    //System.out.println("pass 3: " + dPlayer.getPlayer());
                    //System.out.println("pass 4");
                    dPlayer.setDiscordID(event.getMember().getId());
                    if (dPlayer.getVerified()){
                        CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.verify.alreadyVerified");
                        event.getHook().setEphemeral(true).editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                        return;
                    }
                    dPlayer.setVerified(true);
                    dPlayer.setPlayerUUID(map.getKey());
                    dPlayer.processGroups();
                    dPlayer.update();
                    //System.out.println("pass 5");
                    VeldoryaJDA.instance.getDPlayerCache().remove(dPlayer.getPlayer());
                    VeldoryaJDA.instance.getDPlayerCache().add(dPlayer);

                    VeldoryaJDA.instance.verifyMap.remove(map.getKey(), map.getValue());
                    //System.out.println(dPlayer);
                    //System.out.println("pass 6");
                    try {
                        if (ConfigManager.get("config.yml").getBoolean("changePlayerNameAfterVerify")){
                            if (PermissionUtil.canInteract(event.getGuild().getSelfMember(), event.getMember())){
                                event.getMember().modifyNickname(dPlayer.getPlayer().getName()).queue();
                            }
                        }
                    }catch (Exception exception){
                        continue;
                    }
                    //System.out.println("pass 7");
                    List<String> uuids = ConfigManager.get("tempdata.yml").getStringList("players");
                    if (!uuids.contains(dPlayer.getPlayer().getUniqueId().toString())){
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                for (String key : ConfigManager.get("config.yml").getStringList("discord.verifyCommandList")){
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), key.replaceAll("%player%", Bukkit.getPlayer(UUID.fromString(targetuuid)).getName()));
                                }
                            }
                        }.runTask(VeldoryaJDA.instance);
                        uuids.add(dPlayer.getPlayer().getUniqueId().toString());
                        ConfigManager.set("tempdata.yml", "players", uuids);
                        ConfigManager.save("tempdata.yml");
                        ConfigManager.reload("tempdata.yml");
                    }

                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.verify.success");
                    customEmbed.setDesc(customEmbed.getDesc().replaceAll("%player%", Bukkit.getPlayer(UUID.fromString(targetuuid)).getName()));
                    event.getHook().setEphemeral(true).editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                    return;
                }
            }
            CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.verify.invalidToken");
            event.getHook().setEphemeral(true).editOriginalEmbeds(customEmbed.buildEmbed()).queue();
        }
    }
}
