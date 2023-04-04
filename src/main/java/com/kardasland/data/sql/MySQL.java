package com.kardasland.data.sql;

import com.kardasland.data.ConfigManager;
import com.kardasland.data.ISQLOperations;
import com.kardasland.data.sql.callbacks.DPlayerCallback;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.UUID;

public class MySQL implements ISQLOperations {

    private HikariDataSource hikari;
    String host;
    Integer port;
    String database;
    String username;
    String password;
    @Override
    public void insert(DPlayer dPlayer) {
        new BukkitRunnable(){
            @Override
            public void run() {
                try{
                    Connection connection = hikari.getConnection();
                    PreparedStatement select = connection.prepareStatement("INSERT INTO veldoryajda (playeruuid, discordid, `groups`, timestamp) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE discordid=?");
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
            Connection connection = hikari.getConnection();
            PreparedStatement select = connection.prepareStatement("SELECT * FROM veldoryajda WHERE playeruuid=?");
            select.setString(1, String.valueOf(player.getUniqueId()));
            return select.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getDPlayerAsync(Player player, DPlayerCallback dPlayerCallback) {
        // Run outside of the tick loop
        Bukkit.getScheduler().runTaskAsynchronously(VeldoryaJDA.instance, () -> {
            try{
                Connection connection = hikari.getConnection();
                PreparedStatement select = connection.prepareStatement("SELECT * FROM veldoryajda WHERE playeruuid=?");
                select.setString(1, String.valueOf(player.getUniqueId()));
                ResultSet resultSet = select.executeQuery();
                DPlayer dPlayer = new DPlayer();
                dPlayer.setPlayer(player);
                dPlayer.setPlayerUUID(player.getUniqueId().toString());
                dPlayer.setVerified(false);
                if (resultSet.next()){
                    dPlayer.setDiscordID(resultSet.getString("discordid"));
                    dPlayer.setGroups(resultSet.getString("groups"));
                    dPlayer.setTimestamp(resultSet.getTimestamp("timestamp"));
                    dPlayer.setVerified(true);
                }
                Bukkit.getScheduler().runTask(VeldoryaJDA.instance, () -> {
                    // call the callback with the result
                    dPlayerCallback.onQueryDone(dPlayer);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void getDPlayerAsync(UUID uuid, DPlayerCallback dPlayerCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(VeldoryaJDA.instance, () -> {
            try{
                Connection connection = hikari.getConnection();
                PreparedStatement select = connection.prepareStatement("SELECT * FROM veldoryajda WHERE playeruuid=?");
                select.setString(1, String.valueOf(uuid));
                ResultSet resultSet = select.executeQuery();
                DPlayer dPlayer = new DPlayer();
                dPlayer.setOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
                dPlayer.setPlayerUUID(uuid.toString());
                dPlayer.setVerified(false);
                if (resultSet.next()){
                    dPlayer.setDiscordID(resultSet.getString("discordid"));
                    dPlayer.setGroups(resultSet.getString("groups"));
                    dPlayer.setTimestamp(resultSet.getTimestamp("timestamp"));
                    dPlayer.setVerified(true);
                }

                Bukkit.getScheduler().runTask(VeldoryaJDA.instance, () -> {
                    // call the callback with the result
                    dPlayerCallback.onQueryDone(dPlayer);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Deprecated
    @Override
    public ResultSet select(String discordid) {
        try{
            Connection connection = hikari.getConnection();
            PreparedStatement select = connection.prepareStatement("SELECT * FROM veldoryajda WHERE discordid=?");
            select.setString(1, discordid);
            return select.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkDatabase() {
        try {
            Connection connection = hikari.getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            Utils.noPrefix("MYSQL Version: " + meta.getDatabaseProductVersion());
            PreparedStatement create = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `veldoryajda` (`id` int NOT NULL AUTO_INCREMENT,`playeruuid` varchar(512) NOT NULL,`discordid` varchar(512) NOT NULL,`groups` varchar(512) DEFAULT NULL,`timestamp` timestamp(3) NULL DEFAULT NULL,PRIMARY KEY (`id`),UNIQUE KEY `playeruuid_UNIQUE` (`playeruuid`),UNIQUE KEY `discordid_UNIQUE` (`discordid`)) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4");
            if (create.execute()){
                Utils.noPrefix("Created the MySQL Database.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deletePlayer(DPlayer dPlayer) {

        new BukkitRunnable(){
            @Override
            public void run() {
                try{
                    Connection connection = hikari.getConnection();
                    PreparedStatement select = connection.prepareStatement("DELETE FROM veldoryajda WHERE discordid=?");
                    select.setString(1, dPlayer.getDiscordID());
                    select.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously(VeldoryaJDA.instance);
    }

    @Override
    public void setupHikari() {
        FileConfiguration cf = ConfigManager.get("config.yml");
        assert cf != null;
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        //hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.host = cf.getString("sql.host");
        this.port = cf.getInt("sql.port");
        this.database = cf.getString("sql.database");
        this.username = cf.getString("sql.username");
        this.password = cf.getString("sql.password");

        //hikari.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+database);
        //hikari.setUsername(username);
        //hikari.setPassword(password);

        hikari.setMaximumPoolSize(750);

        hikari.addDataSourceProperty("serverName", host);
        hikari.addDataSourceProperty("port", port);
        hikari.addDataSourceProperty("databaseName", database);
        hikari.addDataSourceProperty("user", username);
        hikari.addDataSourceProperty("password", password);
        hikari.addDataSourceProperty("verifyServerCertificate", false);
        hikari.addDataSourceProperty("allowPublicKeyRetrieval", true);
        hikari.addDataSourceProperty("useSSL", false);
        //hikari.setLeakDetectionThreshold(90000);
        hikari.setIdleTimeout(60000);
        hikari.setConnectionTimeout(600000);
        hikari.setMaxLifetime(1800000);
        hikari.setMinimumIdle(20);
        hikari.setValidationTimeout(3000);

        /*
        assert cf != null;
        hikari = new HikariDataSource();
        hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.host = cf.getString("sql.host");
        this.port = cf.getInt("sql.port");
        this.database = cf.getString("sql.database");
        this.username = cf.getString("sql.username");
        this.password = cf.getString("sql.password");
        hikari.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+database+"?verifyServerCertificate=false&allowPublicKeyRetrieval=true&useSSL=false");
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setIdleTimeout(5000);
        hikari.setMaxLifetime(60 * 1000);
         */
    }

    @Override
    public void closeHikari() {
        if (hikari != null)
            hikari.close();
    }
}
