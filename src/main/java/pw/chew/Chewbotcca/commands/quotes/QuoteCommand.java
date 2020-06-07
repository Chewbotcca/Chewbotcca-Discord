package pw.chew.Chewbotcca.commands.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

public class QuoteCommand extends Command {

    public QuoteCommand() {
        this.name = "quote";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        String[] args = event.getArgs().split(" ");
        String mesId = args[0];
        AtomicReference<Message> message = new AtomicReference<>();
        if(args.length == 1) {
            event.getChannel().retrieveMessageById(mesId).queue(message::set,
                    (exception) -> event.reply("Invalid Message ID. If this message is in a separate channel, provide its ID as well.")
            );
            await().atMost(5, TimeUnit.SECONDS).until(() -> message.get() != null);
        } else {
            String chanId = args[1];
            TextChannel channel;
            channel = event.getJDA().getTextChannelById(chanId);
            if (channel != null) {
                channel.retrieveMessageById(mesId).queue(message::set,
                        (exception) -> event.reply("Invalid Message ID. That message might not exist in the channel provided.")
                );
                await().atMost(5, TimeUnit.SECONDS).until(() -> message.get() != null);
            } else {
                event.reply("Invalid Channel ID.");
                return;
            }
        }

        if(message.get() == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Quote");
        embed.setDescription(message.get().getContentRaw());
        embed.setTimestamp(message.get().getTimeCreated());
        embed.setAuthor(message.get().getAuthor().getAsTag(), null, message.get().getAuthor().getAvatarUrl());
        boolean thisGuild = event.getGuild() == message.get().getGuild();
        if(!thisGuild)
            embed.addField("Server", message.get().getGuild().getName(), true);
        if(args.length > 1 && !thisGuild) {
            embed.addField("Channel", message.get().getChannel().getName(), true);
        } else if (args.length > 1) {
            embed.addField("Channel", ((TextChannel) message.get().getChannel()).getAsMention(), true);
        }
        embed.addField("Jump", "[Link](" + message.get().getJumpUrl() + ")", true);
        AtomicReference<Member> member = new AtomicReference<>();
        AtomicBoolean bruh = new AtomicBoolean(false);
        event.getGuild().retrieveMember(message.get().getAuthor()).queue(member::set, (oh) -> bruh.set(true));
        await().atMost(5, TimeUnit.SECONDS).until(() -> member.get() != null || bruh.get());

        if(!bruh.get())
            embed.setColor(member.get().getColor());
        event.reply(embed.build());
    }
}
