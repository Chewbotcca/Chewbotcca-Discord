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

        embed.setAuthor("Information for " + issue, apiUrl.replace("rest/api/latest/issue", "browse") + issue);
        embed.setTitle(data.getString("summary"));
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
        for(String key : projects.keySet()) {
            List<String> list = projects.get(key);
            if(list.contains(target))
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
