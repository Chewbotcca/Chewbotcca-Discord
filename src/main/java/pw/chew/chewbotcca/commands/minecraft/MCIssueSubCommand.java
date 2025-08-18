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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MCIssueSubCommand extends SlashCommand {
    private static final HashMap<String, List<String>> projects = new HashMap<>();

    public MCIssueSubCommand() {
        this.name = "issue";
        this.help = "Searches Mojira or Spigot MC Jira for a specified issue. Requires PROJ-NUM or link";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "issue", "The issue to lookup, e.g. WEB-2303").setRequired(true)
        );

        projects.put(
            "https://report.bugs.mojang.com/rest/servicedeskapi/request/",
            Arrays.asList("BDS", "MCPE", "MCAPI", "MCCE", "MCD", "MCL", "REALMS", "MCE", "MC", "WEB")
        );
        projects.put(
            "https://hub.spigotmc.org/jira/rest/api/latest/issue/",
            Arrays.asList("BUILDTOOLS", "SPIGOT", "PLUG")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String issue = event.optString("issue", "");

        if (issue.length() < 4) {
            event.reply("Please specify a project AND issue, for example, `WEB-2303`").setEphemeral(true).queue();
            return;
        }

        String apiUrl = getApiUrl(issue.split("-")[0]);
        if (apiUrl == null) {
            event.reply("Invalid Project Specified. Supported projects are any from Mojang Studios or SpigotMC Jira").setEphemeral(true).queue();
            return;
        }

        JSONObject data;
        if (apiUrl.contains("mojang.com")) {
            JSONObject payload = new JSONObject()
                .put("advanced", true)
                .put("search", "key = " + issue)
                .put("project", issue.split("-")[0]);

            JSONObject res = RestClient.post("https://bugs.mojang.com/api/jql-search-post", payload).asJSONObject();

            if (res.optInt("statusCode", 200) == 500) {
                event.reply("Error looking up issue. Try again later!").setEphemeral(true).queue();
                return;
            }

            JSONArray issues = res.getJSONArray("issues");
            if (issues.isEmpty()) {
                event.reply("Issue not found!").setEphemeral(true).queue();
                return;
            } else {
                data = issues.getJSONObject(0);
            }
        } else {
            data = RestClient.get(apiUrl + issue).asJSONObject();
        }

        try {
            event.replyEmbeds(generateEmbed(data, issue, apiUrl).build()).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    public static EmbedBuilder generateEmbed(JSONObject data, String issue, String apiUrl) {
        EmbedBuilder embed = new EmbedBuilder();
        if(data.has("errorMessages")) {
            throw new IllegalArgumentException(data.getJSONArray("errorMessages").getString(0));
        }

        JSONObject renderedField = data.optJSONObject("renderedFields", data.getJSONObject("fields"));
        data = data.getJSONObject("fields");

        embed.setAuthor("Information for " + issue.toUpperCase(), apiUrl.replace("rest/api/latest/issue", "browse") + issue);
        embed.setTitle(data.getString("summary"));
        embed.setDescription(FlexmarkHtmlConverter.builder().build().convert(
            renderedField.getString("description")
                // replace <a name="stuff"> with just <a>
                .replaceAll("(?i)<a\\b[^>]*>", "<a>")
                // replace <tt> with <code>
                .replaceAll("tt>", "code>")
                // bye bye <span> tags, their content is fine
                .replaceAll("(?i)</span\\s*>", "")
                .replaceAll("(?i)<span\\b[^>]*>", "")
        ));
        embed.addField("Type", data.getJSONObject("issuetype").getString("name"), true);
        embed.addField("Resolution", data.isNull("resolution") ? "Unresolved": data.getJSONObject("resolution").optString("name"), true);
        embed.addField("Status", data.getJSONObject("status").getString("name"), true);
        try {
            embed.addField("Priority", data.getJSONObject("priority").getString("name"), true);
        } catch (JSONException exception) {
            embed.addField("Priority", "None", true);
        }
        if(!data.isNull("assignee"))
            embed.addField("Assignee", data.getJSONObject("assignee").getString("displayName"), true);
        if(!data.isNull("assignee"))
            embed.addField("Reporter", data.getJSONObject("reporter").getString("displayName"), true);

        OffsetDateTime formatted = MiscUtil.dateParser(data.getString("created"), "uuuu-MM-dd'T'HH:mm:ss.SSSZ");
        embed.setTimestamp(formatted);

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
}
