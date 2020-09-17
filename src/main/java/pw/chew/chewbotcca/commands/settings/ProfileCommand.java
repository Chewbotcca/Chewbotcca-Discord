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
import pw.chew.chewbotcca.objects.Profile;

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
        if(commandEvent.getArgs().equals("delete")) {
            profile.delete();
            commandEvent.reply("Your profile has been deleted from the database!");
            return;
        }
        if(!commandEvent.getArgs().contains("set")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Your Chewbotcca Profile")
                    .setDescription("The profile system is a work in progress! More details will appear soon!")
                    .setFooter("ID: " + profile.getId());

            if(profile.getLastFm() == null) {
                embed.addField("Lastfm Username", "Set with `%^profile set lastfm [name]`", true);
            } else {
                embed.addField("Lastfm Username", profile.getLastFm(), true);
            }

            commandEvent.reply(embed.build());
            return;
        }
        String[] args = commandEvent.getArgs().split(" ");
        if(args.length < 3) {
            commandEvent.reply("""
                You are missing arguments! Must have `set`, `key`, `value`. Possible keys:
                ```
                lastfm - Your last.fm username for %^lastfm
                ```""");
            return;
        }
        profile.saveData(args[1].toLowerCase(), args[2]);
        commandEvent.reply("If you see this message, then it saved successfully... hopefully.");
    }
}
