package com.kardasland.discord.commands.prefix;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;


public class PrefixVerify extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        //System.out.println(content);
        String prefix = ConfigManager.get("config.yml").getString("discord.prefix");
        if (!event.getChannel().getId().equals(ConfigManager.get("config.yml").getString("discord.channels.verify"))){
            return;
        }
        event.getMessage().delete().queue();
        if (content.length() > 8 && (content.startsWith(prefix+"verify") || content.startsWith(prefix+"doğrula") || content.startsWith(prefix+"eşle"))){
            String text = content.replaceAll("\\D+","");
            try {
                DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayer(event.getMember().getId());
                if (dPlayer != null && dPlayer.getVerified()){
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.verify.alreadyVerified");
                    event.getChannel().sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> Utils.deleteAfter(queue, 10));
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
                        event.getChannel().sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> Utils.deleteAfter(queue, 10));
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
                    event.getChannel().sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> Utils.deleteAfter(queue, 10));
                    return;
                }
            }
            CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.verify.invalidToken");
            event.getChannel().sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> Utils.deleteAfter(queue, 10));
        }
    }
}
