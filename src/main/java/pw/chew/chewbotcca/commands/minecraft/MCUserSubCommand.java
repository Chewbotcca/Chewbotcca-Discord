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
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

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
        JSONObject response = RestClient.get("https://playerdb.co/api/player/minecraft/" + name).asJSONObject();

        if (!response.getBoolean("success")) {
            throw new IllegalArgumentException("Not a valid Minecraft player! Please enter a valid username or UUID.");
        }

        JSONObject player = response.getJSONObject("data").getJSONObject("player");
        String uuid = player.getString("id"); // UUID with dashes
        String rawId = player.getString("raw_id"); // UUID without dashes
        String currentName = player.getString("username");
        String avatar = player.getString("avatar");

        // Build name history
        StringBuilder names = new StringBuilder();
        if (player.has("meta") && !player.isNull("meta")) {
            JSONObject meta = player.getJSONObject("meta");
            if (meta.has("name_history") && !meta.isNull("name_history")) {
                JSONArray history = meta.getJSONArray("name_history");
                for (int i = history.length() - 1; i >= 0; i--) {
                    JSONObject entry = history.getJSONObject(i);
                    String time;
                    if (entry.has("changedToAt") && !entry.isNull("changedToAt")) {
                        time = TimeFormat.DATE_TIME_SHORT.format(entry.getLong("changedToAt") * 1000);
                    } else {
                        time = "Original";
                    }
                    names.append("`").append(entry.getString("name")).append("` - ").append(time).append("\n");
                }
            }
        }

        ActionRow buttons = ActionRow.of(
            Button.link("https://namemc.com/profile/" + uuid, "View on NameMC"),
            Button.link("https://playerdb.co/player/minecraft/" + rawId, "View on PlayerDB")
        );

        if (names.length() > 0) {
            return Container.of(
                Section.of(
                    Thumbnail.fromUrl(avatar),
                    TextDisplay.of("# Minecraft Profile Information\n ## User: " + currentName)
                ),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of("Name History\n" + names),
                Separator.createDivider(Separator.Spacing.SMALL),
                buttons
            );
        } else {
            return Container.of(
                Section.of(
                    Thumbnail.fromUrl(avatar),
                    TextDisplay.of("# Minecraft Profile Information\n ## User: " + currentName)
                ),
                Separator.createDivider(Separator.Spacing.SMALL),
                buttons
            );
        }
    }
}
