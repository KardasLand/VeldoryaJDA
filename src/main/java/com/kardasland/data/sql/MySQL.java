package com.kardasland.data.sql;

import com.kardasland.data.ConfigManager;
import com.kardasland.data.ISQLOperations;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import com.kardasland.veldoryadiscord.models.DPlayer;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;

public class MySQL implements ISQLOperations {

    private HikariDataSource hikari;
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
    public void checkDatabase() throws SQLException {
        try {
            Connection connection = hikari.getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            Utils.noPrefix("MYSQL Version: " + meta.getDatabaseProductVersion());
            PreparedStatement create = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `veldoryajda` (`id` int NOT NULL AUTO_INCREMENT,`playeruuid` varchar(512) NOT NULL,`discordid` varchar(512) NOT NULL,`groups` varchar(512) DEFAULT NULL,`timestamp` timestamp(3) NULL DEFAULT NULL,PRIMARY KEY (`id`),UNIQUE KEY `playeruuid_UNIQUE` (`playeruuid`),UNIQUE KEY `discordid_UNIQUE` (`discordid`)) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci");
            if (create.execute()){
                Utils.noPrefix("Created the MySQL Database.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setupHikari() {
        FileConfiguration cf = ConfigManager.get("config.yml");
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        assert cf != null;
        hikari.addDataSourceProperty("serverName", cf.getString("sql.host"));
        hikari.addDataSourceProperty("port", cf.getInt("sql.port"));
        hikari.addDataSourceProperty("databaseName", cf.getString("sql.database"));
        hikari.addDataSourceProperty("user", cf.getString("sql.username"));
        hikari.addDataSourceProperty("password", cf.getString("sql.password"));
        hikari.addDataSourceProperty("verifyServerCertificate", false);
        hikari.addDataSourceProperty("allowPublicKeyRetrieval", true);
        hikari.addDataSourceProperty("useSSL", false);
        hikari.setIdleTimeout(5000);
        hikari.setMaxLifetime(60 * 1000);
    }

    @Override
    public void closeHikari() {
        if (hikari != null)
            hikari.close();
    }
}
