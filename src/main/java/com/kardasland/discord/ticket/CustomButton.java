package com.kardasland.discord.ticket;

import com.kardasland.data.ConfigManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;

public class CustomButton {
    @Getter @Setter String title;
    @Getter @Setter String friendlyStyle;
    @Getter @Setter ButtonStyle buttonStyle;
    @Getter @Setter String emojiID;

    @Getter @Setter String id;



    public CustomButton(String path, String id){
        FileConfiguration cf = ConfigManager.get("ticketsystem.yml");
        this.id = id;
        this.title = cf.getString("buttons."+path+".title");
        this.friendlyStyle = cf.getString("buttons."+path+".color");
        this.emojiID = cf.isSet("buttons."+path+".emoji") ? cf.getString("buttons."+path+".emoji") : null;
        switch (friendlyStyle){
            case "GRAY":{
                this.buttonStyle = ButtonStyle.SECONDARY;
                break;
            }
            case "RED":{
                this.buttonStyle = ButtonStyle.DANGER;
                break;
            }
            case "GREEN":{
                this.buttonStyle = ButtonStyle.SUCCESS;
                break;
            }
            case "BLUE":
            default:{
                this.buttonStyle = ButtonStyle.PRIMARY;
                break;
            }
        }
    }

    public ItemComponent buildButton(){
        ItemComponent component;
        if (this.getEmojiID() != null && !this.getEmojiID().isEmpty()) {
            component = Button.of(this.getButtonStyle(), id, this.getTitle()).withEmoji(Emoji.fromFormatted(this.getEmojiID())).withId(id);
        }else {
            component = Button.of(this.getButtonStyle(), id, this.getTitle()).withId(id);
        }
        return component;
    }
}
