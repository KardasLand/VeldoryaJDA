package com.kardasland.discord.ticket;

import com.kardasland.data.ConfigManager;
import com.kardasland.discord.models.CustomEmbed;
import com.kardasland.veldoryadiscord.Utils;
import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.kardasland.discord.ticket.Ticket.fromString;

public class ModalListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        //System.out.println(event.getComponentId());
        FileConfiguration ticketSystem = ConfigManager.get("ticketsystem.yml");
        switch (event.getComponentId()){
            case "velCloseTicket":{
                event.deferEdit().queue();
                String ownerID = Utils.findOwnerID(event.getChannel().getName());
                event.getChannel().asTextChannel().getManager().putPermissionOverride(VeldoryaJDA.instance.getGuild().getMemberById(ownerID), null, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)).queue();
                CustomEmbed closedEmbed = new CustomEmbed(ticketSystem, "messages.ticketClosed");
                closedEmbed.setDesc(closedEmbed.getDesc().replace("%mentionmember%", event.getMember().getAsMention()));
                event.getChannel().sendMessageEmbeds(closedEmbed.buildEmbed()).queue();
                CustomEmbed ticketControls = new CustomEmbed(ticketSystem, "messages.ticketControls");

                ItemComponent reopenComponent = new CustomButton("reopenTicket", "velReopenTicket").buildButton();
                ItemComponent deleteComponent = new CustomButton("deleteTicket", "velDeleteTicket").buildButton();

                event.getChannel().sendMessageEmbeds(ticketControls.buildEmbed()).addActionRow(
                       reopenComponent, deleteComponent
                ).queue();

                break;
            }case "velDeleteTicket":{
                event.deferEdit().queue();
                List<String> encodedList = ConfigManager.get("ticketdata.yml").getStringList("tickets");
                List<String> refinedList = new ArrayList<>();
                String ownerID = null;
                try {
                    for (String encode : encodedList){
                        Ticket ticket = (Ticket) fromString(encode);
                        //System.out.println(ticket.toString());
                        //System.out.println(event.getChannel().getName());
                        if (!ticket.channelName.equalsIgnoreCase(event.getChannel().getName())) {
                            refinedList.add(encode);
                            //System.out.println("a");
                        }else {
                            ownerID = ticket.getOwnerID();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                //System.out.println(refinedList.size());
                ConfigManager.get("ticketdata.yml").set("tickets", refinedList);
                ConfigManager.save("ticketdata.yml");
                ConfigManager.reload("ticketdata.yml");

                MessageHistory history = MessageHistory.getHistoryFromBeginning(event.getChannel()).complete();
                List<Message> mess = history.getRetrievedHistory();
                ArrayList<Message> messageArrayList = new ArrayList<>(mess);
                Collections.reverse(messageArrayList);

                List<String> transcript = new ArrayList<>();
                for(Message message : messageArrayList){
                    String friendlyTime = message.getTimeCreated().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"));
                    String content = message.getContentRaw().isEmpty() ? "This is probably an embed or an image, cannot view." : message.getContentRaw();
                    transcript.add("[" + friendlyTime + "] " + message.getAuthor().getName()+ ": " + content);
                    //transcript.add(message.getContentDisplay());
                }

                try {
                    //Path tempFile = Files.createTempFile(event.getMessageChannel().getName(), ".txt");
                    //Files.move(tempFile, tempFile.resolveSibling(event.getMessageChannel().getName() + ".txt"));
                    File tempFile = createTempFile(event.getMessageChannel().getName(),".txt");
                    Files.write(tempFile.toPath(), transcript, StandardOpenOption.APPEND);
                    TextChannel channel = event.getGuild().getTextChannelById(ConfigManager.get("ticketsystem.yml").getString("transcriptChannel"));
                    channel.sendMessageEmbeds(buildTranscriptEmbed(event.getChannel().asTextChannel(), event.getMember().getId(), ownerID).buildEmbed()).addFiles(FileUpload.fromData(tempFile)).queue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                event.getChannel().delete().queue();
                break;
            }case "velReopenTicket":{
                String ownerID = Utils.findOwnerID(event.getChannel().getName());
                try {
                    event.getChannel().asTextChannel().getManager().putPermissionOverride(VeldoryaJDA.instance.getGuild().getMemberById(ownerID), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null).queue();
                    event.getMessage().delete().queue();
                    CustomEmbed customEmbed = new CustomEmbed(ticketSystem, "messages.ticketReopened");
                    customEmbed.setDesc(customEmbed.getDesc().replace("%mentionmember%", event.getMember().getAsMention()));
                    event.replyEmbeds(customEmbed.buildEmbed()).queue();
                }catch (Exception exception){
                    event.reply("Ticket owner left the server or some exception happened. Please delete the ticket for preventing further errors.").queue();
                }
                break;
            } case "velHelpTicket":{
                CustomEmbed customEmbed = new CustomEmbed(ticketSystem, "messages.ticketHelpMessage");
                event.replyEmbeds(customEmbed.buildEmbed()).setEphemeral(true).queue();
                break;
            }
        }
    }

    public static File createTempFile(String prefix, String suffix) {
        File parent = new File(System.getProperty("java.io.tmpdir"));

        File temp = new File(parent, prefix + suffix);

        if (temp.exists()) {
            temp.delete();
        }

        try {
            temp.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return temp;
    }

    CustomEmbed buildTranscriptEmbed(TextChannel channel, String closerID, String ownerID){
        FileConfiguration cf = ConfigManager.get("ticketsystem.yml");
        CustomEmbed customEmbed = new CustomEmbed(cf, "messages.transcriptMessage");


        long creationTime = channel.getTimeCreated().toEpochSecond();
        long deletionTime = Instant.now().getEpochSecond();

        //System.out.println("IDS:");
        //System.out.println(ownerID);
        //System.out.println(closerID);

        //"<t:"+ Instant.now().getEpochSecond() +":f>"

        List<CustomEmbed.EmbedField> embedFieldList = new ArrayList<>();
        for (String key : cf.getConfigurationSection("messages.transcriptMessage.fields.").getKeys(false)){
            String title = cf.getString("messages.transcriptMessage.fields."+key+".title");
            String desc = cf.getString("messages.transcriptMessage.fields."+key+".desc")
                    .replace("%mentionopener%", channel.getGuild().getMemberById(ownerID).getAsMention())
                    .replace("%mentioncloser%", channel.getGuild().getMemberById(closerID).getAsMention())
                    .replace("%opentimestamp%", "<t:"+ creationTime +":f>")
                    .replace("%closetimestamp%", "<t:"+ deletionTime +":f>");
            Boolean inline = cf.getBoolean("messages.transcriptMessage.fields."+key+".inline");
            CustomEmbed.EmbedField field = new CustomEmbed.EmbedField(title, desc, inline);
            embedFieldList.add(field);
        }
        customEmbed.setThumbnail(channel.getGuild().getMemberById(ownerID).getUser().getEffectiveAvatarUrl());
        customEmbed.setEmbedFieldList(embedFieldList);
        return customEmbed;
    }

    public void reOpenTicket(Ticket toBeCreatedTicket, ModalInteractionEvent event) throws IOException {
        List<String> encodedList = ConfigManager.get("ticketdata.yml").getStringList("tickets");
        List<String> refinedList = new ArrayList<>();
        try {
            for (String encode : encodedList){
                Ticket ticket = (Ticket) fromString(encode);
                if (!ticket.ownerID.equalsIgnoreCase(toBeCreatedTicket.getOwnerID())) {
                    refinedList.add(encode);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(refinedList.size());
        ConfigManager.get("ticketdata.yml").set("tickets", refinedList);
        ConfigManager.save("ticketdata.yml");
        ConfigManager.reload("ticketdata.yml");
        toBeCreatedTicket.createNewTicket();
        event.getHook().editOriginal(ConfigManager.get("ticketsystem.yml").getString("messages.ticketOpened").replaceAll("%mentionticket%", event.getGuild().getTextChannelsByName(toBeCreatedTicket.channelName, true).get(0).getAsMention())).queue();
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        switch (event.getModalId()){
            case "velTalep":{
                try {
                    event.deferReply(true).queue();
                    String body = event.getValue("velTicketReason").getAsString();
                    Ticket ticket = new Ticket(Objects.requireNonNull(event.getMember()).getId(), body);
                    List<String> encodedList = ConfigManager.get("ticketdata.yml").getStringList("tickets");
                    for (String encode : encodedList){
                        Ticket targetTicket = (Ticket) fromString(encode);
                        if (targetTicket.getOwnerID().equalsIgnoreCase(event.getMember().getId())) {
                            if (event.getGuild().getTextChannelById(targetTicket.getChannelID()) == null){
                                reOpenTicket(ticket, event);
                                return;
                            }else {
                                event.getHook().editOriginal(ConfigManager.get("ticketsystem.yml").getString("messages.alreadyOpenedOne").replaceAll("%mentionticket%", event.getGuild().getTextChannelById(targetTicket.getChannelID()).getAsMention())).queue();
                                return;
                            }
                        }
                    }
                    ticket.createNewTicket();
                    event.getHook().editOriginal(ConfigManager.get("ticketsystem.yml").getString("messages.ticketOpened").replaceAll("%mentionticket%", event.getGuild().getTextChannelsByName(ticket.channelName, true).get(0).getAsMention())).queue();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            default:{

            }
        }
    }
}
