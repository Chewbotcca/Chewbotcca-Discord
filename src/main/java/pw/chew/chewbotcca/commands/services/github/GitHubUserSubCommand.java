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
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.objects.UserProfile;

import java.io.IOException;
import java.util.Collections;

/**
 * <h2><code>/github user</code> Sub-Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/github/#user-subcommand">Docs</a>
 */
public class GitHubUserSubCommand extends SlashCommand {
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

    public static MessageEmbed gatherUser(GHUser user) {
        // Generate Embed
        EmbedBuilder e = new EmbedBuilder();
        try {
            // Set repo count and thumbnail
            e.setThumbnail(user.getAvatarUrl());
            e.addField("Repositories", String.valueOf(user.getPublicRepoCount()), true);
            // If it's actually an org, handle it differently
            boolean isOrg = user.getType().equals("Organization");
            if (!isOrg) {
                // If it's not an org, it's a profile!
                e.setTitle("GitHub profile for " + user.getLogin(), "https://github.com/" + user.getLogin());
                e.setDescription(user.getBio());
                e.addField("Followers", String.valueOf(user.getFollowersCount()), true);
                e.addField("Following", String.valueOf(user.getFollowingCount()), true);
                if (user.getCompany() != null)
                    e.addField("Company", user.getCompany(), true);
                if (!user.getBlog().isBlank())
                    e.addField("Website", user.getBlog(), true);
                if (user.getTwitterUsername() != null)
                    e.addField("Twitter", "[@" + user.getTwitterUsername() + "](https://twitter.com/" + user.getTwitterUsername() + ")", true);
            } else {
                // If it is an org, list other stuff instead
                GHOrganization org = Memory.getGithub().getOrganization(user.getLogin());
                e.setTitle("GitHub profile for Organization " + user.getLogin(), "https://github.com/" + user.getLogin());
                e.addField("Public Members", String.valueOf(org.listMembers().toArray().length), true);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return e.build();
    }
}