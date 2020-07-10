/*
 * Copyright (C) 2020 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

// Listen to server joins
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
        // If it's a join event
        boolean joined = event instanceof GuildJoinEvent;
        long servers = jda.getGuildCache().size();
        // Set server count unless in dev
        if(!PropertiesManager.getSentryEnv().equals("development"))
            Main.getTopgg().setStats((int) servers);
        EmbedBuilder e = new EmbedBuilder();
        // Say join or leave
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
