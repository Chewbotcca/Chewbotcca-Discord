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
import com.mcprohosting.MCProHostingAPI;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.discordbots.api.client.DiscordBotListAPI;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.chew.api.ChewAPI;
import pw.chew.chewbotcca.listeners.MagReactListener;
import pw.chew.chewbotcca.listeners.MessageHandler;
import pw.chew.chewbotcca.listeners.ServerJoinLeaveListener;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.PropertiesManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// The Main Bot class. Where all the magic happens!
public class Main {
    // Instance variables
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws LoginException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        // Load properties into the PropertiesManager
        Properties prop = new Properties();
        prop.load(new FileInputStream("bot.properties"));
        PropertiesManager.loadProperties(prop);

        // Initialize Sentry to catch errors
        Sentry.init(PropertiesManager.getSentryDsn()).setEnvironment(PropertiesManager.getSentryEnv());

        // Initialize Top.gg for stats posting
        DiscordBotListAPI topgg = new DiscordBotListAPI.Builder()
            .token(PropertiesManager.getTopggToken())
            .botId(PropertiesManager.getClientId())
            .build();

        // Initialize the waiter and client
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder client = new CommandClientBuilder();

        // Set the client settings
        client.useDefaultGame();
        client.setOwnerId(PropertiesManager.getOwnerId());
        client.setPrefix(PropertiesManager.getPrefix());

        client.useHelpBuilder(false);

        // Initialize GitHub for GitHub commands
        GitHub github = null;
        try {
            github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error occurred initializing GitHub. How did this happen?");
        }

        // Initialize APIs
        ChewAPI chew = new ChewAPI();
        MCProHostingAPI mcpro = new MCProHostingAPI();

        client.addCommands(getCommands());

        // Register JDA
        JDA jda = JDABuilder.createDefault(PropertiesManager.getToken())
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .enableIntents(GatewayIntent.GUILD_PRESENCES)
            .enableCache(CacheFlag.ACTIVITY)
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(Activity.playing("Booting..."))
            .addEventListeners(waiter, client.build())
            .build();

        // Register listeners
        jda.addEventListener(
                new MagReactListener(),
                new MessageHandler(),
                new ServerJoinLeaveListener()
        );

        new Memory(waiter, jda, chew, mcpro, github, topgg);
    }

    /**
     * Loads all commands from "commands" folder.
     * @return an array of commands
     */
    private static Command[] getCommands() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        File directory = new File("./src/main/java/pw/chew/chewbotcca/commands");
        String packageName = "pw.chew.chewbotcca.commands.";
        List<Command> commands = new ArrayList<>();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        for (String content : directory.list()) {
            if (content.startsWith(".") || content.contains(".rb"))
                continue;
            if (content.endsWith(".java")) {
                LoggerFactory.getLogger(Main.class).debug("Loading command: " + content);
                commands.add((Command) classLoader.loadClass(packageName + content.replace(".java", "")).newInstance());
                LoggerFactory.getLogger(Main.class).debug("Loaded command: " + content);
            } else {
                LoggerFactory.getLogger(Main.class).debug("Searching directory: " + content);
                File subdirectory = new File("./src/main/java/pw/chew/chewbotcca/commands/" + content);
                String subpackageName = "pw.chew.chewbotcca.commands." + content + ".";
                for (String subcontent : subdirectory.list()) {
                    LoggerFactory.getLogger(Main.class).debug("Loading command: " + subcontent);
                    if (subcontent.startsWith(".") || subcontent.contains(".rb"))
                        continue;
                    commands.add((Command) classLoader.loadClass(subpackageName + subcontent.replace(".java", "")).newInstance());
                    LoggerFactory.getLogger(Main.class).debug("Loaded command: " + subcontent);
                }
            }
        }

        return commands.toArray(new Command[0]);
    }
}
