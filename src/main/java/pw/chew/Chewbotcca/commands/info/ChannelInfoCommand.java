package pw.chew.Chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

public class ChannelInfoCommand extends Command {
    public ChannelInfoCommand() {
        this.name = "channelinfo";
        this.aliases = new String[]{"cinfo"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String args = commandEvent.getArgs();
        GuildChannel channel = null;
        if(args.length() == 0) {
            channel = commandEvent.getGuild().getGuildChannelById(commandEvent.getChannel().getId());
        } else if(args.contains("<#")) {
            String id = args.replace("<#", "").replace(">", "");
            channel = commandEvent.getGuild().getGuildChannelById(id);
        } else {
            channel = commandEvent.getGuild().getGuildChannelById(args);
        }

        if(channel == null) {
            commandEvent.reply("This channel could not be found!");
            return;
        }

        EmbedBuilder e = new EmbedBuilder();
        if(channel.getType() == ChannelType.TEXT) {
            e.setTitle("Channel Info for #" + channel.getName());
            e.setDescription(((TextChannel)channel).getTopic());
        } else {
            e.setTitle("Channel Info for " + channel.getName());
        }

        e.addField("ID", channel.getId(), true);

        if (channel.getType() == ChannelType.TEXT || channel.getType() == ChannelType.VOICE)
            e.addField("Users in Channel", String.valueOf(channel.getMembers().size()), true);

        e.addField("Type", channel.getType().toString(), true);

        if(channel.getType() == ChannelType.TEXT && commandEvent.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            AtomicReference<List<Webhook>> hooks = new AtomicReference<>();
            ((TextChannel) channel).retrieveWebhooks().queue((hooks::set));
            await().atMost(5, TimeUnit.SECONDS).until(() -> hooks.get() != null);
            e.addField("Webhooks", String.valueOf(hooks.get().size()), true);
        }

        e.setFooter("Channel Created");
        e.setTimestamp(channel.getTimeCreated());

        commandEvent.reply(e.build());
    }
}