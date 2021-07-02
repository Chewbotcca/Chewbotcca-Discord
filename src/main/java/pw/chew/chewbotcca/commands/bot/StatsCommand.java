/*
 * Copyright (C) 2021 Chewbotcca
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
package pw.chew.chewbotcca.commands.bot;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import pw.chew.chewbotcca.util.DateTime;

import java.time.Instant;

import static pw.chew.chewbotcca.commands.services.github.GHRepoCommand.bytesToFriendly;

// %^stats command
public class StatsCommand extends SlashCommand {
    private final static Instant startTime = Instant.now();

    public StatsCommand() {
        this.name = "stats";
        this.help = "Cool stats about the bot!";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(generateStatsEmbed(event.getJDA())).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(generateStatsEmbed(commandEvent.getJDA()));
    }

    private MessageEmbed generateStatsEmbed(JDA jda) {
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        String memoryUsage = bytesToFriendly(memoryUsed / 1024) + "/" + bytesToFriendly(runtime.totalMemory() / 1024);

        // TODO: Cpu stats?
        // OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        return new EmbedBuilder()
            .setTitle("Chewbotcca - A basic, yet functioning, discord bot")
            .addField("Author", "<@!476488167042580481>", true)
            .addField("Code", "[View code on GitHub](https://github.com/Chewbotcca/Discord)", true)
            .addField("Library", "[JDA " + JDAInfo.VERSION + "](" + JDAInfo.GITHUB + ")", true)
            // Convert the time difference into a time ago
            .addField("Uptime", DateTime.timeAgo(Instant.now().toEpochMilli() - startTime.toEpochMilli()), true)
            // Get the server count. NOT GUILD NOT GUILD NOT GUILD
            .addField("Servers", String.valueOf(jda.getGuildCache().size()), true)
            // Memory usage
            .addField("Memory", memoryUsage, true)
            .setColor(0xd084)
            .build();
    }
}
