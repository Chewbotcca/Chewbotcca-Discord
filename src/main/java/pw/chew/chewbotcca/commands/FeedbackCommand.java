package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

public class FeedbackCommand extends Command {

    public FeedbackCommand() {
        this.name = "feedback";
        this.cooldown = 30;
        this.cooldownScope = CooldownScope.USER;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        var feedback = commandEvent.getArgs();
        var embed = new EmbedBuilder();
        embed.setTitle("New Feedback!");
        embed.setColor(Color.decode("#6166A8"));
        embed.setDescription(feedback);
        embed.setTimestamp(Instant.now());
        embed.setAuthor(commandEvent.getAuthor().getAsTag(), null, commandEvent.getAuthor().getAvatarUrl());
        embed.addField("User ID", commandEvent.getAuthor().getId(), true);
        if(commandEvent.getChannelType() == ChannelType.PRIVATE) {
            embed.addField("Server", "Sent from a DM", true);
        } else {
            embed.addField("Server", "Name: " + commandEvent.getGuild().getName() + "\n" + commandEvent.getGuild().getId(), true);
        }
        Objects.requireNonNull(commandEvent.getJDA().getTextChannelById("720118610785468446")).sendMessage(embed.build()).queue();
        commandEvent.reply("I have successfully sent the feedback! Feel free to see it on the help server with `%^invite`");
    }
}