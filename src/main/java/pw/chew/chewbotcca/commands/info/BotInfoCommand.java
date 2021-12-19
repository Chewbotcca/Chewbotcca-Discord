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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONObject;
import pw.chew.chewbotcca.objects.Bot;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.ResponseHelper;
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.jdachewtils.command.OptionHelper;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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
        String botId = OptionHelper.optString(event, "bot", "");

        // Get it from the specified list if it's valid, and let them know
        try {
            switch (OptionHelper.optString(event, "list", "dbots")) {
                case "topgg" -> event.replyEmbeds(gatherTopggInfo(botId, event.getJDA()).build()).queue();
                case "del" -> event.replyEmbeds(gatherDELInfo(botId, event.getJDA()).build()).queue();
                default -> event.replyEmbeds(gatherDBotsInfo(botId, event.getJDA()).build()).queue();
            }
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(ResponseHelper.generateFailureEmbed("Error occurred!", e.getMessage())).setEphemeral(true).queue();
        }
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Send typing, this'll take a while
        commandEvent.getChannel().sendTyping().queue();
        String[] args = commandEvent.getArgs().split(" ");
        // If there's no args
        if (args.length == 0) {
            commandEvent.reply("Please specify a bot with either a mention or its ID");
            return;
        }
        // Parse the bot mention or just use the ID they provided an ID
        String botId;
        if (!commandEvent.getMessage().getMentionedUsers().isEmpty()) {
            botId = commandEvent.getMessage().getMentionedUsers().get(0).getId();
        } else {
            botId = args[0];
        }
        // Default list: dbl, otherwise use what they specify
        String list = "dbl";
        if (args.length > 1) {
            list = args[1].toLowerCase();
        }
        // Get it from the specified list if it's valid, and let them know
        try {
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
        } catch (IllegalArgumentException e) {
            commandEvent.reply(ResponseHelper.generateFailureEmbed("Error occurred!", e.getMessage()));
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
        JSONObject botData = new JSONObject(RestClient.get("https://top.gg/api/bots/" + id, PropertiesManager.getTopggToken()));

        // If there's an error let them know
        if (botData.has("error")) {
            throw new IllegalArgumentException(botData.getString("error"));
        }

        // Start generating the embed
        Bot bot = new Bot(jda);

        // Set Basic info
        bot.setName(botData.getString("username") + "#" + botData.getString("discriminator"));
        bot.setDescription(botData.getString("shortdesc"));
        bot.setAvatar(String.format("https://cdn.discordapp.com/avatars/%s/%s.%s", id, botData.getString("avatar"), "png"));
        bot.setId(id);
        bot.setLibrary(botData.getString("lib"));
        bot.setPrefix(botData.getString("prefix"));
        bot.setServers(botData.optInt("server_count", 0));
        bot.setAddedTime(dateParser(botData.getString("date")));
        bot.setUrl("https://top.gg/bot/" + id);
        bot.addTags(botData.getJSONArray("tags"));
        bot.addOwners(botData.getJSONArray("owners"));

        // Find and set links
        bot.addLink("Bot Page", "https://top.gg/bot/" + id);
        bot.addLink("Vote", "https://top.gg/bot/" + id + "/vote");
        bot.addLink("Invite", botData.getString("invite"));
        bot.addLink("Website", botData.getString("website"));
        bot.addLink("Support Server", "https://discord.gg/" + botData.getString("support"));
        bot.addLink("GitHub", botData.getString("github"));

        EmbedBuilder e = bot.buildEmbed();

        // Set point details
        e.addField("Points", "This Month: " + botData.getInt("monthlyPoints") + "\n" +
            "All Time: " + botData.getInt("points"), true);

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
        JSONObject botData = new JSONObject(RestClient.get("https://discord.bots.gg/api/v1/bots/" + id, PropertiesManager.getDbotsToken()));

        // If there's a message
        if (botData.has("message")) {
            throw new IllegalArgumentException("Error response from the API! " + botData.getString("message"));
        }

        // Start setting embed
        Bot bot = new Bot(jda);

        // Set basic information
        bot.setName(botData.getString("username") + "#" + botData.getString("discriminator"));
        bot.setDescription(botData.getString("shortDescription"));
        bot.setAvatar(botData.getString("avatarURL"));
        bot.setUrl("https://discord.bots.gg/bots/" + id);
        bot.setId(id);
        bot.setServers(botData.getInt("guildCount"));
        bot.setPrefix(botData.getString("prefix"));
        bot.setLibrary(botData.getString("libraryName"));
        bot.setAddedTime(dateParser(botData.getString("addedDate")));
        bot.addOwner(botData.getJSONObject("owner").getString("userId"));

        // Set links
        bot.addLink("Bot Page", "https://discord.bots.gg/bots/" + id);
        bot.addLink("Invite", botData.getString("botInvite"));
        bot.addLink("Website", botData.optString("website", null));
        bot.addLink("Support Server", botData.optString("supportInvite", null));
        bot.addLink("Source Code", botData.optString("openSource", null));

        EmbedBuilder e = bot.buildEmbed();

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
        if (response.getBoolean("error")) {
            throw new IllegalArgumentException("An error occurred getting that bot, does it exist?");
        }

        JSONObject botData = response.getJSONObject("bot");

        // Start generating the embed
        Bot bot = new Bot(jda);

        // Add basic info
        bot.setName(botData.getString("name"));
        bot.setDescription(botData.getString("shortDesc"));
        bot.setAvatar(botData.getJSONObject("avatar").getString("url"));
        bot.setId(botData.getString("id"));
        bot.setServers(botData.getInt("serverCount"));
        bot.setUrl("https://discordextremelist.xyz/en-US/bots/" + id);
        bot.setPrefix(botData.getString("prefix"));
        bot.setLibrary(botData.getString("library"));
        bot.addTags(botData.getJSONArray("tags"));
        bot.addOwner(botData.getJSONObject("owner").getString("id"));
        bot.addOwners(botData.getJSONArray("editors"));

        // Add links
        JSONObject urls = botData.getJSONObject("links");
        bot.addLink("Bot Page", "https://discordextremelist.xyz/en-US/bots/" + id);
        bot.addLink("Invite", urls.getString("invite"));
        bot.addLink("Support Server", urls.getString("support"));
        bot.addLink("Website", urls.getString("website"));
        bot.addLink("Donate", urls.getString("donation"));
        bot.addLink("Source Code", urls.getString("repo"));

        // Build Embed
        EmbedBuilder e = bot.buildEmbed();

        e.setColor(Color.decode("#43B581"));

        return e;
    }

    /**
     * Date parser because java is weird
     *
     * @param date the date
     * @return the parsed date
     */
    public OffsetDateTime dateParser(String date) {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
        return OffsetDateTime.parse(date, inputFormat);
    }
}
