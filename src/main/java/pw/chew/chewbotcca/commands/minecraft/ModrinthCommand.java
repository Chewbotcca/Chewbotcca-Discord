/*
 * Copyright (C) 2024 Chewbotcca
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.json.JSONObject;
import pw.chew.chewbotcca.objects.services.ModrinthProject;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class ModrinthCommand extends SlashCommand {
    public ModrinthCommand() {
        this.name = "modrinth";
        this.help = "Searches modrinth.com for a specified project";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};

        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "query", "The project to search for", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String args = event.optString("query", "");

        try {
            event.replyEmbeds(buildModEmbed(args)).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();

        try {
            event.reply(buildModEmbed(args));
        } catch (IllegalArgumentException e) {
            event.replyError(e.getMessage());
        }
    }

    private MessageEmbed buildModEmbed(String query) {
        // URL encode the query
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);

        // Search for the mod via the API subdomain
        JSONObject data = RestClient.get("https://api.modrinth.com/v2/search?limit=1&query=" + query).asJSONObject();

        // If the mod doesn't exist, throw an error
        if (data.getInt("total_hits") == 0) {
            throw new IllegalArgumentException("No project found for " + query);
        }

        // Store the first JSONObject from the hits jsonarray
        ModrinthProject mod = new ModrinthProject(data.getJSONArray("hits").getJSONObject(0));

        // Add support field
        MessageEmbed.Field support = new MessageEmbed.Field("Platform Support",
            "Client: " + mod.clientSide() + "\n" + "Server: " + mod.serverSide(), true);

        // Add tags
        MessageEmbed.Field tags = new MessageEmbed.Field("Tags", String.join(", ", mod.categories()), true);

        // Add downloads
        MessageEmbed.Field stats = new MessageEmbed.Field("Stats", """
                Downloads: %s
                Followers: %s
                """.formatted(mod.downloads(), mod.followers()), true);

        // Add field for timestamps
        MessageEmbed.Field timestamps = new MessageEmbed.Field("Timestamps",
            // Add one for publish date and publish date
            "Created: " + TimeFormat.DATE_TIME_SHORT.format(mod.createdDate()) + "\n" +
                "Updated: " + TimeFormat.DATE_TIME_SHORT.format(mod.modifiedDate()), true);

        // Add field for latest version
        MessageEmbed.Field latestVersion = new MessageEmbed.Field("Latest Version", mod.latestVersion(), true);

        // Build and return the embed
        return new EmbedBuilder()
            .setAuthor(mod.author(), mod.authorURL())
            .setThumbnail(mod.iconURL())
            .setTitle(mod.title(), mod.pageURL())
            .setDescription(mod.description())
            .addField(support).addField(tags).addField(stats).addField(latestVersion).addField(timestamps)
            .setColor(mod.color())
            .setFooter("Modrinth.com", "https://avatars.githubusercontent.com/u/67560307").build();
    }
}
