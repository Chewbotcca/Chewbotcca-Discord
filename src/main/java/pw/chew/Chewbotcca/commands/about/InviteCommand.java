package pw.chew.Chewbotcca.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class InviteCommand extends Command {
    public InviteCommand() {
        this.name = "invite";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(new EmbedBuilder()
                .setTitle("Invite me!")
                .setDescription("[Click me to invite me to your server](https://discord.com/oauth2/authorize?client_id=604362556668248095&scope=bot&permissions=0)!\n" +
                        "[Click me to join my help server](https://discord.gg/r583nHA)!\n" +
                        "[Sponsored: Click me to get a VPS from SkySilk Cloud Services](https://www.skysilk.com/ref/4PRQpuQraD)!").build());
    }
}
