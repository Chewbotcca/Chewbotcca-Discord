/*
 * Copyright (C) 2020 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pw.chew.chewbotcca.commands.github;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import pw.chew.chewbotcca.util.PropertiesManager;

import java.io.IOException;
import java.text.DecimalFormat;

// %^ghrepo command
public class GHRepoCommand extends Command {

    public GHRepoCommand() {
        this.name = "ghrepo";
        this.aliases = new String[]{"githubrepo", "ghrepository"};
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get the repo
        String repoName = commandEvent.getArgs();
        if(!repoName.contains("/")) {
            commandEvent.reply("Make sure your input contains a UserOrOrg/RepositoryName (e.g. Chewbotcca/Discord).");
        }
        // Initialize GitHub
        GitHub github;
        commandEvent.getChannel().sendTyping().queue();
        try {
            github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();
        } catch (IOException e) {
            e.printStackTrace();
            commandEvent.reply("Error occurred initializing GitHub. How did this happen?");
            return;
        }
        // Find the repo
        GHRepository repo;
        try {
            repo = github.getRepository(repoName);
        } catch (IOException e) {
            commandEvent.reply("Invalid repository name. Please make sure this repository exists!");
            return;
        }
        // Generate an Embed
        EmbedBuilder e = new EmbedBuilder();
        try {
            // Set data based on repo info
            e.setTitle("GitHub Repository Info for " + repo.getFullName(), "https://github.com/" + repo.getFullName());
            e.setDescription(repo.getDescription());
            if(repo.getHomepage() != null)
                e.addField("URL", String.valueOf(repo.getHomepage()), true);
            if(repo.getLicense() != null)
                e.addField("License", repo.getLicense().getName(), true);
            e.addField("Open Issues/PRs", String.valueOf(repo.getOpenIssueCount()), true);
            e.addField("Stars", String.valueOf(repo.getStargazersCount()), true);

            // Find size of repo and list it
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
