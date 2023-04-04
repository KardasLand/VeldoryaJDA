package com.kardasland.discord.ticket;

import com.kardasland.data.ConfigManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.configuration.file.FileConfiguration;

public class OpenTicketEvent extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("velOpenTicket")) {
            FileConfiguration ticketSystem = ConfigManager.get("ticketsystem.yml");
            if (ticketSystem.getBoolean("askForReason")) {
                TextInput body = TextInput.create("velTicketReason", ticketSystem.getString("messages.reasonForTicket.title"), TextInputStyle.SHORT)
                        .setPlaceholder("Cevabınızı girin.")
                        .setMinLength(5)
                        .setMaxLength(1000)
                        .build();

                Modal modal = Modal.create("velTalep", "Destek Talebi")
                        .addActionRows(ActionRow.of(body))
                        .build();
                event.replyModal(modal).queue();
            }
        }
    }
}
