package com.kardasland.discord.ticket;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.templates.TemplateChannel;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import javax.xml.soap.Text;
import java.io.*;
import java.time.Instant;
import java.util.*;

public class Ticket implements Serializable {
    @Getter @Setter
    String ownerID;
    @Getter @Setter
    String channelName;
    @Getter @Setter
    String channelID;

    @Getter @Setter
    String reason;

    private static final long serialVersionUID = 6529685098267757690L;



    public Ticket(String ownerID){
        this.ownerID = ownerID;
    }
    public Ticket(String ownerID, String reason){
        this.reason = reason;
        this.ownerID = ownerID;
    }


    public void createNewTicket() throws IOException {
        FileConfiguration ticketSystem = ConfigManager.get("ticketsystem.yml");
        FileConfiguration ticketData = ConfigManager.get("ticketdata.yml");

        Category category = VeldoryaJDA.jda.getCategoryById(ticketSystem.getString("ticketCategory"));
        //List<TextChannel> channels = category.getTextChannels();
        int ticketSize = ticketData.getInt("number") + 1;
        // "ticket-"+ ticketSize;


        this.channelName = ticketSystem.getString("ticketFormat")
                .replace("%number%", String.valueOf(ticketSize))
                .replace("%member%", VeldoryaJDA.instance.getGuild().getMemberById(ownerID).getUser().getName()).replaceAll("[^a-zA-Z0-9]", " ");
        this.channelName = channelName.replace(" ", "-");
        ticketData.set("number", ticketSize);
        ConfigManager.save("ticketdata.yml");
        ConfigManager.reload("ticketdata.yml");
        //this.channelName = !channels.isEmpty() ? (("ticket-"+ ticketSize + 1).toLowerCase()) : (("ticket-1").toLowerCase());
        category.createTextChannel(channelName)
                .addPermissionOverride(VeldoryaJDA.instance.getGuild().getMemberById(ownerID), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                .addPermissionOverride(VeldoryaJDA.instance.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))
                .complete();
        for (TextChannel textChannel : category.getTextChannels()){
            if (textChannel.getName().equalsIgnoreCase(this.channelName)){
                channelID = textChannel.getId();
                makeRest(textChannel);
            }
        }
        List<String> encodedList = ConfigManager.get("ticketdata.yml").getStringList("tickets");

        String encoded = toString(this);
        encodedList.add(encoded);

        /*List<Ticket> ticketList = new ArrayList<>();
        try {
            for (String encode : encodedList){
                Ticket ticket = (Ticket) fromString(encode);
                ticketList.add(ticket);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }*/


        /*List<Ticket> ticketList = (List<Ticket>) ConfigManager.get("ticketdata.yml").get("tickets");
        if (ticketList == null){
            ticketList = new ArrayList<>();
        }
        ticketList.add(this);*/
        ConfigManager.get("ticketdata.yml").set("tickets", encodedList);
        ConfigManager.save("ticketdata.yml");
        ConfigManager.reload("ticketdata.yml");
        /*new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    channelID = category.getGuild().getTextChannelsByName(channelName, true).get(0).getId();
                }catch (Exception ignored){}
            }
        }.runTaskLater(VeldoryaJDA.instance, 60L);*/
    }

    void makeRest(TextChannel textChannel){
        FileConfiguration ticketSystem = ConfigManager.get("ticketsystem.yml");
        FileConfiguration ticketData = ConfigManager.get("ticketdata.yml");
        ItemComponent closeComponent = new CustomButton("closeTicket", "velCloseTicket").buildButton();
        textChannel.sendMessageEmbeds(buildCustomEmbed().buildEmbed()).addActionRow(
               closeComponent).queue();
        TextChannelManager manager = textChannel.getManager();
        for (String roleid : ticketSystem.getStringList("supportTeamRoles")){
            //System.out.println(roleid);
            manager.putPermissionOverride(textChannel.getGuild().getRoleById(roleid), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null);
        }
        manager.complete();
        if (reason != null && !reason.isEmpty()){
            textChannel.sendMessageEmbeds(buildReasonEmbed().buildEmbed()).complete();
        }
    }

    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    CustomEmbed buildCustomEmbed(){
        FileConfiguration cf = ConfigManager.get("ticketsystem.yml");
        List<CustomEmbed.EmbedField> fields = new ArrayList<>();
        CustomEmbed.EmbedField ticketOpen = new CustomEmbed.EmbedField(
                cf.getString("messages.openTicketMessage.fields.ticketOpener.title"),
                cf.getString("messages.openTicketMessage.fields.ticketOpener.desc").replace("%mentionmember%", VeldoryaJDA.instance.getGuild().getMemberById(ownerID).getAsMention()), true
        );
        fields.add(ticketOpen);
        CustomEmbed.EmbedField ticketOpenTime = new CustomEmbed.EmbedField(
                cf.getString("messages.openTicketMessage.fields.ticketOpenTime.title"),
                cf.getString("messages.openTicketMessage.fields.ticketOpenTime.desc").replace("%timestamp%", "<t:"+ Instant.now().getEpochSecond() +":f>"), true
        );
        fields.add(ticketOpenTime);

        CustomEmbed customEmbed = new CustomEmbed(Utils.getAWTColor(cf.getString("messages.openTicketMessage.color")), fields);

        CustomEmbed.Footer footer = new CustomEmbed.Footer(
                cf.getString("messages.openTicketMessage.footer.text").replace("%member%",  VeldoryaJDA.instance.getGuild().getMemberById(ownerID).getUser().getName()),
                cf.getString("messages.openTicketMessage.footer.iconURL").replace("%avatar%", VeldoryaJDA.instance.getGuild().getMemberById(ownerID).getUser().getEffectiveAvatarUrl()));
        customEmbed.setFooter(footer);

        CustomEmbed.EmbedAuthor author = new CustomEmbed.EmbedAuthor(cf.getString("messages.openTicketMessage.author.name").replace("%member%",  VeldoryaJDA.instance.getGuild().getMemberById(ownerID).getUser().getName()),
                cf.getString("messages.openTicketMessage.author.iconURL").replace("%avatar%", VeldoryaJDA.instance.getGuild().getMemberById(ownerID).getUser().getEffectiveAvatarUrl()));
        customEmbed.setAuthor(author);

        customEmbed.setThumbnail(cf.getString("messages.openTicketMessage.icon").replace("%avatar%", VeldoryaJDA.instance.getGuild().getMemberById(ownerID).getUser().getEffectiveAvatarUrl()));
        return customEmbed;
    }
    CustomEmbed buildReasonEmbed(){
        FileConfiguration cf = ConfigManager.get("ticketsystem.yml");
        CustomEmbed customEmbed = new CustomEmbed(cf, "messages.reasonForTicket");
        customEmbed.setDesc(customEmbed.getDesc().replace("%reason%", reason));
        return customEmbed;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ownerID='" + ownerID + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelID='" + channelID + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
