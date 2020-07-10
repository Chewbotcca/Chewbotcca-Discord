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
package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

public class MCStatusCommand extends Command {

    public MCStatusCommand() {
        this.name = "mcstatus";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Get stats
        JSONArray statusurl = new JSONArray(RestClient.get("https://status.mojang.com/check"));
        String[] sites = new String[]{"minecraft.net", "session.minecraft.net", "account.mojang.com", "authserver.mojang.com", "sessionserver.mojang.com", "api.mojang.com", "textures.minecraft.net", "mojang.com"};
        StringBuilder up = new StringBuilder();
        StringBuilder shakey = new StringBuilder();
        StringBuilder red = new StringBuilder();

        // Iterate through each site
        for(int i = 0; i < statusurl.length(); i++) {
            JSONObject data = statusurl.getJSONObject(i);
            String status = data.getString(sites[i]);
            switch (status) {
                case "green" -> up.append(sites[i]).append("\n");
                case "yellow" -> shakey.append(sites[i]).append("\n");
                case "red" -> red.append(sites[i]).append("\n");
            }
        }

        // Return gathered info
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
