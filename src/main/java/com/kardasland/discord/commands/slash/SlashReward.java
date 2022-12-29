package com.kardasland.discord.commands.slash;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.kardasland.veldoryadiscord.models.DailyReward;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.UUID;

import static com.kardasland.veldoryadiscord.Utils.compareTwoTimeStamps;


public class SlashReward extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("dailyreward") || event.getName().equals("günlüködül")) {
            event.deferReply().queue();
            if (!event.getChannel().getId().equals(ConfigManager.get("config.yml").getString("discord.channels.dailyreward"))){
                CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.cannotUseInChannel");
                event.getHook().editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                return;
            }
            try {
                DPlayer dPlayer = VeldoryaJDA.instance.getDPlayerCache().findPlayer(event.getMember().getId());
                if (dPlayer == null){
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.notOnline");
                    event.getHook().editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                    return;
                }
                if (!dPlayer.getVerified()){
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.notVerified");
                    event.getHook().editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                    return;
                }
                Player target = Bukkit.getPlayer(UUID.fromString(dPlayer.getPlayerUUID()));
                if ( target != null && target.isOnline()){
                    Timestamp timestamp = dPlayer.getTimestamp();
                    if (timestamp != null){
                        Timestamp current = new Timestamp(System.currentTimeMillis());
                        long second = compareTwoTimeStamps(current, timestamp);
                        for (DailyReward reward : VeldoryaJDA.instance.getDailyRewards()){
                            if (reward.checkGroup(dPlayer)){
                                if (reward.checkTime(second)){
                                    dPlayer.setTimestamp(current);
                                    dPlayer.processGroups();
                                    dPlayer.update();
                                    reward.execute(target);
                                    event.getHook().editOriginalEmbeds(reward.getEmbed().buildEmbed()).queue();
                                    return;
                                }else {
                                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.cantRewardNow");
                                    customEmbed.setDesc(customEmbed.getDesc().replaceAll("%remaining%", Utils.formatTime(reward.getCooldown() - second)));
                                    event.getHook().editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                                    return;
                                }
                            }
                        }
                        CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.cantReward");
                        event.getHook().editOriginalEmbeds(customEmbed.buildEmbed()).queue();
                    }else {
                        // This should be the first time. Depends on the database configuration. Some engines set it to null, some set it to current time.
                        Timestamp current = new Timestamp(System.currentTimeMillis());
                        for (DailyReward reward : VeldoryaJDA.instance.getDailyRewards()){
                            if (reward.checkGroup(dPlayer)){
                                dPlayer.setTimestamp(current);
                                dPlayer.processGroups();
                                dPlayer.update();
                                reward.execute(target);
                                event.getHook().editOriginalEmbeds(reward.getEmbed().buildEmbed()).queue();
                                return;
                            }
                        }
                    }
                }else {
                    CustomEmbed customEmbed = new CustomEmbed(ConfigManager.get("messages.yml"), "discord.embeds.reward.notOnline");
                    event.getHook().editOriginalEmbeds(customEmbed.buildEmbed()).queue();

                }
            }catch (Exception ex){
                ex.printStackTrace();
                event.getHook().editOriginal("An exception occurred.").queue();
            }
        }
    }

}
