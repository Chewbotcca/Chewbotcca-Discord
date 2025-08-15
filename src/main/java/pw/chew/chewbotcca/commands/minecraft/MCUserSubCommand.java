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
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * <h2><code>/minecraft user</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/minecraft#user-subcommand">Docs</a>
 */
public class MCUserSubCommand extends SlashCommand {

    public MCUserSubCommand() {
        this.name = "user";
        this.help = "Looks up a Minecraft user and returns their profile";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "user", "The user name or UUID to lookup").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String name = event.optString("user", "");
        try {
            event.deferReply()
                .queue(hook -> hook.editOriginalComponents(gatherData(name)).useComponentsV2().queue());
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private Container gatherData(String name) {
        String uuid;
        // Get profile info
        if (name.length() == 32 || name.length() == 36) {
            // If it's a UUID
            try {
                uuid = name;
            } catch (JSONException e) {
                throw new IllegalArgumentException("Not a valid input! Please enter a valid UUID!");
            }
        } else if (name.length() >= 1 && name.length() <= 16) {
            // If it's a username
            try {
                JSONObject profile = RestClient.get("https://api.mojang.com/users/profiles/minecraft/" + name).asJSONObject();
                uuid = profile.getString("id");
            } catch (JSONException e) {
                throw new IllegalArgumentException("Not a valid input! Please enter a valid username!");
            }
        } else {
            throw new IllegalArgumentException("Not a valid input! Please enter a valid username or a valid UUID!");
        }
        JSONObject profile = RestClient.get("https://laby.net/api/v2/user/" + uuid + "/get-profile").asJSONObject();
        JSONArray history = profile.getJSONArray("username_history");

        // Find recent names and when they were changed
        StringBuilder names = new StringBuilder();
        for(int i = history.length() - 1; i >= 0; i--) {
            JSONObject data = history.getJSONObject(i);
            String time;
            if (!data.isNull("changed_at") && data.getString("changed_at").length() > 4) {
                OffsetDateTime at = MiscUtil.dateParser(data.getString("changed_at"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                time = TimeFormat.DATE_TIME_SHORT.format(at);
            } else if (!data.isNull("changed_at")) {
                time = data.getString("changed_at");
            } else {
                time = "Original";
            }

            String username = data.getString("name");
            names.append("`").append(username).append("` - ").append(time).append("\n");
        }

        String currentName = profile.getString("username");

        return Container.of(
            Section.of(
                // avatar
                Thumbnail.fromUrl("https://minotar.net/helm/" + uuid),
                TextDisplay.of("# Minecraft Profile Information\n ## User: " + currentName)
            ),

            Separator.createDivider(Separator.Spacing.SMALL),

            TextDisplay.of("Name History\n" + names),

            Separator.createDivider(Separator.Spacing.SMALL),

            // buttons to view online
            ActionRow.of(
                Button.link("https://namemc.com/profile/" + uuid, "View on NameMC"),
                Button.link("https://laby.net/@" + uuid, "View on LabyMod")
            )
        );
    }
}
