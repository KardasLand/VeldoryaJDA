package com.kardasland.data;

import com.kardasland.veldoryadiscord.models.DPlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ISQLOperations {
    void insert(DPlayer dPlayer);
    ResultSet select(Player player);
    ResultSet select(String discordid);

    void checkDatabase() throws SQLException;

    void setupHikari();

    void closeHikari();
}
