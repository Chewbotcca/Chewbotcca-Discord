/*
 * Copyright (C) 2022 Chewbotcca
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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import javax.annotation.Nonnull;
import java.awt.Color;

// Listen to server joins
public class ServerJoinLeaveListener extends ListenerAdapter {
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
        EmbedBuilder e = new EmbedBuilder();
        // Say join or leave
        if(joined) {
            e.setTitle("I joined a server!");
            e.setColor(Color.GREEN);
        } else {
            e.setTitle("I left a server!");
            e.setColor(Color.RED);
        }
        e.addField("Name", guild.getName(), true);
        e.addField("ID", guild.getId(), true);
        e.addField("Members", String.valueOf(guild.getMemberCount()), true);
        e.addField("New Server Count", String.valueOf(servers), false);
        TextChannel joinChannel = jda.getTextChannelById("718316552272740414");
        if(joinChannel == null)
            LoggerFactory.getLogger(this.getClass()).error("Join Channel not found, this is not good.");
        else
            joinChannel.sendMessageEmbeds(e.build()).queue();

        if(!PropertiesManager.getSentryEnv().equals("development")) {
            syncStats(1, servers);
        }
    }

    public static void syncStats(int shards, long servers) {
        // Put all stats here, this varies depending on list so we just throw it all there who cares
        JSONObject stats = new JSONObject()
            .put("shardCount", 1)
            .put("shard_count", 1)
            .put("guildCount", servers)
            .put("server_count", servers);
        RestClient.post("https://discord.bots.gg/api/v1/bots/" + PropertiesManager.getClientId() + "/stats", PropertiesManager.getDbotsToken(), stats);
        RestClient.post("https://top.gg/api/bots/" + PropertiesManager.getClientId() + "/stats", PropertiesManager.getTopggToken(), stats);
        RestClient.post("https://api.discordextremelist.xyz/v2/bot/" + PropertiesManager.getClientId() + "/stats", PropertiesManager.getDELToken(), stats);
    }
}
