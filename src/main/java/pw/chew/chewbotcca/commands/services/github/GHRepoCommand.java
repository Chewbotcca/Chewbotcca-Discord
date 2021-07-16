/*
 * Copyright (C) 2021 Chewbotcca
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
package pw.chew.chewbotcca.commands.services.github;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.kohsuke.github.GHRepository;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.MiscUtil;

import java.io.IOException;

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
        if (!repoName.contains("/")) {
            commandEvent.reply("Make sure your input contains a UserOrOrg/RepositoryName (e.g. Chewbotcca/Discord).");
            return;
        }
        commandEvent.getChannel().sendTyping().queue();
        // Find the repo
        GHRepository repo;
        try {
            repo = Memory.getGithub().getRepository(repoName);
        } catch (IOException e) {
            commandEvent.reply("Invalid repository name. Please make sure this repository exists!");
            return;
        }
        commandEvent.reply(gatherRepoData(repo));
    }

    public static MessageEmbed gatherRepoData(GHRepository repo) {
        // Generate an Embed
        EmbedBuilder e = new EmbedBuilder();
        try {
            // Set data based on repo info
            e.setTitle("GitHub Repository Info for " + repo.getFullName(), "https://github.com/" + repo.getFullName());
            e.setDescription(repo.getDescription());
            if (repo.getHomepage() != null)
                e.addField("URL", String.valueOf(repo.getHomepage()), true);
            if (repo.getLicense() != null)
                e.addField("License", repo.getLicense().getName(), true);
            e.addField("Open Issues/PRs", String.valueOf(repo.getOpenIssueCount()), true);
            e.addField("Stars", String.valueOf(repo.getStargazersCount()), true);

            e.addField("Size", MiscUtil.bytesToFriendly(repo.getSize()), true);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return e.build();
    }
}
