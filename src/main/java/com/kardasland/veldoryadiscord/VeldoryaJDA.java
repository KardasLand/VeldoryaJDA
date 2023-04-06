package com.kardasland.veldoryadiscord;

import com.kardasland.data.ConfigManager;
import com.kardasland.data.ISQLOperations;
import com.kardasland.data.sql.MySQL;
import com.kardasland.data.sql.SQLite;
import com.kardasland.discord.JDAThread;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.discord.models.PrefixDynamic;
import com.kardasland.spigot.commands.*;
import com.kardasland.spigot.events.*;
import com.kardasland.spigot.runnable.AutoSaveRunnable;
import com.kardasland.spigot.runnable.OnlineCounterRunnable;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.kardasland.veldoryadiscord.models.DailyReward;
import com.kardasland.veldoryadiscord.models.cache.DPlayerCache;
import com.kardasland.veldoryadiscord.models.cache.PermRoleCache;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitWorker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kardasland.veldoryadiscord.Utils.copyInputStreamToFile;

public final class VeldoryaJDA extends JavaPlugin {


    public static VeldoryaJDA instance;
    public Thread mainJDAThread;
    public Map<String, Integer> verifyMap;

    @Getter @Setter List<PrefixDynamic> embedCommandsList;
    @Getter @Setter List<DailyReward> dailyRewards;

    @Getter @Setter public DPlayerCache dPlayerCache;
    @Getter @Setter public PermRoleCache permRoleCache;

    @Getter @Setter
    public ISQLOperations isqlOperations;

    @Getter @Setter Guild guild;
    @Getter @Setter String consoleChannel;

    LogServerAppender logServerAppender;

    @Getter @Setter public List<DPlayer> dPlayerList;

    public boolean isFullyLoaded = false;

    public static JDA jda;

    Logger log;


    /**
     * Hello to anyone who edits these files.
     * 2023 for me is YKS (Uni entrance exam) year, so I don't have proper mental for cleaning and optimizing codes.
     * Because of this, my code quality should be trash xd
     * But you should not have a hard time reading lines.
     * I'm not giving support for this plugin. I do not have enough time for this.
     * But if you want to read/modify it but can't read it or too messy, or if you want to commission me after June 2023, you can contact me.
     * - KardasLand#9552
     */



    @Override
    public void onEnable() {
        try {
            instance = this;

            if (!setupDependencyInjection()){
                VeldoryaJDA.instance.isFullyLoaded = true;
                return;
            }

            this.verifyMap = new HashMap<>();
            this.embedCommandsList = new ArrayList<>();
            this.dailyRewards = new ArrayList<>();

            ConfigManager.load("config.yml");
            ConfigManager.load("roles.yml");
            ConfigManager.load("commands.yml");
            ConfigManager.load("dailyrewards.yml");
            ConfigManager.load("messages.yml");
            ConfigManager.load("ticketdata.yml");
            ConfigManager.load("ticketsystem.yml");
            ConfigManager.load("tempdata.yml");

            loadEmbeds();
            loadRewards();
            loadModules();

            Bukkit.getPluginManager().registerEvents(new OtherEventsHandler(), this);

            dPlayerCache = new DPlayerCache();
            permRoleCache = new PermRoleCache();
            permRoleCache.loadRoles();


            this.isqlOperations = ConfigManager.get("config.yml").getString("sql.type").equalsIgnoreCase("mysql") ? new MySQL() : new SQLite();
            isqlOperations.setupHikari();
            isqlOperations.checkDatabase();

            if (!ConfigManager.get("config.yml").isSet("discord.token")){
                Utils.noPrefix("You need to fill discord token and channels in config.yml in order to function. Shutting down for now..");
                VeldoryaJDA.instance.isFullyLoaded = true;
                this.getPluginLoader().disablePlugin(this);
                return;
            }

            getCommand("veldoryadiscord").setExecutor(new VeldoryaCommand());
            getCommand("dinfo").setExecutor(new SeeCommand());

            setupBot();

            int autosave = ConfigManager.get("config.yml").getInt("autosaveTime") * 20;
            new AutoSaveRunnable().runTaskTimerAsynchronously(this, autosave, autosave);
            new OnlineCounterRunnable().runTaskTimer(this, 20 * 10, 20 * 10);

            if (!isFullyLoaded){
                isFullyLoaded = true;
            }

        }catch (SQLException ex){
            System.out.println("SQL Exception! Printing the error and shutting down the plugin.");
            VeldoryaJDA.instance.isFullyLoaded = true;
            ex.printStackTrace();
            this.getPluginLoader().disablePlugin(this);
        } catch (NullPointerException ex){
            System.out.println("Config Error! Please check everything in the config file.");
            VeldoryaJDA.instance.isFullyLoaded = true;
            this.getPluginLoader().disablePlugin(this);
        } catch (InterruptedException | NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 IOException | RuntimeException ex){
            System.out.println("Interrupted/JDA Exception! Printing the error and shutting down the plugin.");
            VeldoryaJDA.instance.isFullyLoaded = true;
            ex.printStackTrace();
            this.getPluginLoader().disablePlugin(this);
        }
    }

