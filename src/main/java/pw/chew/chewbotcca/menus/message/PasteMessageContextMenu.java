/*
 * Copyright (C) 2024 Chewbotcca
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
package pw.chew.chewbotcca.menus.message;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.Map;

// %^paste command
public class PasteMessageContextMenu extends MessageContextMenu {
    final Map<String, String> pasted = new HashMap<>();

    public PasteMessageContextMenu() {
        this.name = "Pastebin File";
        this.cooldown = 15;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
    }

    @Override
    protected void execute(MessageContextMenuEvent event) {
        // Store message and file URL for later
        Message message = event.getTarget();
        String file = message.getAttachments().get(0).getUrl();

        try {
            event.reply(pasteData(file))
                .setComponents(ActionRow.of(Button.link(message.getJumpUrl(), "Jump to Message")))
                .queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private String pasteData(String file) {
        // Get file name if URL. If no slashes, the name itself is fine.
        String name = file.split("/")[file.split("/").length - 1];

        // We only support discord CDN files for now.
        if (!file.contains("cdn.discordapp.com")) {
            throw new IllegalArgumentException("Only cdn.discordapp.com urls are supported at this time!");
        }

        // Check if we've pasted this before
        if (pasted.containsKey(file)) {
            throw new IllegalArgumentException("Already pasted " + name + "! Link: https://paste.gg/chewbotcca/" + pasted.get(file));
        }

        // Get the contents of the file
        String contents = RestClient.get(file).asString();

        // Create the payload
        JSONObject payload = new JSONObject()
            .put("name", "Uploaded file from Chewbotcca Discord Bot")
            .put("files", new JSONArray().put(new JSONObject()
                .put("name", name)
                .put("content", new JSONObject()
                    .put("format", "text")
                    .put("highlight_language", "null")
                    .put("value", contents)
                )
            ));

        // Upload to paste.gg
        JSONObject response = RestClient.post("https://api.paste.gg/v1/pastes", payload, PropertiesManager.getPasteGgKey()).asJSONObject();

        // Return response
        if (response.getString("status").equals("success")) {
            String url = "https://paste.gg/chewbotcca/" + response.getJSONObject("result").getString("id");
            pasted.put(file, response.getJSONObject("result").getString("id"));
            return "Your paste for " + name + " is available at: " + url;
        } else {
            throw new IllegalArgumentException("Failed to paste data");
        }
    }
}
