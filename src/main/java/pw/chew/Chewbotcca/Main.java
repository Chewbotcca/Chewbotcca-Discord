package pw.chew.Chewbotcca;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.chew.Chewbotcca.commands.about.HelpCommand;
import pw.chew.Chewbotcca.commands.about.InviteCommand;
import pw.chew.Chewbotcca.commands.about.PingCommand;
import pw.chew.Chewbotcca.commands.about.StatsCommand;
import pw.chew.Chewbotcca.commands.cat.CatCommand;
import pw.chew.Chewbotcca.commands.english.DefineCommand;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    static Properties prop = new Properties();
    public static JDA jda;

    public static void main(String[] args) throws LoginException, IOException {
        prop.load(new FileInputStream("bot.properties"));

        EventWaiter waiter = new EventWaiter();

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

                // English Module
                new DefineCommand()
        );

        // Register listeners

        // Register JDA
        jda = JDABuilder.createDefault(prop.getProperty("token"))
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Booting..."))
                .addEventListeners(waiter, client.build())
                .build();
    }

    public JDA getJDA() {
        return jda;
    }

    public static Properties getProp() {
        return prop;
    }
}
