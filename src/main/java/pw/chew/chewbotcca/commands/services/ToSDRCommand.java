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
package pw.chew.chewbotcca.commands.services;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.EmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.JDAUtilUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.util.List;

public class ToSDRCommand extends Command {

    public ToSDRCommand() {
        this.name = "tosdr";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.children = new Command[]{new ToSDRInfoSubCommand(), new ToSDRPointsSubCommand(), new ToSDRDocsSubCommand()};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("""
            Please specify the sub-command you want to do. Examples:
            `%^tosdr info google` => Get information about "Google" service.
            `%^tosdr docs google` => Gets document links for "Google" service.
            `%^tosdr points google` => Gets points for "Google" service.
            """.replaceAll("%\\^", event.getPrefix()));
    }

    public static JSONObject getServiceData(String query) {
        query = query.replaceAll("[^0-9a-z_]", "");
        JSONObject data;
        try {
            data = RestClient.get("https://api.tosdr.org/v1/service/" + query + ".json").asJSONObject();
        } catch (JSONException e) {
            throw new IllegalArgumentException("JSON wasn't returned. Please fix your input before I send out another AMBER alert...");
        }

        if (data.has("error") && data.getJSONArray("error").get(0).equals("INVALID_SERVICE")) {
            data = RestClient.get("https://search.tosdr.org/" + query).asJSONObject();
            if (data.getJSONObject("parameters").isNull("service")) {
                throw new IllegalArgumentException("Service doesn't exist!");
            }
            JSONArray services = data.getJSONObject("parameters").getJSONArray("service");
            return getServiceData(services.getJSONObject(0).getInt("id") + "");
        }

        if (data.has("error")) {
            throw new IllegalArgumentException("Could not get service! Errors: " + data.getJSONArray("error").join(", "));
        }

        return data;
    }

    private static class ToSDRInfoSubCommand extends Command {
        private final String[] emoji = {
            "<:AToS:815821354103865375>",
            "<:BToS:815821351444938782>",
            "<:CToS:815821356478103584>",
            "<:DToS:815821358868856852>",
            "<:EToS:815821361015947284>"
        };

        public ToSDRInfoSubCommand() {
            this.name = "info";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            event.getTextChannel().sendTyping().queue();

            JSONObject data;
            try {
                data = getServiceData(event.getArgs());
            } catch (IllegalArgumentException e) {
                event.reply(e.getMessage());
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Terms of Service; Didn't Read Lookup", "https://tosdr.org", "https://cdn.discordapp.com/icons/324969783508467715/64cee34d12d9bda16cb0d6abf8c530a7.png?size=1024");
            embed.setTitle("Service: " + data.getString("name"));
            embed.setThumbnail(data.getString("image"));
            if (data.get("class") instanceof String) {
                embed.addField("Class", emoji[data.getString("class").charAt(0) - 65], true);
            } else {
                embed.addField("Class", "Not yet classified!", true);
            }
            embed.addField("Points", "[View on Phoenix](https://edit.tosdr.org/services/" + data.getInt("id") + ")\n" +
                "Run `" + event.getPrefix() + "tosdr points " + event.getArgs() + "`", false);
            embed.addField("Documents", "Run `" + event.getPrefix() + "tosdr docs " + event.getArgs() + "`", false);
            event.reply(embed.build());
        }
    }

    private static class ToSDRPointsSubCommand extends Command {

        public ToSDRPointsSubCommand() {
            this.name = "points";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            event.getTextChannel().sendTyping().queue();

            JSONObject data;
            try {
                data = getServiceData(event.getArgs());
            } catch (IllegalArgumentException e) {
                event.reply(e.getMessage());
                return;
            }

            EmbedPaginator.Builder paginator = JDAUtilUtil.makeEmbedPaginator();
            paginator.setUsers(event.getAuthor());
            paginator.allowTextInput(true);
            paginator.setBulkSkipNumber(10);

            int points = data.getJSONArray("points").length();
            List<Object> pointList = data.getJSONArray("points").toList();

            for(int i = 0; i < points; i++) {
                JSONObject pointData = data.getJSONObject("pointsData").getJSONObject(pointList.get(i) + "");

                // Build the point embed
                EmbedBuilder e = new EmbedBuilder();
                e.setAuthor("Points for " + data.getString("name"), null, data.getString("image"));

                e.setTitle(pointData.getString("title"));
                e.addField("Point", pointData.getJSONObject("tosdr").getString("point"), true);

                String quoteText;
                if (pointData.isNull("quoteText")) {
                    quoteText = pointData.getJSONObject("tosdr").getString("tldr");
                } else {
                    quoteText = pointData.getString("quoteText");
                }

                String description = "\"" + quoteText + "\"";

                if (!pointData.isNull("quoteDoc")) {
                    description += " - [" +
                        pointData.getString("quoteDoc") + "](" +
                        data.getJSONObject("links").getJSONObject(pointData.getString("quoteDoc")).getString("url") +
                        ")";
                }

                e.setDescription(description);

                e.setFooter("Point " + (i+1) + "/" + points);

                paginator.addItems(e.build());
            }
            // Send it off!
            paginator.setText("");
            paginator.build().paginate(event.getChannel(), 1);
        }
    }

    private static class ToSDRDocsSubCommand extends Command {

        public ToSDRDocsSubCommand() {
            this.name = "docs";
            this.aliases = new String[]{"links", "documents", "doc"};
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            event.getTextChannel().sendTyping().queue();

            JSONObject data;
            try {
                data = getServiceData(event.getArgs());
            } catch (IllegalArgumentException e) {
                event.reply(e.getMessage());
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor("Terms of Service; Didn't Read Lookup", "https://tosdr.org", "https://cdn.discordapp.com/icons/324969783508467715/64cee34d12d9bda16cb0d6abf8c530a7.png?size=1024");
            embed.setTitle("Documents for Service: " + data.getString("name"));
            embed.setThumbnail(data.getString("image"));

            for (String object : data.getJSONObject("links").keySet()) {
                embed.addField(object, data.getJSONObject("links").getJSONObject(object).getString("url"), true);
            }

            event.reply(embed.build());
        }
    }
}
