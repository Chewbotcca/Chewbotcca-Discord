/*
 * Copyright (C) 2021 Chewbotcca
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
package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.jdachewtils.command.OptionHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// %^rubygem command
public class RubyGemsCommand extends SlashCommand {

    public RubyGemsCommand() {
        this.name = "rubygem";
        this.help = "Searches for and returns some basic info about a specified ruby gem";
        this.aliases = new String[]{"gem", "rgem", "rubyg", "gems"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "gem", "The gem to lookup").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            event.replyEmbeds(gatherGemData(OptionHelper.optString(event, "gem", ""))).queue();
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(ResponseHelper.generateFailureEmbed(null, e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        // Start typing
        event.getChannel().sendTyping().queue();

        try {
            event.reply(gatherGemData(event.getArgs().trim()));
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    /**
     * Gathers info about a gem from rubygems.org and returns it as an embed
     *
     * @param name The Gem name
     * @return The information
     */
    private MessageEmbed gatherGemData(String name) {
        JSONObject data;
        // Get gem if it exists
        try {
            data = new JSONObject(RestClient.get("https://rubygems.org/api/v1/gems/" + name + ".json"));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid ruby gem!");
        }
        // Get ranking from best gems
        int rank = -1;
        try {
            rank = new JSONArray(RestClient.get("https://bestgems.org/api/v1/gems/" + name + "/total_ranking.json")).getJSONObject(0).getInt("total_ranking");
        } catch (JSONException ignored) {
        }

        // Get info
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle(data.getString("name") + " (" + data.getString("version") + ")", data.getString("project_uri"));
        e.setAuthor("RubyGem Information", null, "https://cdn.discordapp.com/emojis/232899886419410945.png");
        e.setDescription(data.getString("info"));

        e.addField("Authors", data.getString("authors"), true);

        e.addField("Downloads", "For Version: " + NumberFormat.getNumberInstance(Locale.US).format(data.getInt("version_downloads")) + "\n" +
            "Total: " + NumberFormat.getNumberInstance(Locale.US).format(data.getInt("downloads")), true);

        // Add licenses
        if (!data.isNull("licenses") && !data.getJSONArray("licenses").isEmpty()) {
            e.addField("License", data.getJSONArray("licenses").getString(0), true);
        }

        // Add the rank if there is one
        if (rank > -1) {
            e.addField("Rank", "#" + NumberFormat.getNumberInstance(Locale.US).format(rank), true);
        }

        // Get links
        List<String> links = new ArrayList<>();

        // These are the URLs we want
        String[] uris = new String[]{"documentation", "homepage", "wiki", "mailing_list", "source_code", "bug_tracker", "changelog"};

        // Go through and add them to our link list
        for (String uri : uris) {
            String path = uri + "_uri";
            if (data.isNull(path)) continue;

            links.add(String.format("[%s](%s)", MiscUtil.capitalize(uri), data.getString(path)));
        }

        // Spit em out!
        e.addField("Links", String.join(", ", links), true);

        return e.build();
    }
}
