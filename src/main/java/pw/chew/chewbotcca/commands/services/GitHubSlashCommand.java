/*
 * Copyright (C) 2022 Chewbotcca
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
package pw.chew.chewbotcca.commands.services;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.memerator.api.errors.NotFound;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.PagedIterator;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.objects.UserProfile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static pw.chew.chewbotcca.commands.services.github.GHIssueCommand.issueBuilder;
import static pw.chew.chewbotcca.commands.services.github.GHRepoCommand.gatherRepoData;
import static pw.chew.chewbotcca.commands.services.github.GHUserCommand.gatherUser;

/**
 * This command is exclusively to allow /github [subcommand]
 * Normal command handling is done in the respective GitHub package
 */
public class GitHubSlashCommand extends SlashCommand {

    public GitHubSlashCommand() {
        this.name = "github";
        this.help = "Gathers some info from GitHub";
        this.children = new SlashCommand[]{
            new GitHubIssueSubCommand(),
            new GitHubRepoSubCommand(),
            new GitHubUserSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // This is ignored as we delegate everything to sub-commands
    }

    public static class GitHubIssueSubCommand extends SlashCommand {

        public GitHubIssueSubCommand() {
            this.name = "issue";
            this.help = "Gathers an issue from a repository on GitHub";
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
                issue = parseMessage(repo, term);
            } catch (IOException e) {
                event.reply("Invalid issue number or an invalid repository was provided. Please ensure the issue exists and the repository is public.")
                    .setEphemeral(true).queue();
                return;
            } catch (IllegalArgumentException e) {
                event.reply(e.getMessage()).setEphemeral(true).queue();
                return;
            }
            if(issue == null)
                return;

            // Create and send off an issue embed
            event.replyEmbeds(issueBuilder(issue).build()).queue();
        }

        public GHIssue parseMessage(String repo, String term) throws IOException {
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
    }

    public static class GitHubRepoSubCommand extends SlashCommand {

        public GitHubRepoSubCommand() {
            this.name = "repository";
            this.help = "Gathers a repository from GitHub";
            this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "repo", "The repo to look up (format: UserOrOrg/Repo)").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            // Get the repo
            String repoName = event.optString("repo", "");
            if (!repoName.contains("/")) {
                event.reply("Make sure your input contains a UserOrOrg/RepositoryName (e.g. Chewbotcca/Discord).").setEphemeral(true).queue();
                return;
            }
            // Find the repo
            GHRepository repo;
            try {
                repo = Memory.getGithub().getRepository(repoName);
            } catch (IOException e) {
                event.reply("Invalid repository name. Please make sure this repository exists!").setEphemeral(true).queue();
                return;
            }
            event.replyEmbeds(gatherRepoData(repo)).queue();
        }
    }

    public static class GitHubUserSubCommand extends SlashCommand {

        public GitHubUserSubCommand() {
            this.name = "user";
            this.help = "Gathers a user from GitHub";
            this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "username", "The user to look up (default: yours if set)")
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            // Get the input
            String username = event.optString("username", "");
            if (username.isBlank()) {
                UserProfile profile = UserProfile.getProfile(event.getUser().getId());
                if (profile.getGitHub() != null) {
                    username = profile.getGitHub();
                } else {
                    event.reply("You don't have a GitHub username set on your profile. " +
                            "Please specify a user with `/github user [name]` or set your username with `/profile set github [yourname]`!")
                        .setEphemeral(true)
                        .queue();
                    return;
                }
            }

            GHUser user;
            try {
                user = Memory.getGithub().getUser(username);
            } catch (IOException e) {
                event.reply("Invalid username. Please make sure this user exists!").setEphemeral(true).queue();
                return;
            }
            event.replyEmbeds(gatherUser(user)).queue();
        }
    }
}
