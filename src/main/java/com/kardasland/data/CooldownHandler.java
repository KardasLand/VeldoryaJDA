package com.kardasland.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownHandler {

    private static final Map<String, CooldownHandler> cooldowns = new HashMap<String, CooldownHandler>();
    private long start;
    private final int timeInSeconds;
    private final UUID id;
    private final String cooldownName;

    public CooldownHandler(UUID id, String cooldownName, int timeInSeconds){
        this.id = id;
        this.cooldownName = cooldownName;
        this.timeInSeconds = timeInSeconds;
    }

    public static boolean isInCooldown(UUID id, String cooldownName){
        if(getTimeLeft(id, cooldownName)>=1){
            return true;
        } else {
            stop(id, cooldownName);
            return false;
        }
    }

    private static void stop(UUID id, String cooldownName){
        CooldownHandler.cooldowns.remove(id+cooldownName);
    }

    private static CooldownHandler getCooldown(UUID id, String cooldownName){
        return cooldowns.get(id.toString()+cooldownName);
    }

    public static int getTimeLeft(UUID id, String cooldownName){
        CooldownHandler cooldown = getCooldown(id, cooldownName);
        int f = -1;
        if(cooldown!=null){
            long now = System.currentTimeMillis();
            long cooldownTime = cooldown.start;
            int totalTime = cooldown.timeInSeconds;
            int r = (int) (now - cooldownTime) / 1000;
            f = (r - totalTime) * (-1);
        }
        return f;
    }

    public void start(){
        this.start = System.currentTimeMillis();
        cooldowns.put(this.id.toString()+this.cooldownName, this);
    }

}
