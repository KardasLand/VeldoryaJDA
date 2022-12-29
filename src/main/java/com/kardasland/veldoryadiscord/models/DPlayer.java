package com.kardasland.veldoryadiscord.models;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DPlayer {
    @Getter @Setter Player player;
    @Getter @Setter String playerUUID;
    @Getter @Setter String discordID;
    @Getter @Setter String groups;
    @Getter @Setter Timestamp timestamp;
    @Getter @Setter Boolean verified = false;

    public DPlayer(Player player){
        ResultSet resultSet = VeldoryaJDA.instance.isqlOperations.select(player);
        try {
            this.player = player;
            this.playerUUID = player.getUniqueId().toString();
            this.verified = false;
            if (resultSet.next()){
                this.discordID = resultSet.getString("discordid");
                this.groups = resultSet.getString("groups");
                this.timestamp = resultSet.getTimestamp("timestamp");
                this.verified = true;
            }
        }catch (SQLException exception){
            exception.printStackTrace();
        }
    }

    @Deprecated
    public DPlayer(String discordid){
        ResultSet resultSet = VeldoryaJDA.instance.isqlOperations.select(discordid);
        try{
            this.discordID = discordid;
            this.player = Bukkit.getPlayer(playerUUID);
            if (resultSet.next()){
                this.playerUUID = resultSet.getString("playeruuid");
                this.groups = resultSet.getString("groups");
                this.timestamp = resultSet.getTimestamp("timestamp");
                this.verified = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(){

        // Async to prevent lag.
        VeldoryaJDA.instance.getIsqlOperations().insert(this);

        Guild guild = VeldoryaJDA.jda.getGuildById(ConfigManager.get("config.yml").getString("discord.guild"));

        processGroups();
        // REMOVING ALL ROLES
        for (PermRole allroles : VeldoryaJDA.instance.getPermRoleCache().getPermRoles()){
            Role group = guild.getRoleById(allroles.getRoleid());
            if (group != null){
                if (guild.getMemberById(discordID).getRoles().contains(group)) {
                    guild.removeRoleFromMember(Objects.requireNonNull(guild.getMemberById(discordID)), group).queue();
                }
            }
        }
        // ADDED LATEST ONES
        List<PermRole> roleList = transform();
        for (PermRole role : roleList){
            Role group = guild.getRoleById(role.getRoleid());
            if (group != null){
                guild.addRoleToMember(Objects.requireNonNull(guild.getMemberById(discordID)), group).queue();
            }
        }
    }

    public void processGroups(){
        if (playerUUID == null){
            groups = "";
            return;
        }
        if (Bukkit.getPlayer(UUID.fromString(playerUUID)) != null && Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).isOnline()) {
            player = Bukkit.getPlayer(UUID.fromString(playerUUID)).getPlayer();
            StringBuilder sm = new StringBuilder();
            for (PermRole permRole : VeldoryaJDA.instance.getPermRoleCache().getPermRoles()){
                if (player.hasPermission(permRole.permission)){
                    sm.append(permRole.permission).append(",");
                }
            }
            groups = sm.substring(0, sm.length() - 1);
        }else{
            groups = "";
        }
    }

    public List<PermRole> transform(){
        List<PermRole> temp = new ArrayList<>();
        for (String perm : groups.split(",")){
            temp.add(VeldoryaJDA.instance.getPermRoleCache().findRole(perm));
        }
        return temp;
    }

    @Override
    public String toString() {
        return "DPlayer{" +
                "player=" + player +
                ", playerUUID='" + playerUUID + '\'' +
                ", discordID='" + discordID + '\'' +
                ", groups='" + groups + '\'' +
                ", timestamp=" + timestamp +
                ", verified=" + verified +
                '}';
    }
}
