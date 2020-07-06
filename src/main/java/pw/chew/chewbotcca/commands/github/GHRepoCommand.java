package pw.chew.chewbotcca.commands.github;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.kohsuke.github.*;
import pw.chew.chewbotcca.Main;
import pw.chew.chewbotcca.util.PropertiesManager;

import java.io.IOException;
import java.text.DecimalFormat;

public class GHRepoCommand extends Command {

    public GHRepoCommand() {
        this.name = "ghrepo";
        this.aliases = new String[]{"githubrepo", "ghrepository"};
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String repoName = commandEvent.getArgs();
        if(!repoName.contains("/")) {
            commandEvent.reply("Make sure your input contains a UserOrOrg/RepositoryName (e.g. Chewbotcca/Discord).");
        }
        GitHub github;
        commandEvent.getChannel().sendTyping().queue();
        try {
            github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred initializing GitHub. How did this happen?");
            return;
        }
        GHRepository repo;
        try {
            repo = github.getRepository(repoName);
        } catch (IOException e) {
            commandEvent.reply("Invalid repository name. Please make sure this repository exists!");
            return;
        }
        EmbedBuilder e = new EmbedBuilder();
        try {
            e.setTitle("GitHub Repository Info for " + repo.getFullName(), "https://github.com/" + repo.getFullName());
            e.setDescription(repo.getDescription());
            if(repo.getHomepage() != null)
                e.addField("URL", String.valueOf(repo.getHomepage()), true);
            if(repo.getLicense() != null)
                e.addField("License", repo.getLicense().getName(), true);
            e.addField("Open Issues/PRs", String.valueOf(repo.getOpenIssueCount()), true);
            e.addField("Stars", String.valueOf(repo.getStargazersCount()), true);

            int k = 1024;
            String[] measure = new String[]{"B", "KB", "MB", "GB", "TB"};
            int bytes = repo.getSize();
            double i;
            if(bytes == 0) {
                i = 0;
            } else {
                i = Math.floor(Math.log(bytes) / Math.log(k));
            }
            DecimalFormat df = new DecimalFormat("#.##");

            e.addField("Size", df.format(bytes / Math.pow(k, i)) + " " + measure[(int) i + 1], true);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        commandEvent.reply(e.build());
    }
}
