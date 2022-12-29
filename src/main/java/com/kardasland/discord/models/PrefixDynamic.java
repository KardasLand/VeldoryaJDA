package com.kardasland.discord.models;

import lombok.Getter;
import lombok.Setter;

public class PrefixDynamic{
    @Getter
    @Setter
    String command;
    @Getter
    @Setter
    CustomEmbed embed;
    @Getter
    @Setter
    String desc;
    public PrefixDynamic(String command){
        this.command = command;
    }
}
