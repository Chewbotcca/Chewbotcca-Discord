/*
 * Copyright (C) 2024 Chewbotcca
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
package pw.chew.chewbotcca.commands.bot;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.time.OffsetDateTime;

/**
 * <h2><code>/ping</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/ping">Docs</a>
 */
public class PingCommand extends SlashCommand {
    public PingCommand() {
        this.name = "ping";
        this.help = "Ping the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Has to be simpler due to interaction weirdness
        OffsetDateTime startTime = event.getTimeCreated();
        OffsetDateTime endTime = OffsetDateTime.now();
        long diffInMs = endTime.toInstant().toEpochMilli() - startTime.toInstant().toEpochMilli();

        event.reply("Pong! Took %sms".formatted(Math.abs(diffInMs))).setEphemeral(true).queue();
    }
}
