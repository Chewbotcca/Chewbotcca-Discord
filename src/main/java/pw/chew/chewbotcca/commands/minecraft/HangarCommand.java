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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class HangarCommand extends SlashCommand {
    private static final String baseUrl = "https://hangar.papermc.io/";
    private static final String apiUrl = baseUrl + "api/v1/";

    public HangarCommand() {
        this.name = "hangar";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "plugin", "The plugin to search for").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String plugin = event.optString("plugin", "");

        try {
            event.replyEmbeds(buildPluginEmbed(plugin)).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    /**
     * Searches Hangar for a plugin and returns an embed with the plugin's information.
     *
     * @param args The plugin name to search for.
     * @return The embed with the plugin's information.
     */
    private MessageEmbed buildPluginEmbed(String args) {
        args = URLEncoder.encode(args, StandardCharsets.UTF_8);
        JSONObject response = new JSONObject(RestClient.get(apiUrl + "projects?q=" + args + "&limit=1"));
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Hangar Plugin Repository Search Results", baseUrl);
        if (response.getJSONObject("pagination").getInt("count") == 0) {
            throw new IllegalArgumentException("No results found!");
        }

        JSONObject plugin = response.getJSONArray("result").getJSONObject(0);
        JSONObject namespace = plugin.getJSONObject("namespace");
        String projectURL = baseUrl + namespace.getString("owner") + "/" + namespace.getString("slug");

        embed.setTitle(plugin.getString("name"), projectURL);
        embed.setDescription(plugin.getString("description"));
        embed.setThumbnail(plugin.getString("avatarUrl"));

        JSONObject stats = plugin.getJSONObject("stats");
        embed.addField("Views", String.valueOf(stats.getInt("views")), true);
        embed.addField("Downloads", String.valueOf(stats.getInt("downloads")), true);
        embed.addField("Stars", String.valueOf(stats.getInt("stars")), true);
        embed.addField("Watchers", String.valueOf(stats.getInt("watchers")), true);

        return embed.build();
    }
}
