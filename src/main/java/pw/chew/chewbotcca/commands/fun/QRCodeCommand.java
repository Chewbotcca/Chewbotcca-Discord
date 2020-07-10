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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// %^qrcode command
public class QRCodeCommand extends Command {
    public QRCodeCommand() {
        this.name = "qrcode";
        this.aliases = new String[]{"qr"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
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
