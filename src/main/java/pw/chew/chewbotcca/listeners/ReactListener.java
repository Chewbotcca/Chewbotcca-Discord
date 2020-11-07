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
package pw.chew.chewbotcca.listeners;

import me.memerator.api.errors.NotFound;
import me.memerator.api.object.Meme;
import me.memerator.api.object.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.commands.fun.MemeratorCommand;
import pw.chew.chewbotcca.commands.github.GHIssueCommand;
import pw.chew.chewbotcca.commands.google.YouTubeCommand;
import pw.chew.chewbotcca.commands.minecraft.MCIssueCommand;
import pw.chew.chewbotcca.objects.Memory;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static pw.chew.chewbotcca.commands.fun.MemeratorCommand.MemeratorMemeSubCommand.generateMemeEmbed;
import static pw.chew.chewbotcca.commands.fun.MemeratorCommand.MemeratorUserSubCommand.generateUserEmbed;

// Listen to reactions
public class ReactListener extends ListenerAdapter {
    private static final List<String> describedIds = new ArrayList<>();

    // Listen for all reactions
    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        // Handle if it's a 🔍 reaction
        if(event.getReactionEmote().getName().equals("\uD83D\uDD0D") || event.getReactionEmote().getName().equals("\uD83D\uDD0E")) {
            handleMagReaction(event);
        }
    }

    public void handleMagReaction(@Nonnull GuildMessageReactionAddEvent event) {
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
            if(OffsetDateTime.now().toInstant().toEpochMilli() - msg.getTimeCreated().toInstant().toEpochMilli() >= 15*60*1000) {
                LoggerFactory.getLogger(ReactListener.class).debug("Message older than 15 minutes, not describing!");
                return;
            }

            String content = msg.getContentStripped().replace(">", "");
            if(content.contains("youtube.com") || content.contains("youtu.be")) {
                // If it's a YouTube video
                handleYouTube(content, event, msg);
            } else if(content.contains("github.com") && (content.contains("/issues") || content.contains("/pull"))) {
                // If it's a github issue or pr
                handleGitHub(content, event, msg);
            } else if(content.contains("bugs.mojang.com") || content.contains("hub.spigotmc.org/jira")) {
                // If it's a mojira/spigot jira link
                handleMcIssue(content, event, msg);
            } else if(content.contains("memerator.me/m")) {
                // If it's a Memerator meme
                handleMemeratorMeme(content, event, msg);
            } else if(content.contains("memerator.me/p")) {
                // If it's a Memerator user
                handleMemeratorUser(content, event, msg);
            }
        });
    }

    /**
     * Handle a YouTube video message
     * @param content the message content
     * @param event the reaction event
     * @param msg the message itself
     */
    public void handleYouTube(String content, GuildMessageReactionAddEvent event, Message msg) {
        // Find the video ID
        String video = null;
        for(String query : content.split(" ")) {
            if (query.contains("youtube.com")) {
                video = query.split("=")[1];
            } else if (query.contains("youtu.be")) {
                video = query.split("/")[query.split("/").length - 1];
            }
        }
        // If one couldn't be found for whatever reason
        if(video == null)
            return;
        // Mark it as described
        described(msg.getId());
        // Get the video
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + video + "&key=" + PropertiesManager.getGoogleKey() + "&part=snippet,contentDetails,statistics"));
        // make a YouTube video embed response
        event.getChannel().sendMessage(new YouTubeCommand().response(url, video, event.getChannel()).build()).queue();
    }

    /**
     * Handle a Github.com link
     * @param content the message content
     * @param event the reaction event
     * @param msg the message itself
     */
    public void handleGitHub(String content, GuildMessageReactionAddEvent event, Message msg) {
        // Example: https://github.com/Chewbotcca/Discord/issues/1
        // => "https:" "" "github.com" "Chewbotcca" "Discord" "issues" "1"
        String[] url = content.split("/");
        String repo = url[3] + "/" + url[4];
        // Get the issue num
        int issue = Integer.parseInt(url[6]);
        // Ignore if described
        described(msg.getId());
        // Initialize GitHub and the response
        GitHub github = Memory.getGithub();
        GHIssue ghIssue;
        try {
            ghIssue = github.getRepository(repo).getIssue(issue);
        } catch (IOException e) {
            return;
        }
        event.getChannel().sendMessage(new GHIssueCommand().issueBuilder(ghIssue).build()).queue();
    }

    /**
     * Handle a Mojira / Spigot JIRA link
     * @param content the message content
     * @param event the reaction event
     * @param msg the message itself
     */
    public void handleMcIssue(String content, GuildMessageReactionAddEvent event, Message msg) {
        // Get PROJECT-NUM from URL
        String[] url = content.split("/");
        String issue = url[url.length - 1];
        // Ignore if described
        described(msg.getId());
        // Ensure we actually track this
        String apiUrl = MCIssueCommand.getApiUrl(issue.split("-")[0]);
        if(apiUrl == null)
            return;
        // Get response
        JSONObject data = new JSONObject(RestClient.get(apiUrl + issue));
        // Initialize GitHub and the response
        event.getChannel().sendMessage(MCIssueCommand.generateEmbed(data, issue, apiUrl).build()).queue();
    }

    /**
     * Handles a Memerator Meme Link
     * @param content the message content
     * @param event the reaction event
     * @param msg the message itself
     */
    public void handleMemeratorMeme(String content, GuildMessageReactionAddEvent event, Message msg) {
        String id = content.split("/")[content.split("/").length - 1];
        if (!id.toLowerCase().matches("([a-f]|[0-9]){6,7}")) {
            return;
        }
        // Ignore if described
        described(msg.getId());
        // Get the meme
        Meme meme = MemeratorCommand.MemeratorMemeSubCommand.getMeme(id, true);
        if (meme == null) {
            return;
        }
        event.getChannel().sendMessage(generateMemeEmbed(meme).build()).queue();
        msg.suppressEmbeds(true).queue();
    }

    public void handleMemeratorUser(String content, GuildMessageReactionAddEvent event, Message msg) {
        String name = content.split("/")[content.split("/").length - 1];
        // Ignore if described
        described(msg.getId());
        // Get the user
        User user;
        try {
            user = MemeratorCommand.getAPI().getUser(name);
        } catch (NotFound notFound) {
            return;
        }
        event.getChannel().sendMessage(generateUserEmbed(user).build()).queue();
        msg.suppressEmbeds(true).queue();
    }

    /**
     * Queries whether a specific message ID has been described
     * @param id the message id
     * @return if it was described
     */
    public static boolean didDescribe(String id) {
        return describedIds.contains(id);
    }

    /**
     * Mark a message ID as "described"
     * @param id the message ID
     */
    public static void described(String id) {
        describedIds.add(id);
    }
}
