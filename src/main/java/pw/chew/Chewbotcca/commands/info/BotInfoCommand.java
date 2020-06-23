package pw.chew.Chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.awaitility.core.ConditionTimeoutException;
import org.discordbots.api.client.entity.Bot;
import pw.chew.Chewbotcca.Main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

public class BotInfoCommand extends Command {
    public BotInfoCommand() {
        this.name = "botinfo";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.aliases = new String[]{"binfo"};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        String[] args = commandEvent.getArgs().split(" ");
        if(args.length == 0) {
            commandEvent.reply("Please specify a bot with either a mention or its ID");
            return;
        }
        String botId = null;
        if(args[0].contains("<@")) {
            botId = args[0].replace("<@!", "").replace(">", "");
        } else {
            botId = args[0];
        }
        String list = "dbl";
        if(args.length > 1) {
            list = args[1].toLowerCase();
        }
        switch (list) {
            case "dbl", "top.gg", "topgg" -> commandEvent.reply(gatherTopggInfo(botId).build());
            default -> commandEvent.reply("Invalid Bot List! Possible: ```dbl, topgg```");
        }
    }

    private EmbedBuilder gatherTopggInfo(String id) {
        AtomicReference<Bot> bot = new AtomicReference<>();
        Main.topgg.getBot(id).whenComplete(((bot1, throwable) -> bot.set(bot1)));
        try {
            await().atMost(5, TimeUnit.SECONDS).until(() -> bot.get() != null);
        } catch(ConditionTimeoutException e) {
            return new EmbedBuilder().setTitle("Error!").setDescription("Timed out finding the bot, does it exist?");
        }

        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Bot Information");

        String certified;
        if(bot.get().isCertified()) {
            certified = "https://cdn.discordapp.com/emojis/392249976639455232.png";
        } else {
            certified = String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", id, bot.get().getAvatar(), "png");
        }

        e.setAuthor(bot.get().getUsername() + "#" + bot.get().getDiscriminator(), "https://top.gg/bot/" + id, certified);

        e.setThumbnail(String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", id, bot.get().getAvatar(), "png"));

        e.setDescription(bot.get().getShortDescription());

        List<String> owners = new ArrayList<>();

        e.addField("Bot ID", bot.get().getId(), true);

        e.addField("Server Count", String.valueOf(bot.get().getServerCount()), true);

        e.addField("Prefix", "`" + bot.get().getPrefix() + "`", true);

        e.addField("Points", "This Month: " + bot.get().getMonthlyPoints() + "\n" +
                "All Time: " + bot.get().getPoints(), true);

        List<String> tags = bot.get().getTags();

        if(tags.isEmpty()) {
            e.addField("Tags", "None", true);
        } else {
            e.addField("Tags", String.join(", ", tags), true);
        }

        List<CharSequence> links = new ArrayList<>();
        links.add("[Bot Page](https://top.gg/bot/" + id + ")");
        links.add("[Vote](https://top.gg/bot/" + id + "/vote)");
        if(!bot.get().getInvite().equals(""))
            links.add("[Invite](" + bot.get().getInvite() + ")");
        if(!bot.get().getWebsite().equals(""))
            links.add("[Website](" + bot.get().getWebsite() + ")");
        if(bot.get().getWebsite() != null)
            links.add("[Support](" + bot.get().getWebsite() + ")");

        e.addField("Links", String.join("\n", links), true);

        e.setFooter("Bot added");
        e.setTimestamp(bot.get().getApprovalTime());

        e.setColor(Color.decode("#43B581"));

        return e;
    }
}



