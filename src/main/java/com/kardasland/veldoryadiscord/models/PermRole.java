package com.kardasland.veldoryadiscord.models;

import lombok.Getter;
import lombok.Setter;

public class PermRole {
    @Getter @Setter String permission;
    @Getter @Setter long roleid;

    public PermRole(String permission, long roleid){
        this.permission = permission;
        this.roleid = roleid;
    }
}
