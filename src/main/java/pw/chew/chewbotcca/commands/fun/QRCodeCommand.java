package pw.chew.chewbotcca.commands.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QRCodeCommand extends Command {
    public QRCodeCommand() {
        this.name = "qrcode";
        this.aliases = new String[]{"qr"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        String code = commandEvent.getArgs();
        commandEvent.reply(new EmbedBuilder()
                .setImage("https://chart.apis.google.com/chart?chl=" + URLEncoder.encode(code, StandardCharsets.UTF_8) + "&chld=H|0&chs=500x500&cht=qr")
                .build()
        );
    }
}
