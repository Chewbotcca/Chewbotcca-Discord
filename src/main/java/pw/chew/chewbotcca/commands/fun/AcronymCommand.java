/*
 * Copyright (C) 2025 Chewbotcca
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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.RestClient;

import java.util.Collections;

/**
 * <h2><code>/acronym</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/acronym">Docs</a>
 */
public class AcronymCommand extends SlashCommand {
    public AcronymCommand() {
        this.name = "acronym";
        this.help = "Jokingly tries to fill in an acronym.";
        this.contexts = CommandContext.GLOBAL;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "acronym", "The acronym to fill", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Get acronym strip out non letters
        String acronym = event.optString("acronym", "");
        acronym = acronym.replaceAll("[^A-Za-z]", "");

        // Check for blank acronyms
        if (acronym.isBlank()) {
            event.reply("You must provide some letters!").setEphemeral(true).queue();
            return;
        }

        // Get phrase
        String phrase = RestClient.get("https://api.chew.pro/acronym/" + acronym).asGsonObject(Acronym.class).phrase();
        event.reply("Acronym for %s is %s".formatted(acronym, phrase)).queue();
    }

    private record Acronym(String phrase) { }
}
