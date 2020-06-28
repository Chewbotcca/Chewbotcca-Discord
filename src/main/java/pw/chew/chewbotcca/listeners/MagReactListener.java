package pw.chew.chewbotcca.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.Main;
import pw.chew.chewbotcca.commands.github.GHIssueCommand;
import pw.chew.chewbotcca.commands.google.YouTubeCommand;
import pw.chew.chewbotcca.util.RestClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.OffsetDateTime;

public class MagReactListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if(!event.getReactionEmote().getName().equals("\uD83D\uDD0D")) {
            return;
        }
        String id = event.getMessageId();
        event.getChannel().retrieveMessageById(id).queue((msg) -> {
            String content = msg.getContentStripped().replace(">", "");
            if(content.contains("youtube.com") || content.contains("youtu.be")) {
                handleYouTube(content, event, msg);
            } else if(content.contains("github.com") && (content.contains("/issues") || content.contains("/pull"))) {
                handleGitHub(content, event, msg);
            } else {
            }
        });

    }

    public void handleYouTube(String content, GuildMessageReactionAddEvent event, Message msg) {
        String video = null;
        for(String query : content.split(" ")) {
            if (query.contains("youtube.com")) {
                video = query.split("=")[1];
            } else if (query.contains("youtu.be")) {
                video = query.split("/")[query.split("/").length - 1];
            }
        }
        if(video == null)
            return;
        if(OffsetDateTime.now().toInstant().toEpochMilli() - msg.getTimeCreated().toInstant().toEpochMilli() >= 15*60*1000) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Message older than 15 minutes, not describing!");
            return;
        }
        if(YouTubeCommand.didDescribe(msg.getId())) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Already described this message!");
            return;
        }
        YouTubeCommand.described(msg.getId());
        JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + video + "&key=" + Main.getProp().getProperty("google") + "&part=snippet,contentDetails,statistics"));
        event.getChannel().sendMessage(new YouTubeCommand().response(url, video).build()).queue();
    }

    public void handleGitHub(String content, GuildMessageReactionAddEvent event, Message msg) {
        // Example: https://github.com/Chewbotcca/Discord/issues/1
        // => "https:" "" "github.com" "Chewbotcca" "Discord" "issues" "1"
        String[] url = content.split("/");
        String repo = url[3] + "/" + url[4];
        int issue = Integer.parseInt(url[6]);
        if(OffsetDateTime.now().toInstant().toEpochMilli() - msg.getTimeCreated().toInstant().toEpochMilli() >= 15*60*1000) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Message older than 15 minutes, not describing!");
            return;
        }
        if(GHIssueCommand.didDescribe(msg.getId())) {
            LoggerFactory.getLogger(MagReactListener.class).debug("Already described this message!");
            return;
        }
        GHIssueCommand.described(msg.getId());
        GitHub github;
        try {
            github = new GitHubBuilder().withOAuthToken(Main.getProp().getProperty("github")).build();
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
