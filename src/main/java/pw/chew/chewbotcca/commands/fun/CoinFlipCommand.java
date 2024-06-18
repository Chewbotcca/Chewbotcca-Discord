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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import static pw.chew.chewbotcca.util.MiscUtil.getRandom;

public class CoinFlipCommand extends SlashCommand {

    public CoinFlipCommand() {
        this.name = "coinflip";
        this.help = "Flip a coin";
        this.aliases = new String[]{"flip"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(flipCoin()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(flipCoin());
    }

    private MessageEmbed flipCoin() {
        String first = getRandom("I flipped a coin, and it landed on",
            "I threw the coin into the air and it finally landed on",
            "I dropped the coin, it landed on");
        String headsOrTails = getRandom("heads", "tails");
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Coin Flip");
        e.setDescription(first + " **" + headsOrTails + "**!");
        return e.build();
    }
}
