package pw.chew.chewbotcca.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class UnsuppressCommand extends Command {

    public UnsuppressCommand() {
        this.name = "unsuppress";
        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        Message message;
        try {
            message = getMessage(event, args);
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage());
            return;
        }

        if (message == null) {
            event.reply("Message could not be found or retrieved!");
            return;
        }

        message.suppressEmbeds(false).queue(
            unused -> event.reply("Successfully unsuppressed the embed!"),
            throwable -> event.reply("Could not unsuppress the embed!")
        );
    }

    public Message getMessage(CommandEvent event, String args) {
        Guild server = event.getGuild();
        TextChannel channel = event.getTextChannel();
        Message message;

        // If Copy Link URL
        // 4 => Server
        // 5 => Channel
        // 6 => Message
        if (args.contains("discord.com/channels/")) {
            String[] components = args.split("/");
            if (!components[4].equals(server.getId())) {
                throw new IllegalArgumentException("Server must be the same as thi server!");
            }
            if (!components[5].equals(channel.getId())) {
                channel = server.getTextChannelById(components[5]);
            }
            if (channel == null) {
                throw new IllegalArgumentException("This channel does not exist on this server!");
            }
            return channel.retrieveMessageById(components[6]).complete();
        }

        String[] info = args.split(" ");

        if (info.length == 1) {
            // Retrieve the message
            channel.retrieveMessageById(info[0]).complete();
        } else {
            // Get the second (channel id) arg
            String chanId = info[0];
            chanId = chanId.replace("<#", "").replace(">", "");
            // Get the text channel
            channel = event.getJDA().getTextChannelById(chanId);
            // If it's not null
            if (channel != null) {
                return channel.retrieveMessageById(info[1]).complete();
            } else {
                throw new IllegalArgumentException("Invalid Channel ID.");
            }
        }
        return null;
    }
}
