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
import me.memerator.api.client.errors.NotFound;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.PagedIterator;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.CommandContext;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h2><code>/github issue</code> Sub-Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/github/#issue-subcommand">Docs</a>
 */
public class GitHubIssueSubCommand extends SlashCommand {

    public GitHubIssueSubCommand() {
        this.name = "issue";
        this.help = "Gathers an issue from a repository on GitHub";
        this.contexts = CommandContext.GLOBAL;
        this.options = Arrays.asList(
            new OptionData(OptionType.STRING, "repo", "The repo to look up (format: UserOrOrg/Repo)").setRequired(true),
            new OptionData(OptionType.STRING, "issue", "The issue. Provide a number or search term").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        GHIssue issue;

        String repo = event.optString("repo", "");
        String term = event.optString("issue", "");

        try {
            issue = retrieveIssue(repo, term);
        } catch (IOException | GHException e) {
            event.reply("Invalid issue number or an invalid repository was provided. Please ensure the issue exists and the repository is public.")
                .setEphemeral(true).queue();
            return;
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
            return;
        }

        if (issue == null)
            return;

        // Create and send off an issue embed
        event.replyEmbeds(issueBuilder(issue).build()).queue();
    }

    public GHIssue retrieveIssue(String repo, String term) throws IOException, GHException {
        // Store GHIssue for later
        GHIssue issue;
        // Make sure the issue number is valid
        int issueNum;
        try {
            issueNum = Integer.parseInt(term);
            issue = Memory.getGithub().getRepository(repo).getIssue(issueNum);
        } catch (NumberFormatException e) {
            PagedIterator<GHIssue> iterator = Memory.getGithub().searchIssues()
                .q(term + " repo:" + repo)
                .order(GHDirection.DESC)
                .list()
                ._iterator(1);
            if (iterator.hasNext()) {
                issue = iterator.next();
            } else {
                throw new NotFound("Could not find specified issue in repo `" + repo + "`!");
            }
            int num = issue.getNumber();
            issue = Memory.getGithub().getRepository(repo).getIssue(num);
            return issue;
        }
        return issue;
    }

    /**
     * Method to make an issue embed
     *
     * @param issue the issue to parse
     * @return an EmbedBuilder with all the data
     */
    public static EmbedBuilder issueBuilder(GHIssue issue) {
        EmbedBuilder e = new EmbedBuilder();
        // Set the title and body to the issue title and body
        e.setTitle(issue.getTitle(), String.valueOf(issue.getHtmlUrl()));
        if (issue.getBody() != null) {
            if (issue.getBody().length() > 400) {
                e.setDescription(issue.getBody().substring(0, 399) + "...");
            } else {
                e.setDescription(issue.getBody());
            }
        }
        // Find the state
        boolean open = issue.getState() == GHIssueState.OPEN;
        boolean merged = false;
        boolean draft = false;
        if (issue.isPullRequest()) {
            // If it's a pull request, treat it as such
            e.setAuthor("Information for Pull Request #" + issue.getNumber() + " in " + issue.getRepository().getFullName());
            try {
                GHPullRequest pull = issue.getRepository().getPullRequest(issue.getNumber());
                merged = pull.isMerged();
                draft = pull.isDraft();
            } catch (IOException ioException) {
                // If an IOException ever occurs, we're prepared.
                ioException.printStackTrace();
                e.setTitle("Error!");
                e.setDescription("Error occurred initializing Pull request. How did this happen?");
                return e;
            }
        } else {
            // Otherwise it's just an issue, do nothing special.
            e.setAuthor("Information for Issue #" + issue.getNumber() + " in " + issue.getRepository().getFullName());
        }
        // Set status and color based on issue/pull request status
        if (merged) {
            e.setColor(Color.decode("#6f42c1"));
            e.addField("Status", "Merged", true);
        } else if (!open) {
            e.setColor(Color.decode("#cb2431"));
            e.addField("Status", "Closed", true);
        } else if (draft) {
            e.setColor(Color.decode("#ffffff"));
            e.addField("Status", "Draft", true);
        } else {
            e.setColor(Color.decode("#2cbe4e"));
            e.addField("Status", "Open", true);
        }

        // Try and find the author and set the author field accordingly.
        try {
            GHUser author = issue.getUser();
            if (author.getName() != null)
                e.addField("Author", "[%s (%s)](https://github.com/%s)".formatted(author.getLogin(), author.getName(), author.getLogin()), true);
            else
                e.addField("Author", "[%s](https://github.com/%s)".formatted(author.getLogin(), author.getLogin()), true);
        } catch (IOException ioException) {
            e.addField("Author", "Unknown Author", true);
        }

        // Add labels and assignees as well
        try {
            e.setFooter("Opened");
            e.setTimestamp(issue.getCreatedAt().toInstant());
            List<CharSequence> labels = new ArrayList<>();
            for (GHLabel label : issue.getLabels()) {
                labels.add(label.getName());
            }
            if (!labels.isEmpty())
                e.addField("Labels", String.join(", ", labels), true);
            List<CharSequence> assignees = new ArrayList<>();
            for (GHUser assignee : issue.getAssignees()) {
                assignees.add(assignee.getLogin());
            }
            if (!assignees.isEmpty())
                e.addField("Assignees", String.join(", ", assignees), true);
        } catch (IOException ignored) {
        }

        return e;
    }
}
