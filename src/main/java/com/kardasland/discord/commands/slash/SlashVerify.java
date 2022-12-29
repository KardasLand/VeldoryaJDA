package com.kardasland.discord.commands.slash;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

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
            for (Map.Entry<String, Integer> map : VeldoryaJDA.instance.verifyMap.entrySet()){
                if (text.equals(map.getValue().toString())){
                    String targetuuid = map.getKey();
                    DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayerbyUUID(targetuuid);
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
                    VeldoryaJDA.instance.verifyMap.remove(map.getKey(), map.getValue());
                    //System.out.println(dPlayer);

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            for (String key : ConfigManager.get("config.yml").getStringList("discord.verifyCommandList")){
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), key.replaceAll("%player%", Bukkit.getPlayer(UUID.fromString(targetuuid)).getName()));
                            }
                        }
                    }.runTask(VeldoryaJDA.instance);
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
