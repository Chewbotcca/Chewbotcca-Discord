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
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;

// %^ghuser command
public class GHUserCommand extends Command {
    final GitHub github;

    public GHUserCommand(GitHub github) {
        this.name = "ghuser";
        this.aliases = new String[]{"ghorg"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.github = github;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get the input
        String username = commandEvent.getArgs();
        commandEvent.getChannel().sendTyping().queue();
        // Find the GitHub user and notify if errored
        GHUser user;
        try {
            user = github.getUser(username);
        } catch (IOException e) {
            commandEvent.reply("Invalid username. Please make sure this user exists!");
            return;
        }
        // Generate Embed
        EmbedBuilder e = new EmbedBuilder();
        try {
            // Set repo count and thumbnail
            e.setThumbnail(user.getAvatarUrl());
            e.addField("Repositories", String.valueOf(user.getPublicRepoCount()), true);
            // If it's actually an org, handle it differently
            boolean isOrg = user.getType().equals("Organization");
            if(!isOrg) {
                // If it's not an org, it's a profile!
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
                // If it is an org, list other stuff instead
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
