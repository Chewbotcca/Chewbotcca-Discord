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
package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import static pw.chew.chewbotcca.commands.fun.EightBallCommand.getRandom;

public class CoinFlipCommand extends Command {

    public CoinFlipCommand() {
        this.name = "coinflip";
        this.aliases = new String[]{"flip"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent event) {
        String first = getRandom(new String[]{
            "I flipped a coin, and it landed on",
            "I threw the coin into the air and it finally landed on",
            "I dropped the coin, it landed on"});
        String headsOrTails = getRandom(new String[]{
            "heads",
            "tails"});
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Coin Flip");
        e.setDescription(first + " **" + headsOrTails + "**!");
        event.reply(e.build());
    }
}
