/*
 * Copyright (C) 2024 Chewbotcca
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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import pw.chew.chewbotcca.util.DateTime;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;

import static pw.chew.chewbotcca.util.MiscUtil.bytesToFriendly;
import static pw.chew.chewbotcca.util.MiscUtil.delimitNumber;

/**
 * <h2><code>/stats</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/stats">Docs</a>
 */
public class StatsCommand extends SlashCommand {
    private final static Instant startTime = Instant.now();

    private static int sentMessages = 0;
    private static int executedCommands = 0;

    public StatsCommand() {
        this.name = "stats";
        this.help = "Cool stats about the bot!";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(generateStatsEmbed(event.getJDA())).setEphemeral(true).queue();
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
        // double cpuLoad = operatingSystemMXBean.getSystemLoadAverage();

        // Gather other data. All must be strings
        long uptimeInSeconds = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        String uptime = DateTime.timeAgoFromNow(startTime).replaceAll(", ", ",\n");
        long servers = jda.getGuildCache().size();
        int users = jda.retrieveApplicationInfo().complete().getUserInstallCount();
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        // rounded to 4 decimal places
        float commandsPerMinute = (float) executedCommands / ((float) uptimeInSeconds / 60);
        float messagesPerSecond = (float) sentMessages / (float) uptimeInSeconds;
        String commands = delimitNumber(executedCommands) + String.format(" [%s/m]", df.format(commandsPerMinute));
        String messages = delimitNumber(sentMessages) + String.format(" [%s/s]", df.format(messagesPerSecond));

        return new EmbedBuilder()
            .setTitle("Chewbotcca - A basic, yet functioning, Discord bot")
            // Basic bot info
            .addField("Author", "[Chew](https://github.com/Chew)", true)
            .addField("Code", "[View code on GitHub](https://github.com/Chewbotcca/Chewbotcca-Discord)", true)
            .addField("Library", "[JDA " + JDAInfo.VERSION + "](" + JDAInfo.GITHUB + ")", true)
            // Convert the time difference into a time ago.
            .addField("Uptime", uptime, true)
            // Get the installation count.
            .addField("Installs", "Servers: %s\nUsers: %s".formatted(servers, users), true)
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
