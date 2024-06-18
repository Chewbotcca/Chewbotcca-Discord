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
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

// %^qrcode command
public class QRCodeCommand extends SlashCommand {

    public QRCodeCommand() {
        this.name = "qrcode";
        this.help = "Converts arguments into a QR Code";
        this.aliases = new String[]{"qr"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "data", "The data to convert into a QR Code").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Get the query string
        String code = event.optString("data", "");
        event.replyEmbeds(new EmbedBuilder()
            // Encode the input and return an image from google chart apis
            .setImage("https://chart.apis.google.com/chart?chl=" + URLEncoder.encode(code, StandardCharsets.UTF_8) + "&chld=H|0&chs=500x500&cht=qr")
            .build()
        ).queue();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get the query string
        String code = commandEvent.getArgs();
        commandEvent.reply(new EmbedBuilder()
            // Encode the input and return an image from google chart apis
            .setImage("https://chart.apis.google.com/chart?chl=" + URLEncoder.encode(code, StandardCharsets.UTF_8) + "&chld=H|0&chs=500x500&cht=qr")
            .build()
        );
    }
}
