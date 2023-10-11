/*
 * Copyright (C) 2023 Chewbotcca
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
import org.json.JSONObject;
import pw.chew.chewbotcca.commands.minecraft.MCIssueSubCommand;
import pw.chew.chewbotcca.util.RestClient;

public class MCIssueUnfurler implements GenericUnfurler {
    @Override
    public boolean checkLink(String link) {
        return link.contains("bugs.mojang.com") || link.contains("hub.spigotmc.org/jira");
    }

    @Override
    public @Nullable MessageEmbed unfurl(String link) {
        // Get PROJECT-NUM from URL
        String[] url = link.split("/");
        String issue = url[url.length - 1];

        // Ensure we actually track this
        String apiUrl = MCIssueSubCommand.getApiUrl(issue.split("-")[0]);
        if (apiUrl == null)
            return null;
        // Get response
        JSONObject data = new JSONObject(RestClient.get(apiUrl + issue));
        // Initialize GitHub and the response
        return MCIssueSubCommand.generateEmbed(data, issue, apiUrl).build();
    }
}
