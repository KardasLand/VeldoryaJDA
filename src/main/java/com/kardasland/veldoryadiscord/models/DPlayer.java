package com.kardasland.veldoryadiscord.models;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class DPlayer {
    @Getter @Setter Player player;
    @Getter @Setter
    OfflinePlayer offlinePlayer;
    @Getter @Setter String playerUUID;
    @Getter @Setter String discordID;
    @Getter @Setter String groups;
    @Getter @Setter Timestamp timestamp;
    @Getter @Setter Boolean verified = false;



    @Deprecated
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

    public DPlayer(){

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

        //System.out.println("update pass 1");

        // Async to prevent lag.
        if (!verified){
            //System.out.println("update fail 1");
            return;
        }
        //System.out.println("update pass 2");

        VeldoryaJDA.instance.getIsqlOperations().insert(this);

        //System.out.println("update pass 3");
        processGroups();

        new BukkitRunnable(){
            @Override
            public void run() {
                //System.out.println("update pass 4");

                //System.out.println("member id : " + discordID);

                Guild guild = VeldoryaJDA.jda.getGuildById(ConfigManager.get("config.yml").getString("discord.guild"));
                Member member = guild.getMemberById(discordID);
                //User member = VeldoryaJDA.jda.retrieveUserById(discordID).complete();

                List<PermRole> modifiedRoleList = transform();

                List<Role> transformList = new ArrayList<>();
                List<Role> permRoles = new ArrayList<>();
                List<Role> memberRoles = member.getRoles();

                for (PermRole permRole : modifiedRoleList){
                    if (permRole != null){
                        transformList.add(guild.getRoleById(permRole.getRoleid()));
                    }
                }
                for (PermRole permRole : VeldoryaJDA.instance.getPermRoleCache().getPermRoles()){
                    permRoles.add(guild.getRoleById(permRole.getRoleid()));
                }

                /*

                for (Role role : transformList){
                    System.out.println("transformList: " + role.getName());
                }
                for (Role role : permRoles){
                    System.out.println("permRoles: " + role.getName());
                }
                for (Role role : memberRoles){
                    System.out.println("memberRoles: " + role.getName());
                }*/

                Set<Role> transIntersectPerm = transformList.stream().distinct().filter(permRoles::contains).collect(Collectors.toSet());
                Set<Role> memberIntersectPerm = permRoles.stream().distinct().filter(memberRoles::contains).collect(Collectors.toSet());
                Set<Role> memberIntersectTrans = transformList.stream().distinct().filter(memberRoles::contains).collect(Collectors.toSet());

                Set<Role> dontTouchList = transIntersectPerm.stream().distinct().filter(memberRoles::contains).collect(Collectors.toSet());

                /*
                for (Role role : transIntersectPerm){
                    System.out.println("transIntersectPerm: " + role.getName());
                }
                for (Role role : memberIntersectPerm){
                    System.out.println("memberIntersectPerm: " + role.getName());
                }
                for (Role role : memberIntersectTrans){
                    System.out.println("memberIntersectTrans: " + role.getName());
                }
                for (Role role : dontTouchList){
                    System.out.println("dontTouchList: " + role.getName());
                }*/

                Set<Role> deleteList = new HashSet<>();
                deleteList.addAll(memberIntersectPerm);
                deleteList.removeAll(transformList);

                /*for (Role role : deleteList){
                    System.out.println("deleteList: " + role.getName());
                }*/

                Set<Role> addList = new HashSet<>();
                for (Role role : transIntersectPerm){
                    //System.out.println("addLoop: " + role.getName());
                    if (!memberRoles.contains(role)){
                        //System.out.println(" ");
                        //System.out.println("addLoop added: " + role.getName());
                        addList.add(role);
                    }
                }
                /*for (Role role : addList){
                    System.out.println("addList: " + role.getName());
                }*/


                for (Role role : deleteList){
                    if (!dontTouchList.contains(role)){
                        guild.removeRoleFromMember(Objects.requireNonNull(guild.getMemberById(discordID)), role).queue();
                    }
                }
                for (Role role : addList){
                    if (!dontTouchList.contains(role)){
                        guild.addRoleToMember(Objects.requireNonNull(guild.getMemberById(discordID)), role).queue();
                    }
                }
                /*
                if (!member.getRoles().isEmpty()){
                    for (Role memberRole : member.getRoles()){
                        if (transformList.contains(memberRole)){
                            guild.addRoleToMember(Objects.requireNonNull(guild.getMemberById(discordID)), group).queue();
                        }
                    }
                }

                // REMOVING ALL ROLES
                for (PermRole allroles : VeldoryaJDA.instance.getPermRoleCache().getPermRoles()){
                    Role group = guild.getRoleById(allroles.getRoleid());
                    if (group != null){
                        //if (guild.getMemberById(discordID).getRoles().contains(group)) {
                        guild.removeRoleFromMember(Objects.requireNonNull(guild.getMemberById(discordID)), group).queue();
                        //}
                    }
                }
                // ADDED LATEST ONES
                List<PermRole> roleList = transform();
                for (PermRole role : roleList){
                    Role group = guild.getRoleById(role.getRoleid());
                    if (group != null){
                        guild.addRoleToMember(Objects.requireNonNull(guild.getMemberById(discordID)), group).queue();
                    }
                }*/
            }
        }.runTaskAsynchronously(VeldoryaJDA.instance);
    }

    public void processGroups(){
        if (playerUUID == null){
            groups = "";
            return;
        }
        try {
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
        }catch (StringIndexOutOfBoundsException exception){
            groups = "";
        }catch (NullPointerException exception){
            VeldoryaJDA.instance.getLogger().info("A specific permission does not exists or you don't have permissions.");
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