    public boolean tryLoadModule(String module, String... channels){
        if (ConfigManager.get("config.yml").getBoolean("modules."+module)){
            for (String channel : channels){
                return ConfigManager.get("config.yml").getString("discord.channels." + channel) != null && !ConfigManager.get("config.yml").getString("discord.channels." + channel).isEmpty();
            }
        }
        return false;
    }

    private void loadModules() {
        if (tryLoadModule("suggestionModule", "suggestions")){
            getCommand("oneri").setExecutor(new SuggestCommand());
        }
        if (tryLoadModule("verifyModule", "verify")){
            getCommand("dogrula").setExecutor(new VerifyCommand());
            getCommand("unlink").setExecutor(new UnlinkCommand());
        }
        if (tryLoadModule("discordChatModule", "discordchat")){
            Bukkit.getPluginManager().registerEvents(new ChatEvent(), this);
        }
        if (tryLoadModule("joinLeaveModule", "joinleave")){
            Bukkit.getPluginManager().registerEvents(new JoinLeaveEvent(), this);
        }
        if (ConfigManager.get("config.yml").getBoolean("modules.2faModule")){
            Bukkit.getPluginManager().registerEvents(new TwoFactorProtection(), this);
            TwoFactorProtection.ipRememberEnabled = ConfigManager.get("config.yml").getBoolean("remember2FAIPAddresses");
        }
        if (tryLoadModule("consoleModule", "consoleMirror")){
            getLogger().info("ConsoleMirror system is currently on development. Disabling the module..");
        }
    }

