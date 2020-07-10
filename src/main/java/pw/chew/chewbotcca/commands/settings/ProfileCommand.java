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
package pw.chew.chewbotcca.commands.settings;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import pw.chew.chewbotcca.util.Profile;

// %^profile command
public class ProfileCommand extends Command {

    public ProfileCommand() {
        this.name = "profile";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Start typing
        commandEvent.getChannel().sendTyping().queue();
        // Get Bot Profile details and send
        Profile profile = Profile.retrieveProfile(commandEvent.getAuthor().getId());
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Your Chewbotcca Profile")
                .setDescription("The profile system is a work in progress! More details will appear soon!")
                .setFooter("ID: " + profile.getId())
                .build()
        );
    }
}
