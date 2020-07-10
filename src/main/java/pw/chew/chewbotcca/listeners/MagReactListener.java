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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.commands.github.GHIssueCommand;
import pw.chew.chewbotcca.commands.google.YouTubeCommand;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.OffsetDateTime;

// Listen to 🔍 reactions
public class MagReactListener extends ListenerAdapter {
    // Listen for all reactions
    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        // Ignore if it's not a mac
        if(!event.getReactionEmote().getName().equals("\uD83D\uDD0D")) {
            return;
        }
        // Get the message id
        String id = event.getMessageId();
        // Retrieve the message
        event.getChannel().retrieveMessageById(id).queue((msg) -> {
            String content = msg.getContentStripped().replace(">", "");
            if(content.contains("youtube.com") || content.contains("youtu.be")) {
                // If it's a YouTube video
                handleYouTube(content, event, msg);
            } else if(content.contains("github.com") && (content.contains("/issues") || content.contains("/pull"))) {
                // If it's a github issue or pr
                handleGitHub(content, event, msg);
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
        // Ignore if message >= 15 minutes
        if(OffsetDateTime.now().toInstant().toEpochMilli() - msg.getTimeCreated().toInstant().toEpochMilli() >= 15*60*1000) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Message older than 15 minutes, not describing!");
            return;
        }
        // Ignore if already described to avoid spam
        if(YouTubeCommand.didDescribe(msg.getId())) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Already described this message!");
            return;
        }
        // Mark it as described
        YouTubeCommand.described(msg.getId());
        // Get the video
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + video + "&key=" + PropertiesManager.getGoogleKey() + "&part=snippet,contentDetails,statistics"));
        // make a YouTube video embed response
        event.getChannel().sendMessage(new YouTubeCommand().response(url, video).build()).queue();
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
        // Ignore if message >= 15 minutes old
        if(OffsetDateTime.now().toInstant().toEpochMilli() - msg.getTimeCreated().toInstant().toEpochMilli() >= 15*60*1000) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Message older than 15 minutes, not describing!");
            return;
        }
        // Ignore if already described to avoid spam
        if(GHIssueCommand.didDescribe(msg.getId())) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Already described this message!");
            return;
        }
        // Ignore if described
        GHIssueCommand.described(msg.getId());
        // Initialize GitHub and the response
        GitHub github;
        try {
            github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        GHIssue ghIssue;
        try {
            ghIssue = github.getRepository(repo).getIssue(issue);
        } catch (IOException e) {
            return;
        }
        event.getChannel().sendMessage(new GHIssueCommand().issueBuilder(ghIssue, repo, github, issue).build()).queue();
    }
}