    private boolean setupDependencyInjection() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (Version.getServerVersion(this.getServer()).isOlderThan(Version.v1_16_R3)){
            try {
                ClassLoader classLoader = new ClassLoader();

                String libFolder = VeldoryaJDA.instance.getDataFolder() + File.separator + "libs";
                File lib = new File(libFolder);
                if (!lib.exists()){
                    lib.mkdirs();
                }

                String jdaPath = VeldoryaJDA.instance.getDataFolder() + File.separator + "libs" + File.separator + "jda.jar";
                File jdaFile = new File(jdaPath);
                if (!jdaFile.exists()){
                    jdaFile.createNewFile();
                    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("jda.jar");
                    assert inputStream != null;
                    copyInputStreamToFile(inputStream, jdaFile);
                }
                classLoader.addDependency(jdaFile);
                //loadLibrary(jdaFile, jdaPath);

                String hikariPath = VeldoryaJDA.instance.getDataFolder() + File.separator + "libs" + File.separator + "hikari.jar";
                File hikariFile = new File(hikariPath);
                if (!hikariFile.exists()){
                    hikariFile.createNewFile();
                    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("hikari.jar");
                    assert inputStream != null;
                    copyInputStreamToFile(inputStream, hikariFile);
                }
                classLoader.addDependency(hikariFile);
                //loadLibrary(hikariFile, hikariPath);

                String mysqlPath = VeldoryaJDA.instance.getDataFolder() + File.separator + "libs" + File.separator + "mysql.jar";
                File mysqlFile = new File(mysqlPath);
                if (!mysqlFile.exists()){
                    mysqlFile.createNewFile();
                    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("mysql.jar");
                    assert inputStream != null;
                    copyInputStreamToFile(inputStream, mysqlFile);
                }
                classLoader.addDependency(mysqlFile);
                //loadLibrary(mysqlFile, mysqlPath);
            }catch (IllegalAccessException exception){
                getLogger().severe("Exception Occurred! To make the plugin size small, we have to use dependency injection.");
                getLogger().severe("You need to use these flags as your java arguments:");
                getLogger().severe("--add-opens java.base/jdk.internal.loader=ALL-UNNAMED --add-opens jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED");
                getLogger().severe("Shutting down the plugin..");
                VeldoryaJDA.instance.isFullyLoaded = true;
                return false;
            }
        }
        return true;
    }

    private void loadRewards() {
        FileConfiguration cf = ConfigManager.get("dailyrewards.yml");
        assert cf != null;
        for (String key : cf.getConfigurationSection("dailyrewards.").getKeys(false)){
            DailyReward dailyReward = new DailyReward(key);
            List<String> commands = cf.getStringList("dailyrewards."+key+".commands");
            dailyReward.setCommands(commands);
            dailyReward.setCooldown(cf.getLong("dailyrewards."+key+".cooldown"));
            dailyRewards.add(dailyReward);
            CustomEmbed embed = new CustomEmbed(cf.getString("dailyrewards."+key+".message.title"), cf.getString("dailyrewards."+key+".message.description"), Utils.getAWTColor(cf.getString("dailyrewards."+key+".message.color")));
            embed.setThumbnail(cf.getString("dailyrewards."+key+".message.thumbnail"));
            embed.setFooter(new CustomEmbed.Footer(cf.getString("dailyrewards."+key+".message.footer"), (cf.isSet("dailyrewards."+key+".message.icon") ? cf.getString("dailyrewards."+key+".message.icon") : null)));
            dailyReward.setEmbed(embed);
        }
    }

    private void loadEmbeds() {

        FileConfiguration cf = ConfigManager.get("commands.yml");
        assert cf != null;
        for (String key : cf.getConfigurationSection("commands.").getKeys(false)){
            PrefixDynamic prefixDynamic = new PrefixDynamic(ConfigManager.get("config.yml").getString("discord.prefix") + key);
            String desc = cf.getString("commands."+key+".desc");
            prefixDynamic.setDesc(desc);
            String title = cf.getString("commands."+key+".title");
            String color = cf.getString("commands."+key+".color");
            CustomEmbed customEmbed = new CustomEmbed(title, desc, Utils.getAWTColor(color));
            if (cf.isSet("commands."+key+".thumbnail")){
                customEmbed.setThumbnail(cf.getString("commands."+key+".thumbnail"));
            }
            if (cf.isSet("commands."+key+".footer")){
                customEmbed.setFooter(new CustomEmbed.Footer(cf.getString("commands."+key+".footer"), (cf.isSet("commands."+key+".icon") ? cf.getString("commands."+key+".icon") : null)));
            }

            try {
                if (cf.isConfigurationSection("commands."+key+".fields")){
                    for (String inlines : cf.getConfigurationSection("commands."+key+".fields.").getKeys(false)){
                        if (cf.isSet("commands."+key+".fields."+inlines+".desc")){
                            String fieldTitle = cf.getString("commands."+key+".fields."+inlines+".title");
                            String fieldDesc = cf.getString("commands."+key+".fields."+inlines+".desc");
                            Boolean inline = cf.getBoolean("commands."+key+".fields."+inlines+".inline");
                            CustomEmbed.EmbedField embedField = new CustomEmbed.EmbedField(fieldTitle, fieldDesc, inline);
                            customEmbed.getEmbedFieldList().add(embedField);
                        }else {
                            break;
                        }
                    }
                }
            }catch (NullPointerException exception){
                CustomEmbed.EmbedField embedField = new CustomEmbed.EmbedField("", "", false);
                customEmbed.getEmbedFieldList().add(embedField);
                continue;
            }
            prefixDynamic.setEmbed(customEmbed);
            getEmbedCommandsList().add(prefixDynamic);
        }
    }

    public void setupBot() throws InterruptedException {
        this.mainJDAThread = new JDAThread();
        mainJDAThread.start();
    }


    @Override
    public void onDisable() {
        if (logServerAppender != null){
            log.exit();
            log.setLevel(Level.OFF);
            logServerAppender.stop();
            log.removeAppender(logServerAppender);
            //System.out.println("a");
        }
        System.out.println("Closing all async threads, this could take 5-10 seconds..");
        for (int i=0; i<50; ++i) {
            List<BukkitWorker> workers = Bukkit.getScheduler().getActiveWorkers();
            boolean taskFound = false;
            for (BukkitWorker worker : workers) {
                if (worker.getOwner().equals(this)) {
                    taskFound = true;
                    break; // inner loop
                }
            }
            if (!taskFound) {
                break; // outer loop
            }
            try {
                Thread.sleep(100); // msec
            }
            catch (InterruptedException e) {}
        }
        if (isqlOperations != null){
            isqlOperations.closeHikari();
        }
        if (jda != null){
            OkHttpClient client = jda.getHttpClient();
            client.connectionPool().evictAll();
            client.dispatcher().executorService().shutdown();
            jda.shutdownNow();
            mainJDAThread.interrupt();
            jda = null;
        }
    }
    public void reloadConfigurations() {
        ConfigManager.reload("config.yml");
        ConfigManager.reload("commands.yml");
        ConfigManager.reload("dailyrewards.yml");
        ConfigManager.reload("messages.yml");
        ConfigManager.reload("roles.yml");
        ConfigManager.reload("ticketsystem.yml");
    }
}
