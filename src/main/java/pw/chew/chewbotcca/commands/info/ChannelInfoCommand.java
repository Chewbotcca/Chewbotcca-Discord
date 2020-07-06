package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
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
        String mode = "";

        if(args.contains("pins")) {
            mode = "pins";
            args = args.replace("pins", "");
        }
        args = args.replace(" ", "");

        GuildChannel channel;
        if(args.length() == 0) {
            channel = commandEvent.getGuild().getGuildChannelById(commandEvent.getChannel().getId());
        } else if(args.contains("<#")) {
            String id = args.replace("<#", "").replace(">", "");
            channel = commandEvent.getGuild().getGuildChannelById(id);
        } else {
            try {
                channel = commandEvent.getGuild().getGuildChannelById(args);
            } catch(NumberFormatException e) {
                commandEvent.reply("This channel could not be found!");
                return;
            }
        }

        if(channel == null) {
            commandEvent.reply("This channel could not be found!");
            return;
        }

        if(mode.equals("pins") && channel.getType() == ChannelType.TEXT) {
            commandEvent.reply(getPinsInfo((TextChannel)channel, commandEvent).build());
        } else if(mode.equals("pins")) {
            commandEvent.reply("Pins sub-command only works in Text channels!");
        } else {
            commandEvent.reply(gatherMainInfo(channel, commandEvent).build());
        }
    }

    public EmbedBuilder gatherMainInfo(GuildChannel channel, CommandEvent commandEvent) {
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

        return e;
    }

    public EmbedBuilder getPinsInfo(TextChannel channel, CommandEvent commandEvent) {
        EmbedBuilder e = new EmbedBuilder();
        AtomicReference<List<Message>> pinAR = new AtomicReference<>();
        channel.retrievePinnedMessages().queue((pinAR::set));
        await().atMost(5, TimeUnit.SECONDS).until(() -> pinAR.get() != null);
        List<Message> pins = pinAR.get();
        HashMap<String, Integer> topPins = new HashMap<>();
        for(Message message : pins) {
            String authorId = message.getAuthor().getId();
            int current = topPins.getOrDefault(authorId, 0);
            topPins.put(authorId, current + 1);
        }
        ArrayList<Map.Entry<String, Integer>> l = new ArrayList<>(topPins.entrySet());
        l.sort(Map.Entry.comparingByValue());
        Collections.reverse(l);
        List<CharSequence> top = new ArrayList<>();
        top.add("Total Pins: " + pins.size() + " / 50");
        for(int i = 0; i < l.size(); i++) {
            Map.Entry<String, Integer> entry = l.get(i);
            String user = entry.getKey();
            int pinCount = entry.getValue();
            User userById = commandEvent.getJDA().getUserById(user);
            String tag;
            if(userById == null) {
                tag = "Unknown User";
            } else {
                tag = userById.getAsTag();
            }
            top.add("#" + (i+1) + ": " + pinCount + " pins - " + tag);
        }
        e.setDescription(String.join("\n", top));
        return e;
    }
}