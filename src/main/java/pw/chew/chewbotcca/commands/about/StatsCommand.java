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
package pw.chew.chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import pw.chew.chewbotcca.Main;
import pw.chew.chewbotcca.util.DateTime;

import java.time.Instant;

// %^stats command
public class StatsCommand extends Command {

    public StatsCommand() {
        this.name = "stats";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Chewbotcca - A basic, yet functioning, discord bot")
                .addField("Author", "<@!476488167042580481>", true)
                .addField("Code", "[View code on GitHub](http://github.com/Chewbotcca/Discord)", true)
                .addField("Library", "JDA 4.2.0_172", true)
                // Convert the time difference into a time ago
                .addField("Uptime", DateTime.timeAgo(Instant.now().toEpochMilli() - Main.getStart().toEpochMilli()), true)
                // Get the server count. NOT GUILD NOT GUILD NOT GUILD
                .addField("Server Count", String.valueOf(commandEvent.getJDA().getGuildCache().size()), true)
                .setColor(0xd084)
                .build()
        );
    }
}
