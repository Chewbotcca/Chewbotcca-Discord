package pw.chew.Chewbotcca;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.chew.Chewbotcca.commands.FeedbackCommand;
import pw.chew.Chewbotcca.commands.LastFMCommand;
import pw.chew.Chewbotcca.commands.MixerCommand;
import pw.chew.Chewbotcca.commands.RubyGemsCommand;
import pw.chew.Chewbotcca.commands.about.HelpCommand;
import pw.chew.Chewbotcca.commands.about.InviteCommand;
import pw.chew.Chewbotcca.commands.about.PingCommand;
import pw.chew.Chewbotcca.commands.about.StatsCommand;
import pw.chew.Chewbotcca.commands.cat.CatCommand;
import pw.chew.Chewbotcca.commands.cat.CatFactCommand;
import pw.chew.Chewbotcca.commands.cat.DogCommand;
import pw.chew.Chewbotcca.commands.english.DefineCommand;
import pw.chew.Chewbotcca.commands.english.UrbanDictionaryCommand;
import pw.chew.Chewbotcca.commands.github.GHIssueCommand;
import pw.chew.Chewbotcca.commands.github.GHRepoCommand;
import pw.chew.Chewbotcca.commands.github.GHUserCommand;
import pw.chew.Chewbotcca.commands.google.YouTubeCommand;
import pw.chew.Chewbotcca.commands.info.*;
import pw.chew.Chewbotcca.commands.minecraft.MCPHNodesCommand;
import pw.chew.Chewbotcca.commands.minecraft.MCServerCommand;
import pw.chew.Chewbotcca.commands.minecraft.MCStatusCommand;
import pw.chew.Chewbotcca.commands.minecraft.MCUserCommand;
import pw.chew.Chewbotcca.commands.misc.RollCommand;
import pw.chew.Chewbotcca.commands.owner.NewIssueCommand;
import pw.chew.Chewbotcca.commands.owner.ShutdownCommand;
import pw.chew.Chewbotcca.commands.quotes.AcronymCommand;
import pw.chew.Chewbotcca.commands.quotes.QuoteCommand;
import pw.chew.Chewbotcca.commands.quotes.TRBMBCommand;
import pw.chew.Chewbotcca.commands.settings.ProfileCommand;
import pw.chew.Chewbotcca.listeners.MagReactListener;
import pw.chew.Chewbotcca.listeners.SendJoinedOrLeftGuildListener;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    static Properties prop = new Properties();
    public static JDA jda;
    public static Instant start;
    public static EventWaiter waiter;

    public static void main(String[] args) throws LoginException, IOException {
        prop.load(new FileInputStream("bot.properties"));

        Sentry.init(prop.getProperty("sentry-dsn")).setEnvironment(prop.getProperty("sentry-env"));

        waiter = new EventWaiter();

        CommandClientBuilder client = new CommandClientBuilder();

        client.useDefaultGame();
        client.setOwnerId(prop.getProperty("owner_id"));

        // Set your bot's prefix
        logger.info("Setting Prefix to " + prop.getProperty("prefix"));
        client.setPrefix(prop.getProperty("prefix"));

        client.useHelpBuilder(false);

        // Register commands
        client.addCommands(
                // About Module
                new HelpCommand(),
                new InviteCommand(),
                new PingCommand(),
                new StatsCommand(),

                // Cat Module
                new CatCommand(),
                new CatFactCommand(),
                new DogCommand(),

                // English Module
                new DefineCommand(),
                new UrbanDictionaryCommand(),

                // GitHub Module
                new GHIssueCommand(),
                new GHRepoCommand(),
                new GHUserCommand(),

                // Google Module
                new YouTubeCommand(),

                // Info Module
                new ChannelInfoCommand(),
                new InfoCommand(),
                new RoleInfoCommand(),
                new ServerInfoCommand(),
                new UserInfoCommand(),

                // Minecraft Module
                new MCPHNodesCommand(),
                new MCServerCommand(),
                new MCStatusCommand(),
                new MCUserCommand(),

                // Misc Module
                new RollCommand(),

                // Owner Module
                new NewIssueCommand(),
                new ShutdownCommand(),

                // Quotes Module
                new AcronymCommand(),
                new QuoteCommand(),
                new TRBMBCommand(),

                // Settings Module
                new ProfileCommand(),

                // Everything Else
                new FeedbackCommand(),
                new LastFMCommand(),
                new MixerCommand(),
                new RubyGemsCommand()
        );

        // Register JDA
        jda = JDABuilder.createDefault(prop.getProperty("token"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.ACTIVITY)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Booting..."))
                .addEventListeners(waiter, client.build())
                .build();

        start = Instant.now();

        // Register listeners
        jda.addEventListener(
                new MagReactListener(),
                new SendJoinedOrLeftGuildListener()
        );
    }

    public static JDA getJDA() {
        return jda;
    }

    public static EventWaiter getWaiter() {
        return waiter;
    }

    public static Properties getProp() {
        return prop;
    }
}
