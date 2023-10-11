/*
 * Copyright (C) 2023 Chewbotcca
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

public class MinecraftSlashCommand extends SlashCommand {
    public MinecraftSlashCommand() {
        this.name = "minecraft";
        this.help = "Gathers some info from Minecraft";
        this.children = new SlashCommand[]{
            new MCIssueSubCommand(),
            new MCServerSubCommand(),
            new MCUserSubCommand(),
            new MCWikiSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // unused as we delegate everything to sub-commands
    }
}
