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
package pw.chew.chewbotcca.commands.settings;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chewbotcca.objects.UserProfile;

import java.util.Arrays;
import java.util.List;

// %^profile command
public class ProfileCommand extends SlashCommand {

    public ProfileCommand() {
        this.name = "profile";
        this.help = "Gets your own bot profile.";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.children = new SlashCommand[]{
            new GetProfileSubCommand(),
            new SetProfileSubCommand(),
            new DeleteProfileSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Not executed because slash commands with children don't have root commands
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Start typing
        commandEvent.getChannel().sendTyping().queue();
        // Get Bot Profile details and send
        UserProfile profile = UserProfile.retrieveProfile(commandEvent.getAuthor().getId());
        commandEvent.reply(getProfileData(profile, commandEvent.getPrefix()));
    }

    private MessageEmbed getProfileData(UserProfile profile, String prefix) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Your Chewbotcca Profile")
            .setDescription("The profile system is a work in progress! More details will appear soon!")
            .setFooter("ID: " + profile.getId());

        embed.addField("Lastfm Username", profile.getLastFm() == null ? "Set with `" + prefix + "profile set lastfm [name]`" : profile.getLastFm(), true);
        embed.addField("GitHub Username", profile.getGitHub() == null ? "Set with `" + prefix + "profile set github [name]`" : profile.getGitHub(), true);

        return embed.build();
    }

    private class GetProfileSubCommand extends SlashCommand {

        private GetProfileSubCommand() {
            this.name = "get";
            this.help = "Gets your bot profile";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            // Get Bot Profile details and send
            UserProfile profile = UserProfile.retrieveProfile(event.getUser().getId());
            event.replyEmbeds(getProfileData(profile, "/")).setEphemeral(true).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            // Get Bot Profile details and send
            UserProfile profile = UserProfile.retrieveProfile(event.getAuthor().getId());
            event.reply(getProfileData(profile, "/"));
        }
    }

    private static class SetProfileSubCommand extends SlashCommand {

        private SetProfileSubCommand() {
            this.name = "set";
            this.help = "Changes information associated with your bot profile";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;

            this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "key", "Which key to modify")
                    .setRequired(true)
                    .addChoices(
                        new Command.Choice("Last.fm Username", "lastfm"),
                        new Command.Choice("GitHub Username", "github")
                    ),
                new OptionData(OptionType.STRING, "value", "The value to set").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            // Get Bot Profile details and send
            UserProfile profile = UserProfile.retrieveProfile(event.getUser().getId());

            profile.saveData(
                event.optString("key", ""),
                event.optString("value", "")
            );

            event.reply("If you see this message, then it saved successfully... hopefully.").setEphemeral(true).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            // Get Bot Profile details and send
            UserProfile profile = UserProfile.retrieveProfile(event.getAuthor().getId());

            String[] args = event.getArgs().split(" ");
            if (args.length < 2) {
                event.reply("""
                    You are missing arguments! Must have `set`, `key`, `value`. Possible keys:
                    ```
                    lastfm - Your last.fm username for %^lastfm
                    github - Your GitHub username for %^ghuser
                    ```""".replaceAll("%\\^", event.getPrefix()));
                return;
            }
            List<String> supported = Arrays.asList("github", "lastfm");
            if (supported.contains(args[0].toLowerCase())) {
                profile.saveData(args[0].toLowerCase(), args[1]);
                event.reply("If you see this message, then it saved successfully... hopefully.");
            } else {
                event.reply("Unsupported argument!");
            }
        }
    }

    private static class DeleteProfileSubCommand extends SlashCommand {

        private DeleteProfileSubCommand() {
            this.name = "delete";
            this.help = "Deletes all information associated with your bot profile";
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.guildOnly = false;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            UserProfile profile = UserProfile.retrieveProfile(event.getUser().getId());
            profile.delete();
            event.reply("Your profile has been deleted from the database!").setEphemeral(true).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
            UserProfile profile = UserProfile.retrieveProfile(event.getAuthor().getId());
            profile.delete();
            event.reply("Your profile has been deleted from the database!");
        }
    }
}
