package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import pro.chew.api.ChewAPI;
import pro.chew.api.objects.SpigotDrama;

public class SpigotDramaCommand extends Command {

    public SpigotDramaCommand() {
        this.name = "spigotdrama";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        SpigotDrama response = new ChewAPI().generateSpigotDrama();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("md678685", "https://github.com/md678685/spigot-drama-generator", "https://avatars0.githubusercontent.com/u/1917406");
        embed.setTitle("Spigot Drama Generator", "https://drama.essentialsx.net/");
        embed.setDescription(response.getPhrase() + "\n\n" + "[Permalink](" + response.getPermalink() + ")");

        commandEvent.reply(embed.build());
    }
}
