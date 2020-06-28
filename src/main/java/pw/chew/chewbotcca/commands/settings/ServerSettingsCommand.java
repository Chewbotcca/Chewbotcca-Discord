package pw.chew.chewbotcca.commands.settings;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import pw.chew.chewbotcca.util.ServerSettings;

public class ServerSettingsCommand extends Command {
    public ServerSettingsCommand() {
        this.name = "serversettings";
        this.aliases = new String[]{"settings"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        Guild ser = commandEvent.getGuild();
        ServerSettings server = ServerSettings.retrieveServer(ser.getId());
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Server Settings for " + ser.getName())
                .setDescription("The settings system is a work in progress! More details will appear soon!")
                .setFooter("ID: " + server.getId())
                .build()
        );
    }
}
