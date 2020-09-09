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
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.Main;
import pw.chew.chewbotcca.util.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MCStatusCommand extends Command {

    public MCStatusCommand() {
        this.name = "mcstatus";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendTyping().queue();
        // Get stats
        JSONArray statusurl = new JSONArray(RestClient.get("https://status.mojang.com/check"));
        List<String> forbiddenSites = Arrays.asList("minecraft.net", "sessionserver.mojang.com", "mojang.com");
        List<CharSequence> up = new ArrayList<>();
        List<CharSequence> shakey = new ArrayList<>();
        List<CharSequence> red = new ArrayList<>();

        // Iterate through each site
        for(Object site : statusurl) {
            JSONObject siteData = (JSONObject)site;
            String url = siteData.keys().next();
            if(forbiddenSites.contains(url))
                continue;
            String status = siteData.getString(url);
            switch (status) {
                case "green" -> up.add(url);
                case "yellow" -> shakey.add(url);
                case "red" -> red.add(url);
            }
        }

        if(isUp("https://www.minecraft.net/en-us/")) {
            up.add("minecraft.net");
        } else {
            red.add("minecraft.net");
        }

        if(isUp("https://sessionserver.mojang.com/blockedservers")) {
            up.add("sessionserver.mojang.com");
        } else {
            red.add("sessionserver.mojang.com");
        }

        // Return gathered info
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Minecraft/Mojang Statuses");
        e.setDescription("minecraft.net and sessionserver.mojang.com were manually checked, see [WEB-2303](https://bugs.mojang.com/browse/WEB-2303).");
        if(!up.isEmpty()) {
            e.addField("Up", String.join("\n", up), true);
        }
        if(!shakey.isEmpty()) {
            e.addField("Unstable", String.join("\n", shakey), true);
        }
        if(!red.isEmpty()) {
            e.addField("Down", String.join("\n", red), true);
        }

        commandEvent.reply(e.build());
    }

    public boolean isUp(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Chewbotcca-5331/1.0 (JDA; +https://chew.pw/chewbotcca) DBots/604362556668248095")
                .get()
                .build();

        try (Response response = Main.getJDA().getHttpClient().newCall(request).execute()) {
            return response.code() == 200;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
