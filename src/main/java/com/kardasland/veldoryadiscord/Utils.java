package com.kardasland.veldoryadiscord;

import com.kardasland.data.ConfigManager;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static Color getAWTColor(String color){
        Color color1;
        try {
            Field field = Color.class.getField(color);
            color1 = (Color)field.get(null);
        } catch (Exception e) {
            color1 = null; // Not defined
        }
        return color1;
    }

    public static String formatTime(long second){
        return DurationFormatUtils.formatDuration(second * 1000L, "H:mm:ss", true);
    }

    public static long compareTwoTimeStamps(java.sql.Timestamp currentTime, java.sql.Timestamp oldTime)
    {
        long milliseconds1 = oldTime.getTime();
        long milliseconds2 = currentTime.getTime();

        long diff = milliseconds2 - milliseconds1;
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffSeconds;
    }

    public static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    public static void deleteAfter(Message message, int delay) {
        message.delete().queueAfter(delay, TimeUnit.SECONDS);
    }

    public static void noPrefix(Player player, String message){
        player.sendMessage(color(message));
    }

    public static void noPrefix(String message){
        VeldoryaJDA.instance.getLogger().info(message);
    }

    public static boolean hasPerms(Player player, String permission){
        if (player.hasPermission(permission)) {
            return true;
        }else {
            noPrefix(player, ConfigManager.get("messages.yml").getString("server.noPermission"));
            return false;
        }
    }

}
