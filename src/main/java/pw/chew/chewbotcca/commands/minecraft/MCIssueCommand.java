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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MCIssueCommand extends Command {
    final static ArrayList<String> describedIds = new ArrayList<>();
    private static final HashMap<String, List<String>> projects = new HashMap<>();

    public MCIssueCommand() {
        this.name = "mcissue";
        this.aliases = new String[]{"mojira", "mcbug"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;

        projects.put(
                "https://bugs.mojang.com/rest/api/latest/issue/",
                Arrays.asList("BDS", "MCPE", "MCAPI", "MCCE", "MCD", "MCL", "REALMS", "MCE", "MC", "WEB")
        );
        projects.put(
                "https://hub.spigotmc.org/jira/rest/api/latest/issue/",
                Arrays.asList("BUILDTOOLS", "SPIGOT", "PLUG")
        );
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().length() < 4) {
            event.reply("Please specify a project AND issue, for example, 'WEB-2303'");
            return;
        }

        String issue;

        if(event.getArgs().contains("http")) {
            String[] breakdown = event.getArgs().split("/");
            issue = breakdown[breakdown.length - 1];
        } else {
            issue = event.getArgs();
        }

        String apiUrl = getApiUrl(issue.split("-")[0]);
        if(apiUrl == null) {
            event.reply("Invalid Project Specified. Supported projects are any from Mojang Studios or SpigotMC Jira");
            return;
        }

        event.getChannel().sendTyping().queue();

        JSONObject data = new JSONObject(RestClient.get(apiUrl + issue));

        event.reply(generateEmbed(data, issue, apiUrl).build());
    }

    public static EmbedBuilder generateEmbed(JSONObject data, String issue, String apiUrl) {
        EmbedBuilder embed = new EmbedBuilder();
        if(data.has("errorMessages")) {
            embed.setTitle("Error!");
            embed.setDescription(data.getJSONArray("errorMessages").getString(0));
            return embed;
        }

        data = data.getJSONObject("fields");

        embed.setAuthor("Information for " + issue.toUpperCase(), apiUrl.replace("rest/api/latest/issue", "browse") + issue);
        embed.setTitle(data.getString("summary"));
        if (data.getString("description").length() > 500)
            embed.setDescription(data.getString("description").substring(0, 500));
        else
            embed.setDescription(data.getString("description"));
        embed.addField("Type", data.getJSONObject("issuetype").getString("name"), true);
        try {
            embed.addField("Resolution", data.getJSONObject("resolution").getString("name"), true);
        } catch (JSONException exception) {
            embed.addField("Resolution", "None", true);
        }
        embed.addField("Status", data.getJSONObject("status").getString("name"), true);
        try {
            embed.addField("Priority", data.getJSONObject("priority").getString("name"), true);
        } catch (JSONException exception) {
            embed.addField("Priority", "None", true);
        }
        if(!data.isNull("assignee"))
            embed.addField("Assignee", data.getJSONObject("assignee").getString("displayName"), true);
        embed.addField("Reporter", data.getJSONObject("reporter").getString("displayName"), true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.of("America/Chicago"));
        embed.setTimestamp(formatter.parse(data.getString("created")));

        return embed;
    }

    public static String getApiUrl(String target) {
        String project = target.toUpperCase();
        for(String key : projects.keySet()) {
            List<String> list = projects.get(key);
            if(list.contains(project))
                return key;
        }
        return null;
    }

    /*
    Methods for MagReact
     */

    public static boolean didDescribe(String id) {
        return describedIds.contains(id);
    }

    public static void described(String id) {
        describedIds.add(id);
    }
}
