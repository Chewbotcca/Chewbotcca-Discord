/*
 * Copyright (C) 2025 Chewbotcca
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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.kohsuke.github.GHRepository;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.MiscUtil;

import java.io.IOException;
import java.util.Arrays;

/**
 * <h2><code>/github repository</code> Sub-Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/github/#repository-subcommand">Docs</a>
 */
public class GitHubRepoSubCommand extends SlashCommand {

    public GitHubRepoSubCommand() {
        this.name = "repository";
        this.help = "Gathers a repository from GitHub";
        this.contexts = CommandContext.GLOBAL;
        this.options = Arrays.asList(
            new OptionData(OptionType.STRING, "user", "The user or organization to find", true),
            new OptionData(OptionType.STRING, "repo", "The repo to look up in this user/org", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Get the repo
        String userName = event.optString("user", "");
        String repoName = event.optString("repo", "");

        // Find the repo
        GHRepository repo;
        try {
            repo = Memory.getGithub().getRepository(userName + "/" + repoName);
        } catch (IOException e) {
            event.reply("Invalid repository name. Please make sure this repository exists!").setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(gatherRepoData(repo)).queue();
    }

    public static MessageEmbed gatherRepoData(GHRepository repo) {
        // Generate an Embed
        EmbedBuilder e = new EmbedBuilder();
        try {
            // Set data based on repo info
            e.setTitle("GitHub Repository Info for " + repo.getFullName(), "https://github.com/" + repo.getFullName());
            e.setDescription(repo.getDescription());
            if (repo.getHomepage() != null)
                e.addField("URL", repo.getHomepage(), true);
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
