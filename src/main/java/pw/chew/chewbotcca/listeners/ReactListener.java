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
package pw.chew.chewbotcca.listeners;

import me.memerator.api.client.entities.Meme;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.commands.minecraft.MCIssueCommand;
import pw.chew.chewbotcca.commands.services.MemeratorCommand;
import pw.chew.chewbotcca.commands.services.github.GHIssueCommand;
import pw.chew.chewbotcca.commands.services.google.YouTubeCommand;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.objects.services.YouTubeVideo;
import pw.chew.chewbotcca.util.RestClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static pw.chew.chewbotcca.commands.services.MemeratorCommand.MemeratorMemeSubCommand.generateMemeEmbed;
import static pw.chew.chewbotcca.commands.services.MemeratorCommand.MemeratorUserSubCommand.generateUserEmbed;

// Listen to reactions
public class ReactListener extends ListenerAdapter {
    private static final List<String> describedIds = new ArrayList<>();

    // Listen for all reactions
    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        // Only handle messages from servers
        if (!event.isFromGuild()) return;

        // Handle if it's a 🔍 reaction
        if (event.getEmoji().getName().equals("\uD83D\uDD0D") || event.getEmoji().getName().equals("\uD83D\uDD0E")) {
            handleMagReaction(event);
        }
    }

    public void handleMagReaction(@Nonnull MessageReactionAddEvent event) {
        // Get the message id
        String id = event.getMessageId();
        // Ignore if already described to avoid spam
        if (didDescribe(id)) {
            LoggerFactory.getLogger(ReactListener.class).debug("Already described this message!");
            return;
        }

        // Retrieve the message
        event.getChannel().retrieveMessageById(id).queue((msg) -> {
            // Ignore if message >= 15 minutes old
            if (OffsetDateTime.now().toInstant().toEpochMilli() - msg.getTimeCreated().toInstant().toEpochMilli() >= 15 * 60 * 1000) {
                LoggerFactory.getLogger(ReactListener.class).debug("Message older than 15 minutes, not describing!");
                return;
            }

            MessageEmbed embed = unfurlMessage(msg);
            if (embed != null) {
                msg.replyEmbeds(embed).mentionRepliedUser(false).queue();

                // Mark the message as described
                described(msg.getId());
            }
        });
    }

    /**
     * Returns an Embed of the unfurled message
     *
     * @param msg The message to be unfurled
     * @return an embed of the message
     */
    @Nullable
    public static MessageEmbed unfurlMessage(Message msg) {
        String content = msg.getContentStripped().replace(">", "");

        // Find all the links in the message
        List<String> validLinks = new ArrayList<>();
        final Pattern urlPattern = Pattern.compile("(https?://\\S+)");
        for (String link : content.split(" ")) {
            if (urlPattern.matcher(link).matches()) {
                validLinks.add(link);
            }
        }

        if (validLinks.isEmpty())
            return null;

        // Get the first link
        String link = validLinks.get(0);

        if (link.contains("youtube.com") || link.contains("youtu.be")) {
            // If it's a YouTube video
            return handleYouTube(link);
        } else if (link.contains("github.com") && (link.contains("/issues") || link.contains("/pull"))) {
            // If it's a GitHub issue or pr
            return handleGitHub(link);
        } else if (link.contains("bugs.mojang.com") || link.contains("hub.spigotmc.org/jira")) {
            // If it's a Mojira/Spigot jira link
            return handleMcIssue(link);
        } else if (link.contains("memerator.me/m")) {
            // If it's a Memerator meme
            return handleMemeratorMeme(link);
        } else if (link.contains("memerator.me/p")) {
            // If it's a Memerator user
            return handleMemeratorUser(link);
        }

        return null;
    }

    /**
     * Handle a YouTube video message
     *
     * @param link the link to the YouTube video
     */
    public static MessageEmbed handleYouTube(String link) {
        // Find the video ID
        String video = null;

        if (link.contains("youtube.com")) {
            video = link.split("=")[1];
        } else if (link.contains("youtu.be")) {
            video = link.split("/")[link.split("/").length - 1];
        }

        // If one couldn't be found for whatever reason
        if (video == null)
            return null;

        // Get the video
        YouTubeVideo youTubeVideo = YouTubeCommand.getVideo(video);

        // make a YouTube video embed response
        return YouTubeCommand.buildVideoEmbed(youTubeVideo).build();
    }

    /**
     * Handle a GitHub.com link
     *
     * @param content the message content
     */
    public static MessageEmbed handleGitHub(String content) {
        // Example: https://github.com/Chewbotcca/Discord/issues/1
        // => "https:" "" "github.com" "Chewbotcca" "Discord" "issues" "1"
        String[] url = content.split("/");
        String repo = url[3] + "/" + url[4];
        // Get the issue num
        int issue = Integer.parseInt(url[6]);

        // Initialize GitHub and the response
        GitHub github = Memory.getGithub();
        GHIssue ghIssue;
        try {
            ghIssue = github.getRepository(repo).getIssue(issue);
        } catch (IOException e) {
            return null;
        }
        return GHIssueCommand.issueBuilder(ghIssue).build();
    }

    /**
     * Handle a Mojira / Spigot JIRA link
     *
     * @param content the message content
     */
    public static MessageEmbed handleMcIssue(String content) {
        // Get PROJECT-NUM from URL
        String[] url = content.split("/");
        String issue = url[url.length - 1];

        // Ensure we actually track this
        String apiUrl = MCIssueCommand.getApiUrl(issue.split("-")[0]);
        if (apiUrl == null)
            return null;
        // Get response
        JSONObject data = new JSONObject(RestClient.get(apiUrl + issue));
        // Initialize GitHub and the response
        return MCIssueCommand.generateEmbed(data, issue, apiUrl).build();
    }

    /**
     * Handles a Memerator Meme Link
     *
     * @param content the message content
     */
    public static MessageEmbed handleMemeratorMeme(String content) {
        String id = content.split("/")[content.split("/").length - 1];
        if (!id.toLowerCase().matches("([a-f]|\\d){6,7}")) {
            return null;
        }

        // Get the meme
        Meme meme = MemeratorCommand.MemeratorMemeSubCommand.getMeme(id, true);

        // If it's null, return null
        if (meme == null)
            return null;

        return generateMemeEmbed(meme).build();
    }

    /**
     * Handle a Memerator User Link
     *
     * @param link the message content
     * @return An embed if the user exists, null otherwise
     */
    public static MessageEmbed handleMemeratorUser(String link) {
        String name = link.split("/")[link.split("/").length - 1];

        // Get the user
        var user = MemeratorCommand.getAPI().retrieveUser(name).complete();

        // If the user doesn't exist, return null
        if (user == null)
            return null;

        // Return the user embed
        return generateUserEmbed(user).build();
    }

    /**
     * Queries whether a specific message ID has been described
     *
     * @param id the message id
     * @return if it was described
     */
    public static boolean didDescribe(String id) {
        return describedIds.contains(id);
    }

    /**
     * Mark a message ID as "described"
     *
     * @param id the message ID
     */
    public static void described(String id) {
        describedIds.add(id);
    }
}
