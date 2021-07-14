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
package pw.chew.chewbotcca;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.chew.api.ChewAPI;
import pw.chew.chewbotcca.listeners.BotCommandListener;
import pw.chew.chewbotcca.listeners.InteractionHandler;
import pw.chew.chewbotcca.listeners.MessageHandler;
import pw.chew.chewbotcca.listeners.ReactListener;
import pw.chew.chewbotcca.listeners.ReadyListener;
import pw.chew.chewbotcca.listeners.ServerJoinLeaveListener;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.objects.ServerSettings;
import pw.chew.chewbotcca.util.DatabaseHelper;
import pw.chew.chewbotcca.util.PropertiesManager;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

// The Main Bot class. Where all the magic happens!
public class Chewbotcca {
    // Instance variables
    private static final Logger logger = LoggerFactory.getLogger(Chewbotcca.class);

    public static void main(String[] args) throws LoginException, IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        // Load properties into the PropertiesManager
        Properties prop = new Properties();
        prop.load(new FileInputStream("bot.properties"));
        PropertiesManager.loadProperties(prop);

        // Initialize Database for Server and Profile storage
        logger.info("Connecting to database...");
        DatabaseHelper.openConnection();
        logger.info("Connected!");

        // Initialize Sentry to catch errors
        Sentry.init(options -> {
            options.setDsn(PropertiesManager.getSentryDsn());
            options.setEnvironment(PropertiesManager.getSentryEnv());
        });

        // Initialize the waiter and client
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder client = new CommandClientBuilder();

        // Set the client settings
        client.useDefaultGame();
        client.setOwnerId(PropertiesManager.getOwnerId());
        client.setPrefix(PropertiesManager.getPrefix());
        client.setPrefixes(new String[]{"<@!" + PropertiesManager.getClientId() + "> "});
        client.setPrefixFunction(event -> {
            // If a DM
            if (event.getChannelType() == ChannelType.PRIVATE) {
                return "";
            }
            if (event.isFromGuild()) {
                // Get server prefix, as long as it's cached.
                ServerSettings ss = ServerSettings.getServerIfCached(event.getGuild().getId());
                return ss == null ? null : ss.getPrefix();
            }
            return null;
        });

        client.useHelpBuilder(false);

        // Initialize GitHub for GitHub commands
        GitHub github = null;
        try {
            github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error occurred initializing GitHub. How did this happen?");
        }

        client.addCommands(getCommands());
        client.addSlashCommands(getSlashCommands());

        // Temporary measure to test Slash Commands
        client.forceGuildOnly(PropertiesManager.forceGuildId());
        client.setManualUpsert(true);

        // Listen for commands, errors, and more
        client.setListener(new BotCommandListener());

        // Finalize the command client
        CommandClient commandClient = client.build();

        // Register JDA
        JDA jda = JDABuilder.createDefault(PropertiesManager.getToken())
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
            .enableCache(CacheFlag.ACTIVITY, CacheFlag.ROLE_TAGS)
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(Activity.playing("Booting..."))
            .addEventListeners(
                waiter, commandClient, // JDA-Chewtils stuff
                new InteractionHandler(), // Handle interactions
                new MessageHandler(), // Handle messages
                new ReactListener(), // Handle reactions
                new ReadyListener(), // Ran on boot
                new ServerJoinLeaveListener() // Listen for server count changes for stats
            ).build();

        Memory.remember(waiter, jda, new ChewAPI(), github, commandClient);
    }

    /**
     * Gathers all commands from "commands" package.
     *
     * @return an array of commands
     */
    private static Command[] getCommands() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Reflections reflections = new Reflections("pw.chew.chewbotcca.commands");
        Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);
        List<Command> commands = new ArrayList<>();

        for (Class<? extends Command> theClass : subTypes) {
            // Don't load SubCommands or SlashCommands
            if (theClass.getName().contains("SubCommand") || theClass.getName().contains("SlashCommand"))
                continue;
            try {
                commands.add(theClass.getDeclaredConstructor().newInstance());
                LoggerFactory.getLogger(theClass).debug("Loaded Command Successfully!");
            } catch (InstantiationException ignored) {
                // Tried to load a Slash-only command. Safe to ignore!
            }
        }

        return commands.toArray(new Command[0]);
    }

    /**
     * Gathers all SlashCommands from "commands" package.
     *
     * @return an array of commands
     */
    private static SlashCommand[] getSlashCommands() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Reflections reflections = new Reflections("pw.chew.chewbotcca.commands");
        Set<Class<? extends SlashCommand>> subTypes = reflections.getSubTypesOf(SlashCommand.class);
        List<SlashCommand> commands = new ArrayList<>();

        for (Class<? extends SlashCommand> theClass : subTypes) {
            // Don't load SubCommands
            if (theClass.getName().contains("SubCommand"))
                continue;
            commands.add(theClass.getDeclaredConstructor().newInstance());
            LoggerFactory.getLogger(theClass).debug("Loaded SlashCommand Successfully!");
        }

        return commands.toArray(new SlashCommand[0]);
    }
}
