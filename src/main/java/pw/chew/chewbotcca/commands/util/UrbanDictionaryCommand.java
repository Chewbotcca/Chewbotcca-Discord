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
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.EmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.JDAUtilUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

// %^urban command
public class UrbanDictionaryCommand extends SlashCommand {

    public UrbanDictionaryCommand() {
        this.name = "urban";
        this.help = "Checks a word on Urban Dictionary. Only works in NSFW channels";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "word", "The word to define").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Top.gg requires %^urban to be NSFW only. I hate them but I gotta agree, it can be a bit edgy at times
        if (event.getChannelType() == ChannelType.TEXT && !event.getTextChannel().isNSFW()) {
            event.reply("This command is a little bit too edgy and may only be ran in NSFW channels or DMs. Sorry!").setEphemeral(true).queue();
            return;
        }

        String word = event.optString("word", "");

        // Send message then edit it
        try {
            event.replyEmbeds(new EmbedBuilder().setDescription("Checking the dictionary...").build()).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    gatherDefinition(word, event.getUser()).paginate(message, 1);
                });
            });
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        // Top.gg requires %^urban to be NSFW only. I hate them but I gotta agree, it can be a bit edgy at times
        if (event.getChannelType() == ChannelType.TEXT && !event.getTextChannel().isNSFW()) {
            event.reply("This command is a little bit too edgy and may only be ran in NSFW channels or DMs. Sorry!");
            return;
        }

        // Get the word from Urban Dictionary and if it doesn't exist, let them know.
        String word = event.getArgs();

        // Send message then edit it
        try {
            gatherDefinition(word, event.getAuthor()).paginate(event.getChannel(), 1);
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage());
        }
    }

    private EmbedPaginator gatherDefinition(String word, User user) {
        JSONObject parse = new JSONObject(RestClient.get("https://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(word, StandardCharsets.UTF_8)));
        JSONArray list = parse.getJSONArray("list");
        if (list.isEmpty()) {
            throw new IllegalArgumentException("No results found for term `" + word + "`!");
        }

        EmbedPaginator.Builder paginator = JDAUtilUtil.makeEmbedPaginator();
        paginator.setUsers(user);

        int definitions = list.length();

        for(int i = 0; i < definitions; i++) {
            // Gather information about the word
            JSONObject info = list.getJSONObject(i);
            String definition = info.getString("definition").replaceAll("\n", " ")
                // Remove brackets from words
                .replaceAll("\\[(.*?)]", "$1");
            String example = info.getString("example").replace("\n", " ")
                // Remove brackets from words
                .replaceAll("\\[(.*?)]", "$1");
            int up = info.getInt("thumbs_up");
            int down = info.getInt("thumbs_down");
            String author = info.getString("author");
            int total = up + down;
            // Find like/dislike ratio
            float ratio = ((float) up / (float) total * 100);
            word = info.getString("word");
            String url = info.getString("permalink");
            // Build embed and send it off!
            paginator.addItems(new EmbedBuilder()
                .setTitle("Urban Dictionary definition for **" + word + "**", url)
                .setDescription(definition)
                .addField("Author", author, true)
                .addField("Rating", "**" + up + "** 👍 - **" + down + "** 👎 (**" + ratio + "%**)", true)
                .addField("Example", example, false)
                .setFooter("Definition " + (i+1) + "/" + definitions)
                .build()
            );
        }

        paginator.setText("");
        return paginator.build();
    }
}
