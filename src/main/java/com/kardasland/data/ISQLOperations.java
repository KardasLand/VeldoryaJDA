package com.kardasland.data;

import com.kardasland.data.sql.callbacks.DPlayerCallback;
import com.kardasland.veldoryadiscord.models.DPlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface ISQLOperations {
    void insert(DPlayer dPlayer);
    @Deprecated
    ResultSet select(Player player);

    void getDPlayerAsync(Player player, DPlayerCallback dPlayerCallback);
    void getDPlayerAsync(UUID uuid, DPlayerCallback dPlayerCallback);
    ResultSet select(String discordid);

    void checkDatabase() throws SQLException;

    void deletePlayer(DPlayer dPlayer);

    void setupHikari();

    void closeHikari();
}
