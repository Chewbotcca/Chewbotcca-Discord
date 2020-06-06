package pw.chew.Chewbotcca.commands.github;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.kohsuke.github.*;
import pw.chew.Chewbotcca.Main;

import java.awt.*;
import java.io.IOException;

public class GHUserCommand extends Command {

    public GHUserCommand() {
        this.name = "ghuser";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String username = commandEvent.getArgs();
        GitHub github;
        commandEvent.getChannel().sendTyping().queue();
        try {
            github = new GitHubBuilder().withOAuthToken(Main.getProp().getProperty("github")).build();
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred initializing GitHub. How did this happen?");
            return;
        }
        GHUser user;
        try {
            user = github.getUser(username);
        } catch (IOException e) {
            commandEvent.reply("Invalid username. Please make sure this user exists!");
            return;
        }
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("GitHub profile for " + user.getLogin(), "https://github.com/" + user.getLogin());
        try {
            e.setThumbnail(user.getAvatarUrl());
            e.addField("Repositories", String.valueOf(user.getPublicRepoCount()), true);
            e.addField("Followers", String.valueOf(user.getFollowersCount()), true);
            e.addField("Following", String.valueOf(user.getFollowingCount()), true);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        commandEvent.reply(e.build());
    }

}
