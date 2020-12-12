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
package pw.chew.chewbotcca;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
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
import pw.chew.chewbotcca.listeners.MessageHandler;
import pw.chew.chewbotcca.listeners.ReactListener;
import pw.chew.chewbotcca.listeners.ServerJoinLeaveListener;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.objects.ServerSettings;
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
public class Main {
    // Instance variables
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws LoginException, IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        // Load properties into the PropertiesManager
        Properties prop = new Properties();
        prop.load(new FileInputStream("bot.properties"));
        PropertiesManager.loadProperties(prop);

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

        // Register JDA
        JDA jda = JDABuilder.createDefault(PropertiesManager.getToken())
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .enableIntents(GatewayIntent.GUILD_PRESENCES)
            .enableCache(CacheFlag.ACTIVITY)
            .enableCache(CacheFlag.ROLE_TAGS)
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(Activity.playing("Booting..."))
            .addEventListeners(waiter, client.build())
            .build();

        // Register listeners
        jda.addEventListener(
                new ReactListener(),
                new MessageHandler(),
                new ServerJoinLeaveListener()
        );

        new Memory(waiter, jda, new ChewAPI(), github);
    }

    /**
     * Gathers all commands from "commands" package.
     * @return an array of commands
     */
    private static Command[] getCommands() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Reflections reflections =  new Reflections("pw.chew.chewbotcca.commands");
        Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);
        List<Command> commands = new ArrayList<>();

        for (Class<? extends Command> theClass : subTypes) {
            // Don't load SubCommands
            if (theClass.getName().contains("SubCommand"))
                continue;
            commands.add(theClass.getDeclaredConstructor().newInstance());
            LoggerFactory.getLogger(theClass).debug("Loaded Successfully!");
        }

        return commands.toArray(new Command[0]);
    }
}
