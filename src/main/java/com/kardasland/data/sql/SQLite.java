package com.kardasland.data.sql;

import com.kardasland.data.ISQLOperations;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLite implements ISQLOperations {
    Connection connection;
    @Override
    public void insert(DPlayer dPlayer) {
        new BukkitRunnable(){
            @Override
            public void run() {
                try{
                    PreparedStatement select = connection.prepareStatement("INSERT INTO veldoryajda (playeruuid, discordid, `groups`, timestamp) VALUES(?,?,?,?) ON CONFLICT(`discordid`) DO UPDATE SET discordid=?");
                    select.setString(1, dPlayer.getPlayerUUID());
                    select.setString(2, dPlayer.getDiscordID());
                    select.setString(3, dPlayer.getGroups());
                    select.setTimestamp(4, dPlayer.getTimestamp());
                    select.setString(5, dPlayer.getDiscordID());
                    select.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously(VeldoryaJDA.instance);
    }

    @Override
    public ResultSet select(Player player) {
        try{
            PreparedStatement select = this.connection.prepareStatement("SELECT * FROM veldoryajda WHERE playeruuid=?");
            select.setString(1, String.valueOf(player.getUniqueId()));
            return select.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResultSet select(String discordid) {
        try{
            PreparedStatement select = this.connection.prepareStatement("SELECT * FROM veldoryajda WHERE discordid=?");
            select.setString(1, discordid);
            return select.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkDatabase() {
        try{
            Connection connection = this.connection;
            DatabaseMetaData meta = connection.getMetaData();
            Utils.noPrefix("SQL Version: " + meta.getDatabaseProductVersion());
            PreparedStatement create = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `veldoryajda` (`id` INTEGER PRIMARY KEY AUTOINCREMENT,`playeruuid` TEXT NOT NULL UNIQUE ,`discordid` TEXT NOT NULL UNIQUE ,`groups` TEXT DEFAULT NULL,`timestamp` timestamp(3) NULL DEFAULT NULL)");
            if (create.execute()){
                Utils.noPrefix("Created the SQLite Database.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setupHikari() {
        File data = new File(VeldoryaJDA.instance.getDataFolder(), "database.db");
        try {
            if (!data.exists()){
                data.createNewFile();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + data);
        } catch (IOException | SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void closeHikari() {
        if (connection != null)
            connection.close();
    }
}
