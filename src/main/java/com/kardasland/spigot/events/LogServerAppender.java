package com.kardasland.spigot.events;

import com.kardasland.veldoryadiscord.VeldoryaJDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;


@Deprecated
@Plugin(name = "Log4JAppender", category = "Core", elementType = "appender", printObject = true)
public class LogServerAppender extends AbstractAppender {

    public LogServerAppender() {
        super("ConsoleMirrorModule", null,
                PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss} %level]: %msg").build());
    }

    @Override
    public void append(LogEvent e) {
        if (VeldoryaJDA.instance.getGuild() != null){
            try{
                if (VeldoryaJDA.jda != null){
                    String formattedMessage = e.getMessage().getFormattedMessage().replaceAll("(?i)\u007F[0-9A-FK-ORX]" , "").replaceAll("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})*)?[m|K]" , "");
                    TextChannel textChannel = VeldoryaJDA.instance.getGuild().getTextChannelById(VeldoryaJDA.instance.getConsoleChannel());
                    textChannel.sendMessage(formattedMessage).queue();
                    getStatusLogger().exit();
                }
            }catch (Exception ignored){}

        }
    }
}