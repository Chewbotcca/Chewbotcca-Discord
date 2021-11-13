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
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.internal.utils.Checks;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.jdachewtils.command.OptionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// %^info command
public class InfoCommand extends SlashCommand {
    public InfoCommand() {
        this.name = "info";
        this.help = "Returns some detailed information about a specified command";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;

        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "command", "The command to find info for").setRequired(true)
                //.addChoices(commands())
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            event.replyEmbeds(gatherData(OptionHelper.optString(event, "command", ""))).queue();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Did you mean? ")) {
                SelectionMenu menu = buildSuggestionMenu(e.getMessage());

                event.reply("Invalid command! See <https://chew.pw/chewbotcca/discord/commands> for a list of commands.")
                    .addActionRow(menu)
                    .queue();
            } else {
                event.replyEmbeds(ResponseHelper.generateFailureEmbed(null, e.getMessage())).setEphemeral(true).queue();
            }
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        // Make sure there's an arg
        String command = event.getArgs();
        if (command.isBlank()) {
            event.reply("Please specify a command to find info for!");
            return;
        }

        try {
            event.reply(gatherData(command));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Did you mean? ")) {
                SelectionMenu menu = buildSuggestionMenu(e.getMessage());

                event.getChannel().sendMessage("Invalid command! See <https://chew.pw/chewbotcca/discord/commands> for a list of commands.")
                    .setActionRow(menu)
                    .queue();

                return;
            }

            event.replyWarning(e.getMessage());
        }
    }

    /**
     * Update the "Did you mean?" embed with their selection
     *
     * @param event the selection menu event
     */
    public static void updateInfo(SelectionMenuEvent event) {
        // Selected options is only null if ephemeral, which it never will be
        Checks.notNull(event.getSelectedOptions(), "Selected options");
        String selected = event.getSelectedOptions().get(0).getValue();
        if (selected.equals("NONE")) {
            event.editComponents(new ArrayList<>()).queue();
            return;
        }

        event.editMessageEmbeds(gatherData(event.getSelectedOptions().get(0).getValue()))
            .setContent(null)
            .setActionRows(new ArrayList<>())
            .queue();
    }

    /**
     * Builds a selection menu based on the predictions for the /info response
     *
     * @param message the original message
     * @return a selection menu
     */
    private SelectionMenu buildSuggestionMenu(String message) {
        String[] options = message.split("Did you mean\\? ")[1].split(", ");
        List<SelectOption> data = new ArrayList<>();
        for (String option : options) {
            data.add(SelectOption.of(option, option));
        }
        data.add(SelectOption.of("None of these", "NONE").withDescription("Can't find the command? Select to cancel."));
        return SelectionMenu.create("info:didyoumean")
            .setPlaceholder("Did you mean?")
            .addOptions(data)
            .build();
    }

    /**
     * Gather the data from the internal Chewbotcca commands API
     *
     * @param command the command to look up
     * @return a completed embed filled with command info
     * @throws IllegalArgumentException if the command does not exist
     */
    private static MessageEmbed gatherData(String command) {
        // Get the command from the chew api
        JSONObject data = new JSONObject(RestClient.get("https://chew.pw/chewbotcca/discord/api/command/" + command));
        // If there's an error
        if (data.has("error")) {
            JSONArray didYouMean = data.getJSONArray("didYouMean");
            ArrayList<String> predictions = new ArrayList<>();
            for (int i = 0; i < didYouMean.length(); i++) {
                predictions.add(didYouMean.getString(i));
            }
            if (predictions.size() > 0) {
                throw new IllegalArgumentException("Invalid command! See <https://chew.pw/chewbotcca/discord/commands> for a list of commands. Did you mean? " + String.join(", ", predictions));
            } else {
                throw new IllegalArgumentException("Invalid command! See <https://chew.pw/chewbotcca/discord/commands> for a list of commands.");
            }
        }

        // Gather the data and make an embed with it
        EmbedBuilder e = new EmbedBuilder()
            .setTitle("**Info For**: `" + PropertiesManager.getPrefix() + data.getString("command") + "`")
            .setDescription(data.getString("description"));

        e.addField("Arguments", data.optString("args", "No Arguments"), true);
        e.addField("Flags", data.optString("flags", "No Flags"), true);
        e.addField("Aliases", data.optString("aliases", "No Aliases"), true);
        e.addField("Bot Permissions", data.optString("bot_permissions", "*No special perms needed*"), true);
        e.addField("User Permissions", data.optString("user_permissions", "*No special perms needed*"), true);

        return e.build();
    }

    private List<Command.Choice> commands() {
        List<Command.Choice> response = new ArrayList<>();
        for (com.jagrosh.jdautilities.command.Command command : Memory.getClient().getCommands()) {
            response.add(new Command.Choice(command.getName(), command.getName()));
        }
        return response;
    }
}

