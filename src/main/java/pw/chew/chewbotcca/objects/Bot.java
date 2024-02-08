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
package pw.chew.chewbotcca.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import pw.chew.chewbotcca.util.MiscUtil;

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the skeleton of a bot with all information related to it.<br>
 * Used in the {@link pw.chew.chewbotcca.commands.info.BotInfoCommand BotInfo command}.
 */
public class Bot {
    private String name, description, library, prefix, avatar, url, id;
    private int servers;
    private final List<String> owners = new ArrayList<>();
    private final List<String> links = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private TemporalAccessor addedTime;
    private final JDA jda;

    /**
     * Construct the bot with JDA, so we can retrieve owners if needed
     *
     * @param jda the JDA object
     */
    public Bot(JDA jda) {
        this.jda = jda;
    }

    /**
     * Set this bot's name
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the description of the bot, used in the embed description
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the library this bot is built with
     *
     * @param library the library
     */
    public void setLibrary(String library) {
        this.library = library;
    }

    /**
     * The prefix used to invoke this bot's commands
     *
     * @param prefix the prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * The avatar URL (complete) of this bot
     *
     * @param avatar the avatar URL
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * The URL to view this bot on a specified list
     *
     * @param url the bot page url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The ID of this bot
     *
     * @param id the ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the amount of servers this bot is on.
     * If this number is 0 or less, it is considered "Unknown"
     *
     * @param servers the server count
     */
    public void setServers(int servers) {
        this.servers = servers;
    }

    /**
     * Add an owner of the bot by their ID
     *
     * @param ownerId the owner ID
     */
    public void addOwner(String ownerId) {
        User user = jda.retrieveUserById(ownerId).complete();
        this.owners.add(MiscUtil.getTag(user));
    }

    /**
     * Add multiple owners of the bot, based on a JSONArray of owner ID Strings
     *
     * @param owners the owners
     */
    public void addOwners(JSONArray owners) {
        for (Object owner : owners) {
            addOwner((String) owner);
        }
    }

    /**
     * Add a single tag to be displayed in the embed
     *
     * @param tag the tag
     */
    public void addTag(String tag) {
        this.tags.add(tag);
    }

    /**
     * Add multiple tags to this bot, based on a JSONArray of tag strings
     *
     * @param tags the tags
     */
    public void addTags(JSONArray tags) {
        for (Object tag : tags) {
            addTag((String) tag);
        }
    }

    /**
     * Add a link to the links list.<br>
     * If {@code url} is null or blank, it won't be added, so you can pass these to avoid the hassle of checking yourself.
     *
     * @param title the displayed string
     * @param url   the url to link to
     */
    public void addLink(String title, String url) {
        if (url == null || url.isBlank()) return;
        this.links.add(String.format("[%s](%s)", title, url));
    }

    /**
     * Set the time the bot was added to the list
     *
     * @param time the added time
     */
    public void setAddedTime(TemporalAccessor time) {
        this.addedTime = time;
    }

    /**
     * Construct an EmbedBuilder of all the data we've been provided.<br>
     * If you want to add your own info, feel free! This is a builder for a reason.
     *
     * @return the compiled embed.
     */
    public EmbedBuilder buildEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bot Information for " + name, url);
        embed.setDescription(description);
        embed.setThumbnail(avatar);
        embed.addField("Bot ID", id, true);
        if (servers > 0) {
            embed.addField("Server Count", servers + "", true);
        } else {
            embed.addField("Server Count", "Unknown", true);
        }
        embed.addField("Prefix", "`" + prefix + "`", true);
        embed.addField("Library", library, true);
        embed.addField("Owner", String.join("\n", owners), true);
        embed.addField("Links", String.join("\n", links), true);
        if (!tags.isEmpty()) {
            embed.addField("Tags", String.join(", ", tags), true);
        }
        if (addedTime != null) {
            embed.setFooter("Bot added");
            embed.setTimestamp(addedTime);
        }

        return embed;
    }
}