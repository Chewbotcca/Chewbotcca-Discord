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
package pw.chew.chewbotcca.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// %^paste command
public class PasteCommand extends Command {
    Map<String, String> pasted = new HashMap<>();

    public PasteCommand() {
        this.name = "paste";
        this.cooldown = 15;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Store message and file URL for later
        Message message = event.getMessage();
        String file;
        // Check if there's no args, and we're in a TEXT channel.
        if (event.getArgs().isBlank() && event.getChannelType() == ChannelType.TEXT) {
            message = getRecentUpload(event.getTextChannel(), event.getMessage());
            if (message == null) {
                event.reply("No recent messages have a file to paste!");
                return;
            }
            file = message.getAttachments().get(0).getUrl();
        } else {
            file = event.getArgs();
        }

        // Get file name if URL. If no slashes, the name itself is fine.
        String name = file.split("/")[file.split("/").length - 1];

        // We only support discord CDN files for now.
        if (!file.contains("cdn.discordapp.com")) {
            event.reply("Only cdn.discordapp.com urls are supported at this time!");
            return;
        }
        // Check if we've pasted this before
        if (pasted.containsKey(file)) {
            event.reply("Already pasted " + name + "! Link: https://paste.gg/chewbotcca/" + pasted.get(file));
            return;
        }

        event.getChannel().sendTyping().queue();

        // Get the contents of the file
        String contents = RestClient.get(file);

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
        JSONObject response = new JSONObject(RestClient.post("https://api.paste.gg/v1/pastes", "Key " + PropertiesManager.getPasteGgKey(), payload));

        // Return response
        if (response.getString("status").equals("success")) {
            String url = "https://paste.gg/chewbotcca/" + response.getJSONObject("result").getString("id");
            message.reply("Your paste for " + name + " is available at: " + url).mentionRepliedUser(false).queue();
            pasted.put(file, response.getJSONObject("result").getString("id"));
        }
    }

    /**
     * Searches channel for any recent messages that may contain something paste-able
     * @param channel the current channel
     * @param sentMessage the most recent sent message
     * @return a message that contains at least 1 valid attachment, if one exists.
     */
    public Message getRecentUpload(TextChannel channel, Message sentMessage) {
        // First check if current message has an upload (e.g. %^paste [attach])
        if (!sentMessage.getAttachments().isEmpty()) {
            Message.Attachment attachment = sentMessage.getAttachments().get(0);
            if (!(attachment.isImage() || attachment.isVideo())) {
                return sentMessage;
            }
        }
        // Then retrieve 10 messages and go through them
        List<Message> messages = channel.getHistory().retrievePast(10).complete();
        for (Message message : messages) {
            if (message.getAttachments().isEmpty())
                continue;

            Message.Attachment attachment = message.getAttachments().get(0);
            if (attachment.isImage() || attachment.isVideo())
                continue;

            return message;
        }
        return null;
    }
}
