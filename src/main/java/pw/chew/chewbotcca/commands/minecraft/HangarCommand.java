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
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class HangarCommand extends Command {
    private static final String baseUrl = "https://hangar.minidigger.me/api/v2/";
    private static String key = null;
    private static Instant lastUpdate = Instant.now();

    public HangarCommand() {
        this.name = "hangar";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (lastUpdate.isBefore(Instant.now().minusSeconds(10800)) || key == null) {
            regenerateKey();
        }
        String args = URLEncoder.encode(event.getArgs(), StandardCharsets.UTF_8);
        JSONObject response = new JSONObject(RestClient.get(baseUrl + "projects?q=" + args + "&limit=1", "HangarApi session=\"" + key + "\""));
        LoggerFactory.getLogger(HangarCommand.class).debug(response.toString());
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Hangar Plugin Repository Search Results", baseUrl.replace("api/v2/", ""));
        if (response.getJSONObject("pagination").getInt("count") == 0) {
            embed.setDescription("No results found!");
            event.reply(embed.build());
            return;
        }

        JSONObject plugin = response.getJSONArray("result").getJSONObject(0);
        JSONObject namespace = plugin.getJSONObject("namespace");
        String projectURL = baseUrl.replace("api/v2/", "") + namespace.getString("owner") + "/" + namespace.getString("slug");

        embed.setTitle(plugin.getString("name"), projectURL);
        embed.setDescription(plugin.getString("description"));
        embed.setThumbnail(plugin.getString("icon_url"));
        if (plugin.getJSONArray("promoted_versions").length() > 0) {
            embed.addField("Latest", plugin.getJSONArray("promoted_versions").getJSONObject(0).getString("version"), true);
        }

        JSONObject stats = plugin.getJSONObject("stats");
        embed.addField("Views", String.valueOf(stats.getInt("views")), true);
        embed.addField("Downloads", String.valueOf(stats.getInt("downloads")), true);
        embed.addField("Stars", String.valueOf(stats.getInt("stars")), true);
        embed.addField("Watchers", String.valueOf(stats.getInt("watchers")), true);

        event.reply(embed.build());
    }

    private void regenerateKey() {
        JSONObject response = new JSONObject(RestClient.post(baseUrl + "authenticate", new JSONObject()));
        key = response.getString("session");
        lastUpdate = Instant.now();
    }
}
