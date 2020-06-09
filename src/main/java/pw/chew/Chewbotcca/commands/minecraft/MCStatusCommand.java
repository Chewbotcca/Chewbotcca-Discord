package pw.chew.Chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.Chewbotcca.util.RestClient;

public class MCStatusCommand extends Command {

    public MCStatusCommand() {
        this.name = "mcstatus";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        JSONArray statusurl = new JSONArray(RestClient.get("https://status.mojang.com/check"));
        String[] sites = new String[]{"minecraft.net", "session.minecraft.net", "account.mojang.com", "authserver.mojang.com", "sessionserver.mojang.com", "api.mojang.com", "textures.minecraft.net", "mojang.com"};
        StringBuilder up = new StringBuilder();
        StringBuilder shakey = new StringBuilder();
        StringBuilder red = new StringBuilder();

        for(int i = 0; i < statusurl.length(); i++) {
            JSONObject data = statusurl.getJSONObject(i);
            String status = data.getString(sites[i]);
            switch (status) {
                case "green" -> up.append(sites[i]).append("\n");
                case "yellow" -> shakey.append(sites[i]).append("\n");
                case "red" -> red.append(sites[i]).append("\n");
            }
        }

        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Minecraft/Mojang Statuses");
        e.setDescription("Status reports may be inaccurate, see [WEB-2303](https://bugs.mojang.com/browse/WEB-2303).");
        if(!up.toString().equals("")) {
            e.addField("Up", up.toString(), true);
        }
        if(!shakey.toString().equals("")) {
            e.addField("Unstable", shakey.toString(), true);
        }
        if(!red.toString().equals("")) {
            e.addField("Down", red.toString(), true);
        }

        commandEvent.reply(e.build());
    }
}
