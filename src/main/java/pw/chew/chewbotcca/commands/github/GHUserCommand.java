package pw.chew.chewbotcca.commands.github;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import pw.chew.chewbotcca.Main;

import java.io.IOException;

public class GHUserCommand extends Command {

    public GHUserCommand() {
        this.name = "ghuser";
        this.aliases = new String[]{"ghorg"};
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
        try {
            e.setThumbnail(user.getAvatarUrl());
            e.addField("Repositories", String.valueOf(user.getPublicRepoCount()), true);
            boolean isOrg = user.getType().equals("Organization");
            if(!isOrg) {
                e.setTitle("GitHub profile for " + user.getLogin(), "https://github.com/" + user.getLogin());
                e.setDescription(user.getBio());
                e.addField("Followers", String.valueOf(user.getFollowersCount()), true);
                e.addField("Following", String.valueOf(user.getFollowingCount()), true);
                if(user.getCompany() != null)
                    e.addField("Company", user.getCompany(), true);
                if(!user.getBlog().equals(""))
                    e.addField("Website", user.getBlog(), true);
                if(user.getTwitterUsername() != null)
                    e.addField("Twitter", "[@" + user.getTwitterUsername() + "](https://twitter.com/" + user.getTwitterUsername() + ")", true);
            } else {
                GHOrganization org = github.getOrganization(user.getLogin());
                e.setTitle("GitHub profile for Organization " + user.getLogin(), "https://github.com/" + user.getLogin());
                e.addField("Public Members", String.valueOf(org.listMembers().toArray().length), true);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        commandEvent.reply(e.build());
    }

}
