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

package pw.chew.chewbotcca.commands.services;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import pw.chew.chewbotcca.commands.services.packages.RubyGemsSubCommand;

/**
 * <h2><code>/packages</code> Command</h2>
 *
 * This command has children, so its documentation is in each child's class.
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/packages">Docs</a>
 */
public class PackagesSlashCommand extends SlashCommand {
    public PackagesSlashCommand() {
        this.name = "packages";
        this.help = "Gathers some info from the package manager";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.children = new SlashCommand[]{
            new RubyGemsSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Unused
    }
}
