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

package pw.chew.chewbotcca.commands.minecraft;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.internal.utils.Checks;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.JDAUtilUtil;
import pw.chew.chewbotcca.util.MiscUtil;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WynncraftCommand extends SlashCommand {
    private final static Map<String, JSONObject> characterCache = new HashMap<>();
    private final static Map<String, JSONObject> playerCache = new HashMap<>();
    private final static Map<String, JSONObject> guildCache = new HashMap<>();

    public WynncraftCommand() {
        this.name = "wynncraft";
        this.help = "Finds player or guild stats for Wynncraft";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "query", "The player or guild to search for", true, true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String query = event.optString("query", "");

        if (query.isBlank()) {
            event.reply("You must provide a player or guild to search for").setEphemeral(true).queue();
            return;
        }

        // Check for p: or g:
        String type = "player";
        if (query.startsWith("p:")) {
            query = query.substring(2);
        } else if (query.startsWith("g:")) {
            query = query.substring(2);
            type = "guild";
        } else {
            // We have to assume 1 word is a player and 2+ words is a guild
            if (query.contains(" ")) {
                type = "guild";
            }
        }

        // URL Encode the query
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        query = query.replace("+", "%20");

        // Make the data request
        JSONObject data = RestClient.get("https://api.wynncraft.com/v3/" + type + "/" + query).asJSONObject();

        if (type.equals("player")) {
            playerCache.put(query, data);
            event.replyEmbeds(buildPlayerEmbed(data).build())
                .addActionRow(
                    Button.primary("wynn:chars:" + query, "View Characters"),
                    Button.secondary("wynn:refresh:player:%s".formatted(query), "Refresh")
                ).queue();
        } else {
            guildCache.put(query, data);
            event.replyEmbeds(buildGuildEmbed(data).build())
                .addActionRow(
                    Button.primary("wynn:guild:" + query + ":members", "View Members")
                    // TODO: Implement this
                    // Button.secondary("wynn:refresh:guild:%s".formatted(query), "Refresh")
                ).queue();
        }
    }

    public static void handleGuildMembers(ButtonInteractionEvent event, String guild) {
        JSONObject data = pullFromCache("guild", guild);

        JSONObject members = data.getJSONObject("members");

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("[%s] %s's Members".formatted(data.getString("prefix"), data.getString("name")));

        for (String rank : members.keySet()) {
            if (rank.equals("total")) {
                continue;
            }

            JSONObject rankMembers = members.getJSONObject(rank);

            List<String> memberNames = new ArrayList<>();
            for (String member : rankMembers.keySet()) {
                memberNames.add("%s".formatted(member));
            }

            embed.addField(MiscUtil.capitalize(rank), String.join("\n", memberNames), true);
        }

        // order fields like: "Owner", "Chief", "Strategist", "Captain", "Recruiter", "Recruit"
        List<String> ranks = List.of("Owner", "Chief", "Strategist", "Captain", "Recruiter", "Recruit");

        embed.getFields().sort((a, b) -> {
            // shush intellij
            Checks.notNull(a.getName(), "A");
            Checks.notNull(b.getName(), "B");

            int aRank = ranks.indexOf(a.getName());
            int bRank = ranks.indexOf(b.getName());

            return Integer.compare(aRank, bRank);
        });

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    /**
     * Handles when the "Home" or "Back" button is pressed in the player character/s menu.
     *
     * @param event  the event
     * @param player the player's name
     */
    public static void handleMainPlayerPageButton(ButtonInteractionEvent event, String player) {
        // Make the data request
        JSONObject data = pullFromCache("player", player);

        event.editMessageEmbeds(buildPlayerEmbed(data).build())
            .setComponents(ActionRow.of(
                Button.primary("wynn:chars:" + player, "View Characters"),
                Button.secondary("wynn:refresh:player:%s".formatted(player), "Refresh")
            ))
            .queue();
    }

    /**
     * Handles when the "View Characters" button is pressed.
     *
     * @param event  the event
     * @param player the player's name
     */
    public static void handleCharactersButton(ButtonInteractionEvent event, String player) {
        JSONObject data = RestClient.get("https://api.wynncraft.com/v3/player/" + player + "/characters").asJSONObject();

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(player + "'s Characters")
            .setDescription("Select a character to view stats for using the select menu below.");

        // Build Selection List.
        StringSelectMenu.Builder menu = StringSelectMenu.create("wynn:char:" + player)
            .setPlaceholder("Select a character");

        for (String key : data.keySet()) {
            JSONObject charData = data.getJSONObject(key);

            String name = "%s [Lvl. %s]".formatted(MiscUtil.capitalize(charData.getString("type")), charData.getInt("level"));
            String description = "Total Levels: %s".formatted(charData.getInt("totalLevel"));

            menu.addOption(name, key, description);
        }

        menu.getOptions().sort((a, b) -> {
            // Convert "Name [Lvl. 1]" to just 1, aka only want the level

            int aName = MiscUtil.asInt(a.getLabel().split("Lvl. ")[1].replace("]", ""));
            int bName = MiscUtil.asInt(b.getLabel().split("Lvl. ")[1].replace("]", ""));

            return -Integer.compare(aName, bName);
        });

        event.editMessageEmbeds(embed.build()).setComponents(
            ActionRow.of(Button.primary("wynn:main:" + player, "Back")),
            ActionRow.of(menu.build())
        ).queue();
    }

    /**
     * Handles when a select menu option is selected for a character.
     *
     * @param event   the event
     * @param player  the player's name
     */
    public static void handleCharacterSelection(StringSelectInteractionEvent event, String player) {
        SelectOption option = event.getSelectedOptions().get(0);

        JSONObject data = RestClient.get("https://api.wynncraft.com/v3/player/%s/characters/%s".formatted(player, option.getValue())).asJSONObject();
        characterCache.put(option.getValue(), data);

        EmbedBuilder embed = buildCharacterEmbed(data, player);

        String baseId = "wynn:char:%s:%s:".formatted(player, option.getValue());

        event.editMessageEmbeds(embed.build())
            .setComponents(
                ActionRow.of(
                    Button.primary("wynn:main:" + player, "Home"),
                    Button.secondary("wynn:refresh:char:%s:%s".formatted(player, option.getValue()), "Refresh")
                ),
                ActionRow.of(event.getComponent()),
                ActionRow.of(
                    Button.primary(baseId + "main", "General Stats"),
                    Button.primary(baseId + "prof", "Profession Stats"),
                    Button.primary(baseId + "dung", "Dungeon Stats")
                    // TODO: Add quests later...
                    //Button.primary(baseId + "quests", "Completed Quests")
                )
            ).queue();
    }

    /**
     * Shows general stats for a character.
     *
     * @param event  the event
     * @param player the player's name
     * @param charId the character's id
     */
    public static void handleGeneralStats(ButtonInteractionEvent event, String player, String charId) {
        JSONObject data = pullFromCache("character", player, charId);

        event.editMessageEmbeds(buildCharacterEmbed(data, player).build()).queue();
    }

    /**
     * Handles showing profession stats for a character.
     *
     * @param event  the event
     * @param player the player's name
     * @param charId the character's id
     */
    public static void handleProfessionStats(ButtonInteractionEvent event, String player, String charId) {
        JSONObject data = pullFromCache("character", player, charId);

        JSONObject professions = data.getJSONObject("professions");

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(player + "'s Profession Stats");

        for (String key : professions.keySet()) {
            JSONObject prof = professions.getJSONObject(key);
            if (key.equals("combat")) {
                continue;
            }

            embed.addField(MiscUtil.capitalize(key), "Level: %s\nExp: %s".formatted(prof.getInt("level"), prof.getInt("xpPercent")) + "%", true);
        }

        // Sort fields by level
        embed.getFields().sort((a, b) -> {
            // shush intellij
            Checks.notNull(a.getValue(), "A");
            Checks.notNull(b.getValue(), "B");

            int aLevel = MiscUtil.asInt(a.getValue().split("\n")[0].split("Level: ")[1]);
            int bLevel = MiscUtil.asInt(b.getValue().split("\n")[0].split("Level: ")[1]);

            return -Integer.compare(aLevel, bLevel);
        });

        event.editMessageEmbeds(embed.build()).queue();
    }

    /**
     * Handles showing dungeon stats for a character.
     *
     * @param event  the event
     * @param player the player's name
     * @param charId the character's id
     */
    public static void handleDungeonStats(ButtonInteractionEvent event, String player, String charId) {
        JSONObject data = pullFromCache("character", player, charId);

        JSONObject dungeons = data.getJSONObject("dungeons");
        JSONObject list = dungeons.getJSONObject("list");

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(player + "'s Dungeon Stats");

        for (String key : list.keySet()) {
            int completed = list.getInt(key);

            embed.addField(key, "Completed: %s".formatted(completed), true);
        }

        event.editMessageEmbeds(embed.build()).queue();
    }

    public static void handleQuestStats(ButtonInteractionEvent event, String player, String charId) {
        JSONObject data = pullFromCache("character", player, charId);

        JSONObject quests = data.getJSONObject("quests");
        JSONArray list = quests.getJSONArray("list");

        String[] questList = MiscUtil.toList(list, String.class).toArray(String[]::new);

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(player + "'s Completed Quests");

        var page = JDAUtilUtil.makePaginator();
        page.addItems(questList);

        Paginator paginator = page.build();

        event.replyEmbeds(embed.build()).setEphemeral(true).queue(paginator::display);
    }

    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String query = event.getFocusedOption().getValue();

        if (query.isBlank()) {
            event.replyChoices(Collections.emptyList()).queue();
            return;
        }

        // Strip any Player: or Guild:
        query = query.replaceFirst("^(Player|Guild):\\s*", "");

        // URL Encode the query. E,g, a space turns into %20
        query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        query = query.replace("+", "%20");

        // Make the search request
        JSONObject res = RestClient.get("https://api.wynncraft.com/v3/search/" + query).asJSONObject();

        JSONObject players = res.optJSONObject("players", new JSONObject());
        JSONObject guilds = res.optJSONObject("guilds", new JSONObject());

        List<Command.Choice> choices = new ArrayList<>();

        for (String key : players.keySet()) {
            String player = players.getString(key);
            choices.add(new Command.Choice("Player: " + player, "p:" + player));
        }

        for (String key : guilds.keySet()) {
            String guild = guilds.getJSONObject(key).getString("name");
            choices.add(new Command.Choice("Guild: " + guild, "g:" + guild));
        }

        // Limit to 25 unique choices
        choices = choices.stream().distinct().limit(25).toList();

        event.replyChoices(choices).queue();
    }

    private static EmbedBuilder buildPlayerEmbed(JSONObject meta) {
        String lastSeen = TimeFormat.DATE_TIME_LONG.format(MiscUtil.dateParser(meta.getString("lastJoin"), "uuuu-MM-dd'T'HH:mm:ss.SSSSSSX"));
        if (meta.getBoolean("online")) {
            lastSeen = "Online on " + meta.getJSONObject("location").getString("server");
        }

        JSONObject globalData = meta.getJSONObject("globalData");

        return new EmbedBuilder()
            .setTitle(meta.getString("username") + "'s Wynncraft Stats")
            .setDescription("Viewing global stats for %s".formatted(meta.getString("username")))
            .setColor(Color.decode(meta.getJSONObject("legacyRankColour").getString("main")))
            .setThumbnail("https://visage.surgeplay.com/bust/350/%s".formatted(meta.getString("uuid")))
            .addField("Total Level", globalData.getInt("totalLevel") + "", true)
            .addField("Mobs Killed", globalData.getInt("killedMobs") + "", true)
            .addField("Playtime", meta.getDouble("playtime") + " hours", true)
            .addField("Dates",
                "First Joined: " + TimeFormat.DATE_TIME_LONG.format(MiscUtil.dateParser(meta.getString("firstJoin"), "uuuu-MM-dd'T'HH:mm:ssX"))
                    + "\n" +
                    "Last Seen: " + lastSeen,
                true);
    }

    private static EmbedBuilder buildCharacterEmbed(JSONObject data, String character) {
        List<String> skills = new ArrayList<>();
        JSONObject skillObj = data.getJSONObject("skillPoints");
        for (String skill : skillObj.keySet()) {
            if (skill.equals("defence")) continue;

            skills.add("%s: %s".formatted(MiscUtil.capitalize(skill), skillObj.getInt(skill)));
        }

        return new EmbedBuilder()
            .setTitle(character + "'s " + MiscUtil.capitalize(data.getString("type") + " class"))
            .setThumbnail("https://cdn.wynncraft.com/nextgen/classes/picture/%s.webp".formatted(data.getString("type").toLowerCase(Locale.ROOT)))
            .setDescription("# General Statistics\n" +
                "Total Levels: %s\n".formatted(data.getInt("totalLevel")) +
                "Combat Level: %s\n".formatted(data.getInt("level")) +
                "Time Played: %s hours\n".formatted(data.getDouble("playtime")) +
                "Discoveries: %s\n".formatted(data.getInt("discoveries")) +
                "Logins: %s\n".formatted(data.getInt("logins")) +
                "Deaths: %s\n".formatted(data.getInt("deaths")) +
                "Mobs Killed: %s\n".formatted(data.getInt("mobsKilled")) +
                "Completed Dungeons: %s\n".formatted(data.optJSONObject("dungeons", new JSONObject().put("total", 0)).getInt("total")) +
                "Completed Raids: %s".formatted(data.optJSONObject("raids", new JSONObject().put("total", 0)).getInt("total"))
            ).addField("Skills", String.join("\n", skills), true);
    }

    private static EmbedBuilder buildGuildEmbed(JSONObject data) {
        return new EmbedBuilder()
            .setTitle("[%s] %s".formatted(data.getString("prefix"), data.getString("name")))
            // field for level, showing "Lvl. 5 (45%)" <-- we need to add the percent sign ourselves
            .addField("Level", "Lvl. %s (%s%s)".formatted(data.getInt("level"), data.getInt("xpPercent"), "%"), true)
            .addField("Online Members", "%s / %s".formatted(data.getInt("online"), data.getJSONObject("members").getInt("total")), true)
            .addField("Territories", data.getInt("territories") + "", true)
            .addField("Created", TimeFormat.DATE_TIME_LONG.format(MiscUtil.dateParser(data.getString("created"), "uuuu-MM-dd'T'HH:mm:ss.SSSSSSX")), false)
            ;
    }

    public static JSONObject pullFromCache(String type, String... key) {
        if (type.equals("player")) {
            JSONObject pl = playerCache.get(key[0]);
            if (pl == null) {
                pl = RestClient.get("https://api.wynncraft.com/v3/player/%s".formatted((Object[]) key)).asJSONObject();
                playerCache.put(key[0], pl);
            }
            return pl;
        } else if (type.equals("guild")) {
            JSONObject gu = guildCache.get(key[0]);
            if (gu == null) {
                gu = RestClient.get("https://api.wynncraft.com/v3/guild/%s".formatted((Object[]) key)).asJSONObject();
                guildCache.put(key[0], gu);
            }
            return gu;
        } else {
            JSONObject ch = characterCache.get(key[1]);
            if (ch == null) {
                ch = RestClient.get("https://api.wynncraft.com/v3/player/%s/characters/%s".formatted((Object[]) key)).asJSONObject();
                characterCache.put(key[1], ch);
            }
            return ch;
        }
    }

    public static void clearCache(String type, String key, ButtonInteractionEvent event) {
        if (type.equals("player")) {
            playerCache.remove(key);
        } else if (type.equals("guild")) {
            guildCache.remove(key);
        } else {
            characterCache.remove(key);
        }

        var embed = event.getMessage().getEmbeds().get(0);
        Checks.notNull(embed, "embed");
        Checks.notNull(embed.getTitle(), "embed title");
        Checks.notNull(embed.getDescription(), "embed description");

        String[] parts = event.getComponentId().split(":");

        if (embed.getTitle().contains("Wynncraft Stats")) {
            WynncraftCommand.handleMainPlayerPageButton(event, parts[3]);
        } else if (embed.getDescription().contains("General Statistics")) {
            WynncraftCommand.handleGeneralStats(event, parts[3], parts[4]);
        } else if (embed.getTitle().contains("Profession")) {
            WynncraftCommand.handleProfessionStats(event, parts[3], parts[4]);
        } else if (embed.getTitle().contains("Dungeon")) {
            WynncraftCommand.handleDungeonStats(event, parts[3], parts[4]);
        }
    }
}
