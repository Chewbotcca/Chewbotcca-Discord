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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.util.Collections;

// %^acronym command
public class AcronymCommand extends SlashCommand {

    public AcronymCommand() {
        this.name = "acronym";
        this.help = "Fill in an acronym (2% accuracy)";
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "acronym", "The acronym to fill").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Get acronym and send if the acronym is valid
        try {
            String acronym = ResponseHelper.guaranteeStringOption(event, "acronym", "");
            String phrase = Memory.getChewAPI().generateAcronym(acronym);
            event.reply("Acronym for " + acronym + " is " + phrase).queue();
        } catch (IllegalArgumentException e) {
            event.reply("Args must only contain letters!").setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get acronym and send if the acronym is valid
        try {
            String phrase = Memory.getChewAPI().generateAcronym(event.getArgs());
            event.reply("Acronym for " + event.getArgs() + " is " + phrase);
        } catch (IllegalArgumentException e) {
            event.reply("Args must only contain letters!");
        }
    }
}
