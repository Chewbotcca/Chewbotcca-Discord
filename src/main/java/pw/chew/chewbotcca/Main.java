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
import pw.chew.chewbotcca.commands.DiscrimCommand;
import pw.chew.chewbotcca.commands.FeedbackCommand;
import pw.chew.chewbotcca.commands.LastFMCommand;
import pw.chew.chewbotcca.commands.RubyGemsCommand;
import pw.chew.chewbotcca.commands.about.HelpCommand;
import pw.chew.chewbotcca.commands.about.InviteCommand;
import pw.chew.chewbotcca.commands.about.PingCommand;
import pw.chew.chewbotcca.commands.about.StatsCommand;
import pw.chew.chewbotcca.commands.english.DefineCommand;
import pw.chew.chewbotcca.commands.english.UrbanDictionaryCommand;
import pw.chew.chewbotcca.commands.fun.*;
import pw.chew.chewbotcca.commands.github.GHIssueCommand;
import pw.chew.chewbotcca.commands.github.GHRepoCommand;
import pw.chew.chewbotcca.commands.github.GHUserCommand;
import pw.chew.chewbotcca.commands.google.YouTubeCommand;
import pw.chew.chewbotcca.commands.info.*;
import pw.chew.chewbotcca.commands.minecraft.*;
import pw.chew.chewbotcca.commands.moderation.BanCommand;
import pw.chew.chewbotcca.commands.moderation.RoleCommand;
import pw.chew.chewbotcca.commands.owner.NewIssueCommand;
import pw.chew.chewbotcca.commands.owner.ShutdownCommand;
import pw.chew.chewbotcca.commands.quotes.AcronymCommand;
import pw.chew.chewbotcca.commands.quotes.QuoteCommand;
import pw.chew.chewbotcca.commands.quotes.TRBMBCommand;
import pw.chew.chewbotcca.commands.settings.ProfileCommand;
import pw.chew.chewbotcca.commands.settings.ServerSettingsCommand;
import pw.chew.chewbotcca.listeners.MagReactListener;
import pw.chew.chewbotcca.listeners.MessageHandler;
import pw.chew.chewbotcca.listeners.ServerJoinLeaveListener;
import pw.chew.chewbotcca.util.PropertiesManager;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// The Main Bot class. Where all the magic happens!
public class Main {
    // Instance variables
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static JDA jda;
    private static DiscordBotListAPI topgg;

    public static void main(String[] args) throws LoginException, IOException {
        // Load properties into the PropertiesManager
        Properties prop = new Properties();
        prop.load(new FileInputStream("bot.properties"));
        PropertiesManager.loadProperties(prop);

        // Initialize Sentry to catch errors
        Sentry.init(PropertiesManager.getSentryDsn()).setEnvironment(PropertiesManager.getSentryEnv());

        // Initialize Top.gg for stats posting
        topgg = new DiscordBotListAPI.Builder()
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

        // Register commands
        client.addCommands(
                // About Module
                new HelpCommand(),
                new InviteCommand(),
                new PingCommand(),
                new StatsCommand(),

                // English Module
                new DefineCommand(),
                new UrbanDictionaryCommand(),

                // Fun Module
                new CatCommand(),
                new CatFactCommand(),
                new DogCommand(),
                new EightBallCommand(),
                new NumberFactCommand(),
                new QRCodeCommand(),
                new RedditCommand(),
                new RollCommand(),
                new RoryCommand(),
                new SpigotDramaCommand(chew),

                // Google Module
                new YouTubeCommand(),

                // Info Module
                new BotInfoCommand(),
                new ChannelInfoCommand(),
                new InfoCommand(),
                new RoleInfoCommand(waiter),
                new ServerInfoCommand(waiter),
                new UserInfoCommand(),

                // Minecraft Module
                new MCIssueCommand(),
                new MCPHNodesCommand(mcpro),
                new MCServerCommand(),
                new MCStatusCommand(),
                new MCUserCommand(),
                new MCWikiCommand(),

                // Moderation Commands
                new BanCommand(),
                new RoleCommand(),

                // Owner Module
                new ShutdownCommand(),

                // Quotes Module
                new AcronymCommand(chew),
                new QuoteCommand(),
                new TRBMBCommand(chew),

                // Settings Module
                new ProfileCommand(),
                new ServerSettingsCommand(),

                // Everything Else
                new DiscrimCommand(waiter),
                new FeedbackCommand(),
                new LastFMCommand(),
                new RubyGemsCommand()
        );

        // Add GitHub commands only if it properly initiated
        if(github != null) {
            client.addCommands(
                    // From Owner Module
                    new NewIssueCommand(github),

                    new GHIssueCommand(github),
                    new GHRepoCommand(github),
                    new GHUserCommand(github)
            );
        }

        // Register JDA
        jda = JDABuilder.createDefault(PropertiesManager.getToken())
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
    }

    // Get the JDA if needed
    public static JDA getJDA() {
        return jda;
    }

    // Get the Topgg API when needed
    public static DiscordBotListAPI getTopgg() {
        return topgg;
    }
}
