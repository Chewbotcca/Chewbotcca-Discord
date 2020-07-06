package pw.chew.chewbotcca.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.Main;
import pw.chew.chewbotcca.util.PropertiesManager;

import javax.annotation.Nonnull;
import java.awt.*;

public class SendJoinedOrLeftGuildListener extends ListenerAdapter {
    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        execute(event.getGuild(), event.getJDA(), event);
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        execute(event.getGuild(), event.getJDA(), event);
    }

    private void execute(Guild guild, JDA jda, Object event) {
        boolean joined = event instanceof GuildJoinEvent;
        long servers = jda.getGuildCache().size();
        if(!PropertiesManager.getSentryEnv().equals("development"))
            Main.getTopgg().setStats((int) servers);
        EmbedBuilder e = new EmbedBuilder();
        if(joined) {
            e.setTitle("I joined a server!");
            e.setColor(Color.decode("#00ff00"));
        } else {
            e.setTitle("I left a server!");
            e.setColor(Color.decode("#ff0000"));
        }
        e.addField("Name", guild.getName(), true);
        e.addField("ID", guild.getId(), true);
        e.addField("Members", String.valueOf(guild.getMemberCount()), true);
        e.addField("New Server Count", String.valueOf(servers), false);
        TextChannel joinChannel = jda.getTextChannelById("718316552272740414");
        if(joinChannel == null)
            LoggerFactory.getLogger(this.getClass()).error("Join Channel not found, this is not good.");
        else
            joinChannel.sendMessage(e.build()).queue();
    }
}
