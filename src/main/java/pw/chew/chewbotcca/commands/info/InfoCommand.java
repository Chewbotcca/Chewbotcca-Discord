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
package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;

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
                .addChoices(commands())
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {
            event.replyEmbeds(gatherData(ResponseHelper.guaranteeStringOption(event, "command", ""))).queue();
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(ResponseHelper.generateFailureEmbed(null, e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        // Make sure there's an arg
        String command = event.getArgs();
        if (command.isEmpty()) {
            event.reply("Please specify a command to find info for!");
            return;
        }

        try {
            event.reply(gatherData(command));
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private MessageEmbed gatherData(String command) {
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

        e.addField("Arguments", data.isNull("args") ? "No Arguments" : data.getString("args"), true);
        e.addField("Flags", data.isNull("flags") ? "No Flags" : data.getString("flags"), true);
        e.addField("Aliases", data.isNull("aliases") ? "No Aliases" : data.getString("aliases"), true);
        e.addField("Bot Permissions", data.isNull("bot_permissions") ? "*No special perms needed*" : data.getString("bot_permissions"), true);
        e.addField("User Permissions", data.isNull("user_permissions") ? "*No special perms needed*" : data.getString("user_permissions"), true);

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

