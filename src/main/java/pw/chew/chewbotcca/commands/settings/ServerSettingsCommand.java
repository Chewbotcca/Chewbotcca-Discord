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
package pw.chew.chewbotcca.commands.settings;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.objects.ServerSettings;
import pw.chew.jdachewtils.command.OptionHelper;

import java.util.Arrays;
import java.util.List;

// %^serversettings command
public class ServerSettingsCommand extends SlashCommand {

    public ServerSettingsCommand() {
        this.name = "serversettings";
        this.aliases = new String[]{"settings", "ss"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
        this.children = new SlashCommand[]{
            new GetServerSettingsSubCommand(),
            new SetServerSettingsSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Not executed because slash commands with children don't have root commands
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Start typing
        commandEvent.getChannel().sendTyping().queue();
        // Get server and its settings and return info
        Guild ser = commandEvent.getGuild();
        ServerSettings server = ServerSettings.retrieveServer(ser.getId());
        commandEvent.reply(getServerData(server, commandEvent.getGuild(), commandEvent.getPrefix()));
    }

    private MessageEmbed getServerData(ServerSettings server, Guild ser, String prefix) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Server Settings for " + ser.getName())
            .setDescription("To set an option, execute `" + prefix + "serversettings set option value`.\n" +
                "To view a list of valid options, simply run `" + prefix + "serversettings set` " +
                "or visit the [wiki](https://wiki.chew.pro/view/Commands/serversettings)")
            .setFooter("ID: " + server.getId());

        embed.addField("Prefix", server.getPrefix() == null ? "Set with `" + prefix + "serversettings set prefix [prefix]`" : server.getPrefix(), true);

        return embed.build();
    }

    private class GetServerSettingsSubCommand extends SlashCommand {

        private GetServerSettingsSubCommand() {
            this.name = "get";
            this.help = "Gets this server's settings";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = false;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Guild ser = event.getGuild();
            ServerSettings server = ServerSettings.retrieveServer(ser.getId());
            event.replyEmbeds(getServerData(server, event.getGuild(), "/")).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            Guild ser = event.getGuild();
            ServerSettings server = ServerSettings.retrieveServer(ser.getId());
            event.reply(getServerData(server, event.getGuild(), event.getPrefix()));
        }
    }

    private static class SetServerSettingsSubCommand extends SlashCommand {

        private SetServerSettingsSubCommand() {
            this.name = "set";
            this.help = "Changes information associated with this server's settings";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;

            this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "key", "Which key to modify")
                    .setRequired(true)
                    .addChoices(
                        new Command.Choice("Custom Prefix (for normal commands)", "prefix")
                    ),
                new OptionData(OptionType.STRING, "value", "The value to set").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            // Get Bot Profile details and send
            ServerSettings server = ServerSettings.retrieveServer(event.getGuild().getId());

            server.saveData(
                OptionHelper.optString(event, "key", ""),
                OptionHelper.optString(event, "value", "")
            );

            event.reply("If you see this message, then it saved successfully... hopefully.").setEphemeral(true).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            ServerSettings server = ServerSettings.retrieveServer(event.getGuild().getId());
            String[] args = event.getArgs().split(" ");
            if (args.length < 3) {
                event.reply("""
                    You are missing arguments! Must have `set`, `key`, `value`. Possible keys:
                    ```
                    prefix - The custom server prefix
                    ```""");
                return;
            }
            List<String> supported = Arrays.asList("prefix");
            if (supported.contains(args[1].toLowerCase())) {
                server.saveData(args[1].toLowerCase(), args[2]);
                event.reply("If you see this message, then it saved successfully... hopefully.");
            } else {
                event.reply("Invalid argument!");
            }
        }
    }
}