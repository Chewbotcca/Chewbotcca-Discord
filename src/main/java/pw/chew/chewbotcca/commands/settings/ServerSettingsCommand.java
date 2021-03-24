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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import pw.chew.chewbotcca.objects.ServerSettings;

import java.util.Arrays;
import java.util.List;

// %^serversettings command
public class ServerSettingsCommand extends Command {
    public ServerSettingsCommand() {
        this.name = "serversettings";
        this.aliases = new String[]{"settings", "ss"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Start typing
        commandEvent.getChannel().sendTyping().queue();
        // Get server and its settings and return info
        Guild ser = commandEvent.getGuild();
        ServerSettings server = ServerSettings.retrieveServer(ser.getId());
        if(!commandEvent.getArgs().contains("set")) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Server Settings for " + ser.getName())
                .setDescription("To set an option, execute `" + commandEvent.getPrefix() + "ss set option value`.\n" +
                    "To view a list of valid options, simply run `" + commandEvent.getPrefix() + "ss set` " +
                    "or visit the [wiki](https://wiki.chew.pro/index.php/Commands/serversettings)")
                .setFooter("ID: " + server.getId());

            embed.addField("Prefix", server.getPrefix() == null ? "Set with `" + commandEvent.getPrefix() + "settings set prefix [prefix]`" : server.getPrefix(), true);

            commandEvent.reply(embed.build());
            return;
        }
        String[] args = commandEvent.getArgs().split(" ");
        if(args.length < 3) {
            commandEvent.reply("""
                You are missing arguments! Must have `set`, `key`, `value`. Possible keys:
                ```
                prefix - The custom server prefix
                ```""");
            return;
        }
        List<String> supported = Arrays.asList("prefix");
        if(supported.contains(args[1].toLowerCase())) {
            server.saveData(args[1].toLowerCase(), args[2]);
            commandEvent.reply("If you see this message, then it saved successfully... hopefully.");
        } else {
            commandEvent.reply("Invalid argument!");
        }
    }
}
