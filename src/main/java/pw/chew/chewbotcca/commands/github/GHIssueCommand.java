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
import org.kohsuke.github.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// %^ghissue command
public class GHIssueCommand extends Command {
    final GitHub github;
    final static ArrayList<String> describedIds = new ArrayList<>();

    public GHIssueCommand(GitHub github) {
        this.name = "ghissue";
        this.aliases = new String[]{"ghpull"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.github = github;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Needs a repo and an issue/pr number. If there's not both, let them know.
        String[] args = commandEvent.getArgs().split(" ");
        if(args.length < 2) {
            commandEvent.reply("Please provide a Repository and an Issue Number!");
            return;
        }
        String repo = args[0];
        // Make sure the issue number is valid
        int issueNum;
        try {
            issueNum = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            commandEvent.reply("Invalid number provided for issue number. Must be a number. Make sure you're doing Repo then Number!");
            return;
        }
        commandEvent.getChannel().sendTyping().queue();
        // Find the GitHub issue
        GHIssue issue;
        try {
            issue = github.getRepository(repo).getIssue(issueNum);
        } catch (IOException e) {
            commandEvent.reply("Invalid issue number or an invalid repository was provided. Please ensure the issue exists and the repository is public.");
            return;
        }

        // Create and send off an issue embed
        commandEvent.reply(issueBuilder(issue, repo, github, issueNum).build());
    }


    /**
     * Method to make an issue embed
     * @param issue the issue to parse
     * @param repo the repo the issue is on
     * @param github a github object
     * @param issueNum the issue to grab
     * @return an EmbedBuilder with all the data
     */
    public EmbedBuilder issueBuilder(GHIssue issue, String repo, GitHub github, int issueNum) {
        EmbedBuilder e = new EmbedBuilder();
        // Set the title and body to the issue title and body
        e.setTitle(issue.getTitle());
        if(issue.getBody() != null) {
            if (issue.getBody().length() > 200) {
                e.setDescription(issue.getBody().substring(0, 199) + "...");
            } else {
                e.setDescription(issue.getBody());
            }
        }
        // Find the state
        boolean open = issue.getState() == GHIssueState.OPEN;
        boolean merged = false;
        if(issue.isPullRequest()) {
            // If it's a pull request, treat it as such
            e.setAuthor("Information for Pull Request #" + issueNum + " in " + repo, String.valueOf(issue.getUrl()));
            try {
                GHPullRequest pull = github.getRepository(repo).getPullRequest(issueNum);
                merged = pull.isMerged();
            } catch (IOException ioException) {
                // If an IOException ever occurs, we're prepared.
                ioException.printStackTrace();
                e.setTitle("Error!");
                e.setDescription("Error occurred initializing Pull request. How did this happen?");
                return e;
            }
        } else {
            // Otherwise it's just an issue, do nothing special.
            e.setAuthor("Information for Issue #" + issueNum + " in " + repo, String.valueOf(issue.getUrl()));
        }
        // Set status and color based on issue status
        if(merged) {
            e.setColor(Color.decode("#6f42c1"));
            e.addField("Status", "Merged", true);
        } else if(open) {
            e.setColor(Color.decode("#2cbe4e"));
            e.addField("Status", "Open", true);
        } else {
            e.setColor(Color.decode("#cb2431"));
            e.addField("Status", "Closed", true);
        }
        // Try and find the author and set the author field accordingly.
        try {
            GHUser author = issue.getUser();
            if(author.getName() != null)
                e.addField("Author", "[" + author.getLogin() + " (" + author.getName() + ")" + "](https://github.com/" + author.getLogin() + ")", true);
            else
                e.addField("Author", "[" + author.getLogin() + "](https://github.com/" + author.getLogin() + ")", true);
        } catch (IOException ioException) {
            e.addField("Author", "Unknown Author", true);
        }
        // Add labels and assignees as well
        try {
            e.setFooter("Opened");
            e.setTimestamp(issue.getCreatedAt().toInstant());
            List<CharSequence> labels = new ArrayList<>();
            for(GHLabel label : issue.getLabels()) {
                labels.add(label.getName());
            }
            if(labels.size() > 0)
                e.addField("Labels", String.join(", ", labels), true);
            List<CharSequence> assignees = new ArrayList<>();
            for(GHUser assignee : issue.getAssignees()) {
                assignees.add(assignee.getLogin());
            }
            if(assignees.size() > 0)
                e.addField("Assignees", String.join(", ", assignees), true);
        } catch (IOException ignored) { }
        return e;
    }

    // Method used in MagReact to deduce if it's been handled already
    public static boolean didDescribe(String id) {
        return describedIds.contains(id);
    }

    // Mark a message as handled for MagReact
    public static void described(String id) {
        describedIds.add(id);
    }
}
