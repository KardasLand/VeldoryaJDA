package com.kardasland.discord;

import com.google.common.base.Ticker;
import com.kardasland.data.ConfigManager;
import com.kardasland.discord.commands.prefix.PrefixReward;
import com.kardasland.discord.commands.prefix.PrefixSudo;
import com.kardasland.discord.commands.prefix.PrefixVerify;
import com.kardasland.discord.commands.slash.SlashReward;
import com.kardasland.discord.commands.slash.SlashSudo;
import com.kardasland.discord.commands.slash.SlashVerify;
import com.kardasland.discord.events.MessageReact;
import com.kardasland.discord.events.TwoFactorFeedback;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.discord.ticket.CustomButton;
import com.kardasland.discord.ticket.ModalListener;
import com.kardasland.discord.ticket.OpenTicketEvent;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kardasland.veldoryadiscord.VeldoryaJDA.jda;


public class JDAThread extends Thread {

    // THIS TOOK INSANE AMOUNT OF EFFORT, PLEASE DO NOT TOUCH IT!

    @Override
    public void run() {

        try {
            FileConfiguration cf = ConfigManager.get("config.yml");
            FileConfiguration ticketSystem = ConfigManager.get("ticketsystem.yml");
            assert cf != null;
            JDABuilder builder = JDABuilder.createDefault(cf.getString("discord.token"));
            List<SlashCommandData> commandsList = new ArrayList<>();

            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
            builder.setMemberCachePolicy(MemberCachePolicy.ALL);


            builder.setActivity(Activity.playing("Sunucu başlatılıyor.."));


            builder.addEventListeners(new MessageReact());
            if (ConfigManager.get("config.yml").getBoolean("modules.verifyModule")){
                builder.addEventListeners(new PrefixVerify());
                builder.addEventListeners(new SlashVerify());
                commandsList.add(Commands.slash("verify", "Link your minecraft username with your Discord tag.")
                        .addOption(OptionType.STRING, "code", "A 6 Digit code that we sent to you in minecraft.", true));
                commandsList.add(Commands.slash("doğrula".toLowerCase(), "Minecraft ve discord hesaplarının linklenmesini sağlar.")
                        .addOption(OptionType.STRING, "code", "Sunucu içinden gönderdiğimiz 6 haneli kod.", true));
                commandsList.add(Commands.slash("eşle".toLowerCase(), "Minecraft ve discord hesaplarının linklenmesini sağlar.")
                        .addOption(OptionType.STRING, "code", "Sunucu içinden gönderdiğimiz 6 haneli kod.", true));
            }
            if (ConfigManager.get("config.yml").getBoolean("modules.sudoModule")){
                builder.addEventListeners(new PrefixSudo());
                builder.addEventListeners(new SlashSudo());
                commandsList.add(Commands.slash("sudo", "Sends commands to the server. Admin use only.")
                        .addOption(OptionType.STRING, "command", "Command that will be dispatched by console.", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
            }
            if (ConfigManager.get("config.yml").getBoolean("modules.2faModule")){
                builder.addEventListeners(new TwoFactorFeedback());
            }
            if (ConfigManager.get("config.yml").getBoolean("modules.ticketModule")){
                builder.addEventListeners(new OpenTicketEvent());
                builder.addEventListeners(new ModalListener());
            }
            if (ConfigManager.get("config.yml").getBoolean("modules.dailyRewardModule")){
                builder.addEventListeners(new PrefixReward());
                builder.addEventListeners(new SlashReward());
                commandsList.add(Commands.slash("dailyreward", "A command to get your daily reward."));
                commandsList.add(Commands.slash("günlüködül".toLowerCase(), "Günlük ödülünü almanı sağlar."));
            }

            jda = builder.build();
            jda.awaitReady();

            jda.updateCommands().addCommands(commandsList).queue();


            /*jda.updateCommands().addCommands(
                    Commands.slash("verify", "Link your minecraft username with your Discord tag.")
                            .addOption(OptionType.STRING, "code", "A 6 Digit code that we sent to you in minecraft.", true),
                    Commands.slash("doğrula".toLowerCase(), "Minecraft ve discord hesaplarının linklenmesini sağlar.")
                            .addOption(OptionType.STRING, "code", "Sunucu içinden gönderdiğimiz 6 haneli kod.", true),
                    Commands.slash("eşle".toLowerCase(), "Minecraft ve discord hesaplarının linklenmesini sağlar.")
                            .addOption(OptionType.STRING, "code", "Sunucu içinden gönderdiğimiz 6 haneli kod.", true),
                    Commands.slash("sudo", "Sends commands to the server. Admin use only.")
                            .addOption(OptionType.STRING, "command", "Command that will be dispatched by console.", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("dailyreward", "A command to get your daily reward."),
                    Commands.slash("günlüködül".toLowerCase(), "Günlük ödülünü almanı sağlar.")
            ).queue();*/


            Guild guild = jda.getGuildById(cf.getString("discord.guild"));
            VeldoryaJDA.instance.setGuild(guild);
            guild.loadMembers();
            System.out.println("Connected to Discord Bot successfully.");


            // Fixed verify id cannot be empty issue.
            if (VeldoryaJDA.instance.tryLoadModule("verifyModule", "verify")){
                TextChannel channel = guild.getTextChannelById(ConfigManager.get("config.yml").getString("discord.channels.verify"));
                channel.upsertPermissionOverride(Objects.requireNonNull(guild.getMemberById(jda.getSelfUser().getId()))).setAllowed(Permission.MESSAGE_MANAGE).queue();
            }
            //System.out.println("Permission MANAGE_CHANNEL: " + PermissionUtil.checkPermission(guild.getSelfMember(), Permission.MANAGE_CHANNEL));
            if (ConfigManager.get("config.yml").getBoolean("modules.ticketModule")){
                TextChannel ticketChannel = guild.getTextChannelById(ticketSystem.getString("ticketChannel"));
                if (ticketChannel != null){
                    MessageHistory history = MessageHistory.getHistoryFromBeginning(ticketChannel).complete();
                    List<Message> mess = history.getRetrievedHistory();
                    if (mess.size() == 0){
                        CustomEmbed customEmbed = new CustomEmbed(ticketSystem, "messages.ticketInitialMessage");

                        ItemComponent openComponent = new CustomButton("openTicket", "velOpenTicket").buildButton();
                        ItemComponent helpComponent =  new CustomButton("openTicketHelp", "velHelpTicket").buildButton();
                        ticketChannel.sendMessageEmbeds(customEmbed.buildEmbed()).addActionRow(
                                openComponent, helpComponent
                        ).queue();
                    }
                }
            }



            VeldoryaJDA.instance.isFullyLoaded = true;
        } catch (InterruptedException e) {
            VeldoryaJDA.instance.isFullyLoaded = true;
            throw new RuntimeException(e);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
