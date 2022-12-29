package com.kardasland.discord.commands.prefix;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.kardasland.veldoryadiscord.models.DailyReward;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.UUID;

import static com.kardasland.veldoryadiscord.Utils.compareTwoTimeStamps;
import static com.kardasland.veldoryadiscord.Utils.deleteAfter;

public class PrefixReward extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();
        if (!channel.getId().equals(ConfigManager.get("config.yml").getString("discord.channels.dailyreward"))){
            return;
        }
        String prefix = ConfigManager.get("config.yml").getString("discord.prefix");
        if ((content.startsWith(prefix+"dailyreward") || content.startsWith(prefix+"günlüködül"))){
            try {
                DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayer(event.getMember().getId());
                if (dPlayer == null){
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.notOnline");
                    channel.sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> deleteAfter(queue, 10));
                    return;
                }
                if (!dPlayer.getVerified()){
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.notVerified");
                    channel.sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> deleteAfter(queue, 10));
                    return;
                }
                Player target = Bukkit.getPlayer(UUID.fromString(dPlayer.getPlayerUUID()));
                if ( target != null && target.isOnline()){
                    Timestamp timestamp = dPlayer.getTimestamp();
                    Timestamp current = new Timestamp(System.currentTimeMillis());
                    if (timestamp != null){
                        long second = compareTwoTimeStamps(current, timestamp);
                        for (DailyReward reward : VeldoryaJDA.instance.getDailyRewards()){
                            if (reward.checkGroup(dPlayer)){
                                if (reward.checkTime(second)){
                                    dPlayer.setTimestamp(current);
                                    dPlayer.processGroups();
                                    dPlayer.update();
                                    reward.execute(target);
                                    channel.sendMessageEmbeds(reward.getEmbed().buildEmbed()).queue(queue -> deleteAfter(queue, 10));
                                }else {
                                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.cantRewardNow");
                                    customEmbed.setDesc(customEmbed.getDesc().replaceAll("%remaining%", Utils.formatTime(reward.getCooldown() - second)));
                                    channel.sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> deleteAfter(queue, 10));
                                }
                                return;
                            }
                        }
                        CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.cantReward");
                        channel.sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> deleteAfter(queue, 10));
                    }else {
                        // This should be the first time. Depends on the database configuration. Some engines set it to null, some set it to current time.
                        for (DailyReward reward : VeldoryaJDA.instance.getDailyRewards()){
                            if (reward.checkGroup(dPlayer)){
                                dPlayer.setTimestamp(current);
                                dPlayer.processGroups();
                                dPlayer.update();
                                reward.execute(target);
                                channel.sendMessageEmbeds(reward.getEmbed().buildEmbed()).queue(queue -> deleteAfter(queue, 10));
                                return;
                            }
                        }
                    }
                }else {
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.notOnline");
                    channel.sendMessageEmbeds(customEmbed.buildEmbed()).queue(queue -> deleteAfter(queue, 10));
                }
            }catch (Exception ex){
                ex.printStackTrace();
                channel.sendMessage("An exception occured.").queue(queue -> deleteAfter(queue, 10));
            }
        }
    }
}
