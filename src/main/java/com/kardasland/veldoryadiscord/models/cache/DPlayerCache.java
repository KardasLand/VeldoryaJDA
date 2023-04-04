package com.kardasland.veldoryadiscord.models.cache;

import com.kardasland.veldoryadiscord.models.DPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DPlayerCache {
    @Getter @Setter public List<DPlayer> dPlayerList;

    public DPlayerCache(){
        this.dPlayerList = new ArrayList<>();
    }

    /*
    public void add(Player player){
        dPlayerList.add(new DPlayer(player));
    }*/

    public void add(DPlayer dPlayer){
        dPlayerList.add(dPlayer);
    }
    public void remove(Player player){
        DPlayer target = findPlayer(player);
        dPlayerList.remove(target);
    }
    public void remove(DPlayer dPlayer){
        dPlayerList.remove(dPlayer);
    }
    public DPlayer findPlayer(Player player){
        for (DPlayer search : dPlayerList){
            if (player.getUniqueId().toString().equals(search.getPlayerUUID())){
                return search;
            }
        }
        return null;
    }

    public DPlayer findPlayerbyUUID(String UUID){
        for (DPlayer search : dPlayerList){
            if (UUID.equals(search.getPlayerUUID())){
                return search;
            }
        }
        return null;
    }
    public DPlayer findPlayer(String discordid){
        for (DPlayer search : dPlayerList){
            if (discordid.equals(search.getDiscordID())){
                return search;
            }
        }
        return null;
    }

    public void update(){
        for (DPlayer player : dPlayerList){
            if (player.getDiscordID() != null && player.getVerified()){
                player.update();
            }
        }
    }
}
