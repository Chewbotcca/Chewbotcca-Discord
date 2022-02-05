/*
 * Copyright (C) 2022 Chewbotcca
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class PrivacyCommand extends SlashCommand {

    public PrivacyCommand() {
        this.name = "privacy";
        this.help = "Find a link to Chewbotcca's privacy policy";
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("You can view the Chewbotcca privacy policy here: https://chew.pw/chewbotcca/discord/privacy").setEphemeral(true).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("You can view the Chewbotcca privacy policy here: https://chew.pw/chewbotcca/discord/privacy");
    }
}
