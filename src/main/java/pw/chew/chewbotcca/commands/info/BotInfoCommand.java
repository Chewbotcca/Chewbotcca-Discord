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
package pw.chew.chewbotcca.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// %^binfo command
public class BotInfoCommand extends Command {
    public BotInfoCommand() {
        this.name = "botinfo";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.aliases = new String[]{"binfo"};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Send typing, this'll take a while
        commandEvent.getChannel().sendTyping().queue();
        String[] args = commandEvent.getArgs().split(" ");
        // If there's no args
        if(args.length == 0) {
            commandEvent.reply("Please specify a bot with either a mention or its ID");
            return;
        }
        // Parse the bot mention or just use the ID they provided an ID
        String botId;
        if(args[0].contains("<@")) {
            botId = args[0].replace("<@!", "").replace(">", "");
        } else {
            botId = args[0];
        }
        // Default list: dbl, otherwise use what they specify
        String list = "dbl";
        if(args.length > 1) {
            list = args[1].toLowerCase();
        }
        // Get it from the specified list if it's valid, and let them know
        switch (list) {
            case "dbl", "top.gg", "topgg" -> commandEvent.reply(gatherTopggInfo(botId, commandEvent).build());
            case "dbots" -> commandEvent.reply(gatherDBotsInfo(botId, commandEvent).build());
            default -> commandEvent.reply("Invalid Bot List! Possible: ```dbots, topgg```");
        }
    }

    /**
     * Gather info from top.gg
     * @param id the bot id
     * @param event the command event
     * @return an embed ready to be build
     */
    private EmbedBuilder gatherTopggInfo(String id, CommandEvent event) {
        // Get data from top.gg
        JSONObject bot = new JSONObject(RestClient.get("https://top.gg/api/bots/" + id, PropertiesManager.getTopggToken()));

        // If there's an error let them know
        if(bot.has("error")) {
            return new EmbedBuilder().setTitle("Error!").setDescription(bot.getString("error"));
        }

        // Start generating the embed
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Bot Information");

        // If certified set the image to the certification badge, otherwise use the bot avatar
        String certified;
        if(bot.getBoolean("certifiedBot")) {
            certified = "https://cdn.discordapp.com/emojis/392249976639455232.png";
        } else {
            certified = String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", id, bot.getString("avatar"), "png");
        }

        // Set the author to the bot name
        e.setAuthor(bot.getString("username") + "#" + bot.getString("discriminator"), "https://top.gg/bot/" + id, certified);

        // Set the thumbanil to the bot avatar
        e.setThumbnail(String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", id, bot.getString("avatar"), "png"));

        // Set the description to the short description
        e.setDescription(bot.getString("shortdesc"));

        e.addField("Bot ID", bot.getString("id"), true);

        // Set server count if there is one
        if(bot.has("server_count"))
            e.addField("Server Count", String.valueOf(bot.getInt("server_count")), true);
        else
            e.addField("Server Count", "Unknown", true);

        // Set other details
        e.addField("Prefix", "`" + bot.getString("prefix") + "`", true);
        e.addField("Library", bot.getString("lib"), true);
        e.addField("Points", "This Month: " + bot.getInt("monthlyPoints") + "\n" +
                "All Time: " + bot.getInt("points"), true);

        // Find and set tags
        List<String> tags = new ArrayList<>();
        for(Object tag : bot.getJSONArray("tags")) {
            tags.add((String) tag);
        }

        if(tags.isEmpty()) {
            e.addField("Tags", "None", true);
        } else {
            e.addField("Tags", String.join(", ", tags), true);
        }

        // Find and set owners
        List<String> owners = new ArrayList<>();
        for(Object owner : bot.getJSONArray("owners")) {
            User user = event.getJDA().getUserById((String) owner);
            if(user == null)
                owners.add((String) owner);
            else
                owners.add(user.getAsTag());
        }

        if(owners.isEmpty()) {
            e.addField("Owners", "None", true);
        } else {
            e.addField("Owners", String.join(", ", owners), true);
        }

        // Find and set links
        List<CharSequence> links = new ArrayList<>();
        links.add("[Bot Page](https://top.gg/bot/" + id + ")");
        links.add("[Vote](https://top.gg/bot/" + id + "/vote)");
        if(!bot.getString("invite").equals(""))
            links.add("[Invite](" + bot.getString("invite") + ")");
        if(!bot.getString("website").equals(""))
            links.add("[Website](" + bot.getString("website") + ")");
        if(!bot.getString("support").equals(""))
            links.add("[Support Server](https://discord.gg/" + bot.getString("support") + ")");
        if(!bot.getString("github").equals(""))
            links.add("[GitHub](" + bot.getString("github") + ")");

        e.addField("Links", String.join("\n", links), true);

        e.setFooter("Bot added");
        e.setTimestamp(dateParser(bot.getString("date")));

        e.setColor(Color.decode("#43B581"));

        return e;
    }

    /**
     * Gather info from (the superior) discord.bots.gg
     * @param id the bot id
     * @param event the command event
     * @return an embed ready to be build
     */
    private EmbedBuilder gatherDBotsInfo(String id, CommandEvent event) {
        // Gather info from the site
        JSONObject bot = new JSONObject(RestClient.get("https://discord.bots.gg/api/v1/bots/" + id, PropertiesManager.getDbotsToken()));

        // If there's a message
        if(bot.has("message")) {
            return new EmbedBuilder().setTitle("Error!").setDescription("This bot does not exist on this list!");
        }

        // Start setting embed
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Bot Information");
        e.setAuthor(bot.getString("username") + "#" + bot.getString("discriminator"), "https://discord.bots.gg/bots/" + bot.getString("userId"));
        e.setThumbnail(bot.getString("avatarURL"));
        e.setDescription(bot.getString("shortDescription"));
        e.addField("Bot ID", bot.getString("userId"), true);
        e.addField("Server Count", String.valueOf(bot.getInt("guildCount")), true);
        e.addField("Prefix", "`" + bot.getString("prefix") + "`", true);
        e.addField("Library", bot.getString("libraryName"), true);

        // Find and set the owner as a Discord User
        User user = event.getJDA().getUserById(bot.getJSONObject("owner").getString("userId"));
        if(user == null)
            e.addField("Owner", "Unknown", true);
        else
            e.addField("Owner", user.getAsTag(), true);

        // Set links
        List<CharSequence> links = new ArrayList<>();
        links.add("[Bot Page](https://discord.bots.gg/bots/" + id + ")");
        if(!bot.getString("botInvite").equals(""))
            links.add("[Invite](" + bot.getString("botInvite") + ")");
        if(!bot.getString("website").equals(""))
            links.add("[Website](" + bot.getString("website") + ")");
        if(!bot.getString("supportInvite").equals(""))
            links.add("[Support Server](" + bot.getString("supportInvite") + ")");
        if(!bot.getString("openSource").equals(""))
            links.add("[Source Code](" + bot.getString("openSource") + ")");

        e.addField("Links", String.join("\n", links), true);

        e.setFooter("Bot added");
        e.setTimestamp(dateParser(bot.getString("addedDate")));

        e.setColor(Color.decode("#43B581"));

        return e;
    }

    /**
     * Date parser because java is weird
     * @param date the date
     * @return the parsed date
     */
    public OffsetDateTime dateParser(String date) {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
        return OffsetDateTime.parse(date, inputFormat);
    }
}



