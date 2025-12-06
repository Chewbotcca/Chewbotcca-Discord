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

package pw.chew.chewbotcca.unfurls;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;
import pw.chew.chewbotcca.commands.services.github.GitHubIssueSubCommand;
import pw.chew.chewbotcca.objects.Memory;

import java.io.IOException;

public class GitHubUnfurler implements GenericUnfurler {
    @Override
    public boolean checkLink(String link) {
        return link.contains("github.com") && (link.contains("/issues") || link.contains("/pull"));
    }

    @Override
    public @Nullable MessageEmbed unfurl(String link) {
        // Example: https://github.com/Chewbotcca/Discord/issues/1
        // => "https:" "" "github.com" "Chewbotcca" "Discord" "issues" "1"
        String[] url = link.split("/");
        String repo = url[3] + "/" + url[4];
        // Get the issue num
        int issue = Integer.parseInt(url[6]);

        // Initialize GitHub and the response
        GitHub github = Memory.getGithub();
        GHIssue ghIssue;
        try {
            ghIssue = github.getRepository(repo).getIssue(issue);
            return GitHubIssueSubCommand.issueBuilder(ghIssue).build();
        } catch (IOException e) {
            return null;
        }
    }
}
