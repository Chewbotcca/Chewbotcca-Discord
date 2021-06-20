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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// %^binfo command
public class BotInfoCommand extends SlashCommand {
    public BotInfoCommand() {
        this.name = "botinfo";
        this.help = "Finds info on a specified bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.aliases = new String[]{"binfo"};
        this.guildOnly = false;
        this.options = Arrays.asList(
            new OptionData(OptionType.USER, "bot", "The bot to look up").setRequired(true),
            new OptionData(OptionType.STRING, "list", "The list to look for, default: discord.bots.gg")
                .addChoices(
                    new Command.Choice("discord.bots.gg", "dbots"),
                    new Command.Choice("top.gg", "topgg"),
                    new Command.Choice("discordextremelist.xyz", "del")
                )
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String botId = event.getOption("bot").getAsUser().getId();

        // Get it from the specified list if it's valid, and let them know
        switch (ResponseHelper.guaranteeStringOption(event, "list", "dbots")) {
            case "topgg" -> event.replyEmbeds(gatherTopggInfo(botId, event.getJDA()).build()).queue();
            case "del" -> event.replyEmbeds(gatherDELInfo(botId, event.getJDA()).build()).queue();
            default -> event.replyEmbeds(gatherDBotsInfo(botId, event.getJDA()).build()).queue();
        }
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
            case "dbl", "top.gg", "topgg" -> commandEvent.reply(gatherTopggInfo(botId, commandEvent.getJDA()).build());
            case "dbots" -> commandEvent.reply(gatherDBotsInfo(botId, commandEvent.getJDA()).build());
            case "del" -> commandEvent.reply(gatherDELInfo(botId, commandEvent.getJDA()).build());
            default -> commandEvent.reply("""
                Invalid Bot List! Supported lists:
                `dbots` -> <https://discord.bots.gg>
                `topgg` -> <https://top.gg>
                `del` -> <https://discordextremelist.xyz>
                
                Got one to suggest? Open an issue on GitHub or suggest one with `%^feedback`!""".replace("%^", commandEvent.getPrefix()));
        }
    }

    /**
     * Gather info from top.gg
     * @param id the bot id
     * @param jda JDA for user lookup
     * @return an embed ready to be build
     */
    private EmbedBuilder gatherTopggInfo(String id, JDA jda) {
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

        // Set the thumbnail to the bot avatar
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
            User user = jda.getUserById((String) owner);
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
     * @param jda JDA for user lookup
     * @return an embed ready to be build
     */
    private EmbedBuilder gatherDBotsInfo(String id, JDA jda) {
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
        User user = jda.getUserById(bot.getJSONObject("owner").getString("userId"));
        if(user == null)
            e.addField("Owner", "Unknown", true);
        else
            e.addField("Owner", user.getAsTag(), true);

        // Set links
        List<CharSequence> links = new ArrayList<>();
        links.add("[Bot Page](https://discord.bots.gg/bots/" + id + ")");
        if(!bot.getString("botInvite").equals(""))
            links.add("[Invite](" + bot.getString("botInvite") + ")");
        if(!bot.isNull("website"))
            links.add("[Website](" + bot.getString("website") + ")");
        if(!bot.isNull("supportInvite"))
            links.add("[Support Server](" + bot.getString("supportInvite") + ")");
        if(!bot.isNull("openSource"))
            links.add("[Source Code](" + bot.getString("openSource") + ")");

        e.addField("Links", String.join("\n", links), true);

        e.setFooter("Bot added");
        e.setTimestamp(dateParser(bot.getString("addedDate")));

        e.setColor(Color.decode("#43B581"));

        return e;
    }

    /**
     * Gathers bot info from DiscordExtremeList
     * @param id bot ID
     * @param jda JDA for user lookup
     * @return an embed
     */
    public EmbedBuilder gatherDELInfo(String id, JDA jda) {
        // Gather info from the site
        JSONObject response = new JSONObject(RestClient.get("https://api.discordextremelist.xyz/v2/bot/" + id, PropertiesManager.getDELToken()));

        // If there's an error let them know
        if(response.getBoolean("error")) {
            return new EmbedBuilder().setTitle("Error!").setDescription("An error occurred getting that bot, does it exist");
        }

        JSONObject bot = response.getJSONObject("bot");

        // Start generating the embed
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Bot Information");

        // Set the author to the bot name
        e.setAuthor(bot.getString("name"), "https://discordextremelist.xyz/en-US/bots/" + id, bot.getJSONObject("avatar").getString("url"));

        // Set the thumbnail to the bot avatar
        e.setThumbnail(bot.getJSONObject("avatar").getString("url"));

        // Set the description to the short description
        e.setDescription(bot.getString("shortDesc"));

        e.addField("Bot ID", bot.getString("id"), true);

        // Set server count if there is one
        if(bot.getInt("serverCount") > 0)
            e.addField("Server Count", String.valueOf(bot.getInt("serverCount")), true);
        else
            e.addField("Server Count", "Unknown", true);

        // Set other details
        e.addField("Prefix", "`" + bot.getString("prefix") + "`", true);
        e.addField("Library", bot.getString("library"), true);

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
        owners.add(bot.getJSONObject("owner").getString("id"));
        for(Object owner : bot.getJSONArray("editors")) {
            User user = jda.getUserById((String) owner);
            if(user == null)
                owners.add((String) owner);
            else
                owners.add(user.getAsTag());
        }

        e.addField("Owners", String.join(", ", owners), true);

        // Find and set links
        List<CharSequence> links = new ArrayList<>();
        links.add("[Bot Page](https://discordextremelist.xyz/en-US/bots/" + id + ")");
        JSONObject urls = bot.getJSONObject("links");
        if(!urls.getString("invite").equals(""))
            links.add("[Invite](" + urls.getString("invite") + ")");
        if(!urls.getString("support").equals(""))
            links.add("[Support Server](" + urls.getString("support") + ")");
        if(!urls.getString("website").equals(""))
            links.add("[Website](" + urls.getString("website") + ")");
        if(!urls.getString("donation").equals(""))
            links.add("[Donate](" + urls.getString("donation") + ")");
        if(!urls.getString("repo").equals(""))
            links.add("[Source](" + urls.getString("repo") + ")");

        e.addField("Links", String.join("\n", links), true);

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
