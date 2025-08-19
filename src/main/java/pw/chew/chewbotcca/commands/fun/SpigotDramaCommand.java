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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.RestClient;

import java.util.Collections;

/**
 * <h2><code>/spigotdrama Command</code></h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/spigotdrama">Docs</a>
 */
public class SpigotDramaCommand extends SlashCommand {
    public SpigotDramaCommand() {
        this.name = "spigotdrama";
        this.help = "Generates some random Spigot drama";
        this.contexts = CommandContext.GLOBAL;

        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "mode", "Which mode to use (default: mja00 fork)")
                .addChoice("mja00 fork", "https://drama.mart.fyi/api")
                .addChoice("Classic (mdcfe)", "https://api.chew.pro/spigotdrama")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Find which API to call
        String api = event.optString("mode", "https://drama.mart.fyi/api");

        // Get a SpigotDrama response
        JSONObject response = RestClient.get(api).asJSONObject();

        // Build response
        Container container = Container.of(
            TextDisplay.of("# Spigot Drama Generator"),
            TextDisplay.of(response.getString("response")),
            Separator.createDivider(Separator.Spacing.SMALL),
            ActionRow.of(Button.link(
                response.getString("permalink").replace("/api", ""),
                "Permalink"
            ))
        );

        event.replyComponents(container).useComponentsV2(true).queue();
    }
}
