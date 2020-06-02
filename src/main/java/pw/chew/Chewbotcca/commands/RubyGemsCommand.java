package pw.chew.Chewbotcca.commands.cat;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

public class RubyGemsCommand extends Command {

    public RubyGemsCommand() {
        this.name = "rubygem";
        this.aliases = new String[]{"gem", "rgem", "rubyg", "gems"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        JSONObject data;
        try {
            data = new JSONObject(RestClient.get("https://rubygems.org/api/v1/gems/" + event.getArgs() + ".json"));
        } catch (JSONException e) {
            event.reply("Invalid ruby gem!");
            return;
        }
        int rank = new JSONArray(RestClient.get("http://bestgems.org/api/v1/gems/" + event.getArgs() + "/total_ranking.json")).getJSONObject(0).getInt("total_ranking");

        EmbedBuilder e = new EmbedBuilder();
                e.setTitle("RubyGem Information");
                e.setAuthor(data.getString("name") + " (" + data.getString("version") + ")", null, "https://cdn.discordapp.com/emojis/232899886419410945.png");
                e.setDescription(data.getString("info"));

        e.addField("Authors", data.getString("authors"), true);

        e.addField("Downloads", "For Version: " + data.getInt("version_downloads") + "\n" +
                "Total: " + data.getInt("downloads"), true);

        e.addField("License", data.getJSONArray("licenses").getString(0), true);
        e.addField("Rank", "#" + rank, true);

        StringBuilder links = new StringBuilder();
        links.append("[Project](").append(data.getString("project_uri")).append(")\n");
        links.append("[Gem](").append(data.getString("gem_uri")).append(")\n");
        links.append("[Documentation](").append(data.getString("documentation_uri")).append(")\n");

        if(!data.isNull("homepage_uri"))
            links.append("[Homepage](").append(data.getString("homepage_uri")).append(")\n");
        if(!data.isNull("wiki_uri"))
            links.append("[Wiki](").append(data.getString("wiki_uri")).append(")\n");
        if(!data.isNull("mailing_list_uri"))
            links.append("[Mailing List](").append(data.getString("mailing_list_uri")).append(")\n");
        if(!data.isNull("source_code_uri"))
            links.append("[Source Code](").append(data.getString("source_code_uri")).append(")\n");
        if(!data.isNull("bug_tracker_uri"))
            links.append("[Bug Tracker](").append(data.getString("bug_tracker_uri")).append(")\n");
        if(!data.isNull("changelog_uri"))
            links.append("[Changelog](").append(data.getString("changelog_uri")).append(")");

        e.addField("Links", links.toString(), true);

        event.reply(e.build());
    }
}
