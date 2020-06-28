package pw.chew.chewbotcca.commands.settings;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import pw.chew.chewbotcca.util.Profile;

public class ProfileCommand extends Command {

    public ProfileCommand() {
        this.name = "profile";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        Profile profile = Profile.retrieveProfile(commandEvent.getAuthor().getId());
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Your Chewbotcca Profile")
                .setDescription("The profile system is a work in progress! More details will appear soon!")
                .setFooter("ID: " + profile.getId())
                .build()
        );
    }
}
