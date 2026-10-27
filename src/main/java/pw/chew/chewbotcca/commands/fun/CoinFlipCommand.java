/*
 * Copyright (C) 2026 Chewbotcca
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
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import pw.chew.chewbotcca.util.CommandContext;
import pw.chew.chewbotcca.util.EmojiUtil;
import pw.chew.chewbotcca.util.MiscUtil;

/**
 * <h2><code>/coinflip</code> Command</h2>
 *
 * <a href="https://help.chew.pro/bots/discord/chewbotcca/commands/coinflip">Docs</a>
 */
public class CoinFlipCommand extends SlashCommand {
    private static final String[] PREFIXES = {
        "I flipped a coin, and it landed on",
        "I threw the coin into the air and it finally landed on",
        "I dropped the coin, it landed on",
    };

    public CoinFlipCommand() {
        this.name = "coinflip";
        this.help = "Flips a coin.";
        this.contexts = CommandContext.GLOBAL;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyComponents(flipCoin()).useComponentsV2().queue();
    }

    private MessageTopLevelComponent flipCoin() {
        String prefix = MiscUtil.getRandom(PREFIXES);
        String headsOrTails = MiscUtil.getRandom("heads", "tails");

        EmojiUtil.Emoji emoji = headsOrTails.equals("heads") ? EmojiUtil.Emoji.HEADS_SPIN : EmojiUtil.Emoji.TAILS_SPIN;

        return TextDisplay.of("%s %s **%s**.".formatted(emoji.get(), prefix, headsOrTails));
    }
}
