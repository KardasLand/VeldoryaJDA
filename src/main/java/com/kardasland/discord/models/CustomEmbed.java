package com.kardasland.discord.models;

import com.kardasland.veldoryadiscord.Utils;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomEmbed {

    EmbedBuilder embed;

    @Getter @Setter String title;
    @Getter @Setter String desc;
    @Getter @Setter Color color;
    @Getter @Setter Footer footer;
    @Getter @Setter EmbedAuthor author;
    @Getter @Setter String thumbnail;
    @Getter @Setter List<EmbedField> embedFieldList;

    @Getter @Setter String image;


    public CustomEmbed(String title, String desc, Color color, EmbedField... embedFields){
        this.embed = new EmbedBuilder();
        this.title = title;
        this.desc = desc;
        this.color = color;
        this.embedFieldList = Arrays.stream(embedFields).collect(Collectors.toList());
    }
    public CustomEmbed(String title, String desc, Color color, List<EmbedField> embedFields){
        this.embed = new EmbedBuilder();
        this.title = title;
        this.desc = desc;
        this.color = color;
        this.embedFieldList = embedFields;
    }
    public CustomEmbed(Color color, List<EmbedField> embedFields){
        this.embed = new EmbedBuilder();
        this.color = color;
        this.embedFieldList = embedFields;
    }
    public CustomEmbed (FileConfiguration cf, String path){
        this.embed = new EmbedBuilder();
        this.title = cf.getString(path+".title");
        this.desc = cf.getString(path+".description");
        this.color = Utils.getAWTColor(cf.getString(path+".color"));
        this.thumbnail = cf.isSet(path+ ".thumbnail") ? cf.getString(path+ ".thumbnail") : null;
        this.image = cf.isSet(path+ ".image") ? cf.getString(path+ ".image") : null;
        this.footer = cf.isSet(path+ ".footer") ? new CustomEmbed.Footer(cf.getString(path+ ".footer"), (cf.isSet(path+".icon") ? cf.getString(path+".icon") : null)): null;
        //System.out.println(cf.getString(path+".icon"));
    }
    public CustomEmbed(String title, String desc, Color color){
        this.embed = new EmbedBuilder();
        this.title = title;
        this.desc = desc;
        this.color = color;
        this.embedFieldList = new ArrayList<>();
    }

    public MessageEmbed buildEmbed(){
        if (title != null && !title.isEmpty()){
            embed.setTitle(title);
        }
        if (desc != null && !desc.isEmpty()){
            embed.setDescription(desc);
        }
        embed.setColor(color);
        if (footer != null){
            if (footer.getIconURL() != null && !footer.getIconURL().isEmpty()){
                embed.setFooter(footer.getText(), footer.getIconURL());
            }else {
                embed.setFooter(footer.getText());
            }
        }
        if (author != null){
            embed.setAuthor(author.name, author.url, author.url);
        }
        if (image != null && !image.isEmpty()){
            embed.setImage(image);
        }
        if (embedFieldList != null && !embedFieldList.isEmpty()){
            // IDK WHY THAT HAPPENS, IDK HOW IT HAPPENS. ALL I KNOW IS THIS JUST WORKS.
            // IT DUPLICATES FIELDS. This is a really hacky fix for it.
            for (EmbedField field : embedFieldList){
                boolean found = false;
                for (MessageEmbed.Field field1 : embed.getFields()){
                    if (Objects.equals(field1.getName(), field.getName())){
                        found = true;
                    }
                }
                if (!found){
                    embed.addField(field.getName(), field.getValue(), field.getInline());
                }
            }
        }
        if (thumbnail != null && !thumbnail.isEmpty()){
            embed.setThumbnail(thumbnail);
        }
        return embed.build();
    }

    public static class EmbedAuthor{
        @Getter @Setter String name;
        @Getter @Setter String url;

        public EmbedAuthor(String name, String url){
            this.name = name;
            this.url = url;
        }

        @Override
        public String toString() {
            return "EmbedAuthor{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public static class EmbedField{
        @Getter @Setter String name;
        @Getter @Setter String value;
        @Getter @Setter Boolean inline;
        public EmbedField(String name, String value, Boolean inline){
            this.inline = inline;
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "EmbedField{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", inline=" + inline +
                    '}';
        }
    }
    public static class Footer{
        @Getter @Setter String text;
        @Getter @Setter String iconURL;
        public Footer(String text, String iconURL){
            this.text = text;
            this.iconURL = iconURL;
        }

        @Override
        public String toString() {
            return "Footer{" +
                    "text='" + text + '\'' +
                    ", iconURL='" + iconURL + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CustomEmbed{" +
                "embed=" + embed +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", color=" + color +
                ", footer=" + footer +
                ", embedFieldList=" + embedFieldList +
                '}';
    }
}
