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

import static pw.chew.chewbotcca.util.MiscUtil.bytesToFriendly;

// %^stats command
public class StatsCommand extends SlashCommand {
    private final static Instant startTime = Instant.now();

    private static int sentMessages = 0;
    private static int executedCommands = 0;

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

    /**
     * Generates an embed consisting of the bot's stats
     *
     * @param jda JDA to retrieve data
     * @return An embed
     */
    private MessageEmbed generateStatsEmbed(JDA jda) {
        // Gather memory data
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        String memoryUsage = bytesToFriendly(memoryUsed / 1024) + "/" + bytesToFriendly(runtime.totalMemory() / 1024);

        // TODO: Cpu stats?
        // OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

        // Gather other data. All must be strings
        String author = jda.retrieveUserById("476488167042580481").complete().getAsMention();
        long uptimeInSeconds = Instant.now().toEpochMilli() - startTime.toEpochMilli() / 1000;
        String uptime = DateTime.timeAgoFromNow(startTime).replaceAll(", ", ",\n");
        String servers = String.valueOf(jda.getGuildCache().size());
        // TODO: Round commands/messages per x to 4 decimal
        // DecimalFormat df = new DecimalFormat("#.####");
        // df.setRoundingMode(RoundingMode.CEILING);
        // float commandsPerMinute = (float)getExecutedCommandsCount() / (float)(uptimeInSeconds / 60);
        // float messagesPerSecond = (float)getMessageCount() / (float)uptimeInSeconds;
        // String commands = getExecutedCommandsCount() + String.format(" [%s/m]", df.format(commandsPerMinute));
        // String messages = getMessageCount() + String.format(" [%s/s]", df.format(messagesPerSecond));
        String commands = String.valueOf(getExecutedCommandsCount());
        String messages = String.valueOf(getMessageCount());

        return new EmbedBuilder()
            .setTitle("Chewbotcca - A basic, yet functioning, Discord bot")
            .addField("Author", author, true)
            .addField("Code", "[View code on GitHub](https://github.com/Chewbotcca/Discord)", true)
            .addField("Library", "[JDA " + JDAInfo.VERSION + "](" + JDAInfo.GITHUB + ")", true)
            // Convert the time difference into a time ago
            .addField("Uptime", uptime, true)
            // Get the server count. NOT GUILD NOT GUILD NOT GUILD
            .addField("Servers", servers, true)
            // Memory usage
            .addField("Memory", memoryUsage, true)
            // Sent commands
            .addField("Commands Ran", commands, true)
            // Sent messages
            .addField("Messages", messages, true)
            .setColor(0xd084)
            .build();
    }

    public static void incrementMessageCount() {
        sentMessages++;
    }

    public static void incrementCommandCount() {
        executedCommands++;
    }

    public static int getMessageCount() {
        return sentMessages;
    }

    public static int getExecutedCommandsCount() {
        return executedCommands;
    }
}
