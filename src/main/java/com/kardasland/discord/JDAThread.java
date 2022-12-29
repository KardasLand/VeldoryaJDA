package com.kardasland.discord;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.commands.prefix.PrefixReward;
import com.kardasland.discord.commands.prefix.PrefixSudo;
import com.kardasland.discord.commands.prefix.PrefixVerify;
import com.kardasland.discord.commands.slash.SlashReward;
import com.kardasland.discord.commands.slash.SlashSudo;
import com.kardasland.discord.commands.slash.SlashVerify;
import com.kardasland.discord.events.MessageReact;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.kardasland.veldoryadiscord.VeldoryaJDA.jda;


public class JDAThread extends Thread {

    // THIS TOOK INSANE AMOUNT OF EFFORT, PLEASE DO NOT TOUCH IT!

    @Override
    public void run() {

        try {
            FileConfiguration cf = ConfigManager.get("config.yml");
            assert cf != null;
            JDABuilder builder = JDABuilder.createDefault(cf.getString("discord.token"));

            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            builder.setActivity(Activity.playing("Sunucu başlatılıyor.."));
            builder.addEventListeners(new MessageReact());
            builder.addEventListeners(new PrefixVerify());
            builder.addEventListeners(new PrefixSudo());
            builder.addEventListeners(new PrefixReward());
            builder.addEventListeners(new SlashReward());
            builder.addEventListeners(new SlashSudo());
            builder.addEventListeners(new SlashVerify());

            jda = builder.build();
            jda.awaitReady();

            jda.updateCommands().addCommands(
                    Commands.slash("verify", "Link your minecraft username with your Discord tag.")
                            .addOption(OptionType.STRING, "code", "A 6 Digit code that we sent to you in minecraft.", true),
                    Commands.slash("doğrula", "Minecraft ve discord hesaplarının linklenmesini sağlar.")
                            .addOption(OptionType.STRING, "code", "Sunucu içinden gönderdiğimiz 6 haneli kod.", true),
                    Commands.slash("eşle", "Minecraft ve discord hesaplarının linklenmesini sağlar.")
                            .addOption(OptionType.STRING, "code", "Sunucu içinden gönderdiğimiz 6 haneli kod.", true),
                    Commands.slash("sudo", "Sends commands to the server. Admin use only.")
                            .addOption(OptionType.STRING, "command", "Command that will be dispatched by console.", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                    Commands.slash("dailyreward", "A command to get your daily reward."),
                    Commands.slash("günlüködül", "Günlük ödülünü almanı sağlar.")
            ).queue();


            Guild guild = jda.getGuildById(cf.getString("discord.guild"));
            VeldoryaJDA.instance.setGuild(guild);
            System.out.println("Connected to Discord Bot successfully.");

            VeldoryaJDA.instance.isFullyLoaded = true;


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
