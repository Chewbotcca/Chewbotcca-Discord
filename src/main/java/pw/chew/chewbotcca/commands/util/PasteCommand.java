/*
 * Copyright (C) 2022 Chewbotcca
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
package pw.chew.chewbotcca.commands.util;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// %^paste command
public class PasteCommand extends SlashCommand {
    final Map<String, String> pasted = new HashMap<>();

    public PasteCommand() {
        this.name = "paste";
        this.help = "Pastes a recent message or a provided link to paste.gg";
        this.cooldown = 15;
        this.cooldownScope = CooldownScope.USER;
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "url", "The URL of the file you want to paste (blank to retrieve recent message)").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Store message and file URL for later
        Message message;
        String file;
        String args = event.optString("url", "");
        boolean fromAttachment = false;
        // Check if there's no args, and we're in a TEXT channel or DM.
        if (args.isBlank() && (event.getChannelType() == ChannelType.TEXT || event.getChannelType() == ChannelType.PRIVATE)) {
            message = getRecentUpload(event.getChannel(), null);
            if (message == null) {
                event.reply("No recent messages have a file to paste!").setEphemeral(true).queue();
                return;
            }
            file = message.getAttachments().get(0).getUrl();
            fromAttachment = true;
        } else {
            file = args;
        }

        try {
            event.reply(pasteData(file, fromAttachment)).queue();
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        // Store message and file URL for later
        Message message;
        String file;
        boolean fromAttachment = false;
        // Check if there's no args, and we're in a TEXT channel or DM.
        if (event.getArgs().isBlank() && (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE))) {
            message = getRecentUpload(event.getChannel(), event.getMessage());
            if (message == null) {
                event.reply("No recent messages have a file to paste!");
                return;
            }
            file = message.getAttachments().get(0).getUrl();
            fromAttachment = true;
        } else {
            file = event.getArgs();
        }

        try {
            event.reply(pasteData(file, fromAttachment));
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }

    private String pasteData(String file, boolean fromAttachment) {
        // Get file name if URL. If no slashes, the name itself is fine.
        String name = file.split("/")[file.split("/").length - 1];

        // We only support discord CDN files for now.
        if (!file.contains("cdn.discordapp.com") && !fromAttachment) {
            throw new IllegalArgumentException("Only cdn.discordapp.com urls are supported at this time!");
        }
        // Check if we've pasted this before
        if (pasted.containsKey(file)) {
            throw new IllegalArgumentException("Already pasted " + name + "! Link: https://paste.gg/chewbotcca/" + pasted.get(file));
        }

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
            pasted.put(file, response.getJSONObject("result").getString("id"));
            return "Your paste for " + name + " is available at: " + url;
        } else {
            throw new IllegalArgumentException("Failed to paste data");
        }
    }

    /**
     * Searches channel for any recent messages that may contain something paste-able
     * @param channel the current channel
     * @param sentMessage the most recent sent message
     * @return a message that contains at least 1 valid attachment, if one exists.
     */
    public Message getRecentUpload(MessageChannel channel, Message sentMessage) {
        // First check if current message has an upload (e.g. %^paste [attach])
        if (sentMessage != null && !sentMessage.getAttachments().isEmpty()) {
            Message.Attachment attachment = sentMessage.getAttachments().get(0);
            if (!attachment.isImage() && !attachment.isVideo()) {
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
