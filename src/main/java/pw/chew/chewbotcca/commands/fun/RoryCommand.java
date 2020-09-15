package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class RoryCommand extends Command {
    private static List<String> roryImages = null;

    public RoryCommand() {
        this.name = "rory";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (roryImages == null) {
            getRoryImages();
            await().atMost(10, TimeUnit.SECONDS).until(() -> roryImages != null && roryImages.size() > 50);
        }

        String url = roryImages.get(new Random().nextInt(roryImages.size()));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Rory :3");
        embed.setImage(url);
        event.reply(embed.build());
    }

    public static void getRoryImages() {
        LoggerFactory.getLogger(RoryCommand.class).debug("Gathering Rory Images...");
        roryImages = new ArrayList<>();
        Guild roryServer = Main.getJDA().getGuildById("134445052805120001");
        if (roryServer == null)
            return;
        TextChannel roryChannel = roryServer.getTextChannelById("752063016425619487");
        if (roryChannel == null)
            return;
        roryChannel.getHistoryFromBeginning(100).queue((messages -> {
            for (Message message : messages.getRetrievedHistory()) {
                if (!message.getAttachments().isEmpty()) {
                    Message.Attachment attachment = message.getAttachments().get(0);
                    roryImages.add(attachment.getUrl());
                }
            }
        }));
        LoggerFactory.getLogger(RoryCommand.class).info("Gathered Rory images!");
    }
}
