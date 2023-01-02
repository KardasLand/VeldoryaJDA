package com.kardasland.veldoryadiscord;

import com.kardasland.data.ConfigManager;
import com.kardasland.data.ISQLOperations;
import com.kardasland.data.sql.MySQL;
import com.kardasland.data.sql.SQLite;
import com.kardasland.discord.JDAThread;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.discord.models.PrefixDynamic;
import com.kardasland.spigot.commands.SuggestCommand;
import com.kardasland.spigot.commands.VeldoryaCommand;
import com.kardasland.spigot.commands.VerifyCommand;
import com.kardasland.spigot.events.ChatEvent;
import com.kardasland.spigot.events.JoinLeaveHandler;
import com.kardasland.spigot.runnable.AutoSaveRunnable;
import com.kardasland.spigot.runnable.OnlineCounterRunnable;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.kardasland.veldoryadiscord.models.DailyReward;
import com.kardasland.veldoryadiscord.models.cache.DPlayerCache;
import com.kardasland.veldoryadiscord.models.cache.PermRoleCache;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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

    @Getter @Setter public List<DPlayer> dPlayerList;

    public boolean isFullyLoaded = false;

    public static JDA jda;


    /**
     * Hello to whoever edits the files.
     * I haven't coded for like idk.. 4-5 months? I don't have a proper mental for clean coding, Studying for YKS23 is hard lmao
     * My code quality should be mediocre and not that hard to read, at least that is what I hear
     * But if you still can't read it or too messy, you can contact me.
     * - KardasLand#9552
     */



    @Override
    public void onEnable() {
        try {
            instance = this;

            setupDependencyInjection();



            this.verifyMap = new HashMap<>();
            this.embedCommandsList = new ArrayList<>();
            this.dailyRewards = new ArrayList<>();

            ConfigManager.load("config.yml");
            ConfigManager.load("roles.yml");
            ConfigManager.load("commands.yml");
            ConfigManager.load("dailyrewards.yml");
            ConfigManager.load("messages.yml");

            loadEmbeds();
            loadRewards();

            Bukkit.getPluginManager().registerEvents(new JoinLeaveHandler(), this);
            Bukkit.getPluginManager().registerEvents(new ChatEvent(), this);

            dPlayerCache = new DPlayerCache();
            permRoleCache = new PermRoleCache();
            permRoleCache.loadRoles();


            this.isqlOperations = ConfigManager.get("config.yml").getString("sql.type").equalsIgnoreCase("mysql") ? new MySQL() : new SQLite();
            isqlOperations.setupHikari();
            isqlOperations.checkDatabase();

            if (!ConfigManager.get("config.yml").isSet("discord.token")){
                Utils.noPrefix("You need to fill discord token and channels in config.yml in order to function. Shutting down for now..");
                this.getPluginLoader().disablePlugin(this);
                return;
            }

            getCommand("dogrula").setExecutor(new VerifyCommand());
            getCommand("oneri").setExecutor(new SuggestCommand());
            getCommand("veldoryadiscord").setExecutor(new VeldoryaCommand());

            setupBot();

            int autosave = ConfigManager.get("config.yml").getInt("autosaveTime") * 20;
            new AutoSaveRunnable().runTaskTimerAsynchronously(this, autosave, autosave);
            new OnlineCounterRunnable().runTaskTimer(this, 20 * 10, 20 * 10);

        }catch (SQLException ex){
            System.out.println("SQL Exception! Printing the error and shutting down the plugin.");
            ex.printStackTrace();
            this.getPluginLoader().disablePlugin(this);
        }catch (InterruptedException | NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                IOException | RuntimeException ex){
            System.out.println("Interrupted/JDA Exception! Printing the error and shutting down the plugin.");
            ex.printStackTrace();
            this.getPluginLoader().disablePlugin(this);
        }
    }

    private void setupDependencyInjection() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (Version.getServerVersion(this.getServer()).isOlderThan(Version.v1_16_R3)){
            String libFolder = VeldoryaJDA.instance.getDataFolder() + File.separator + "libs";
            String jarPath = VeldoryaJDA.instance.getDataFolder() + File.separator + "libs" + File.separator + "jda.jar";
            File lib = new File(libFolder);
            File jar = new File(jarPath);
            if (!lib.exists()){
                lib.mkdirs();
            }
            if (!jar.exists()){
                jar.createNewFile();
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("jda.jar");
                assert inputStream != null;
                copyInputStreamToFile(inputStream, jar);
            }
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            try {
                Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, jar.toURI().toURL());
            } catch (NoSuchMethodException e) {
                Method method = classLoader.getClass()
                        .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                method.setAccessible(true);
                method.invoke(classLoader, jarPath);
            } catch (MalformedURLException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
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
        if (jda != null){
            jda.shutdownNow();
            mainJDAThread.interrupt();
        }
        isqlOperations.closeHikari();
    }
    public void reloadConfigurations() {
        ConfigManager.reload("config.yml");
        ConfigManager.reload("commands.yml");
        ConfigManager.reload("dailyrewards.yml");
        ConfigManager.reload("messages.yml");
        ConfigManager.reload("roles.yml");
    }
}
