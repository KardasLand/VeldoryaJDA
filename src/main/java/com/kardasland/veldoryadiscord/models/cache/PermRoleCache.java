package com.kardasland.veldoryadiscord.models.cache;

import com.kardasland.data.ConfigManager;
import com.kardasland.veldoryadiscord.models.PermRole;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PermRoleCache {
    @Getter
    @Setter
    public List<PermRole> permRoles;

    public PermRoleCache(){
        this.permRoles = new ArrayList<>();
    }

    public void add(String permission, long roleid){
        permRoles.add(new PermRole(permission, roleid));
    }
    public boolean remove(String permission){
        PermRole target = findRole(permission);
        return permRoles.remove(target);
    }
    public boolean remove(long roleid){
        PermRole target = findRole(roleid);
        return permRoles.remove(target);
    }
    public PermRole findRole(String permission){
        for (PermRole role : permRoles){
            if (role.getPermission().equals(permission)) {
                return role;
            }
        }
        return null;
    }
    public PermRole findRole(long roleid){
        for (PermRole role : permRoles){
            if (role.getRoleid() == roleid) {
                return role;
            }
        }
        return null;
    }

    public void loadRoles(){
        FileConfiguration cf = ConfigManager.get("roles.yml");
        System.out.println("Loading discord roles...");
        assert cf != null;
        int a = 0;
        for (String keys : cf.getConfigurationSection("roles.").getKeys(false)){
            add(cf.getString("roles."+keys+".permission"), cf.getLong("roles."+keys+".id"));
            a++;
        }
        System.out.println("Loaded " + a + " discord roles.");
    }
}
