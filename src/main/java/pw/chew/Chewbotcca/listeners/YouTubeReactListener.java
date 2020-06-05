package pw.chew.Chewbotcca.listeners;

import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pw.chew.Chewbotcca.Main;
import pw.chew.Chewbotcca.commands.google.YouTubeCommand;
import pw.chew.Chewbotcca.util.RestClient;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

public class YouTubeReactListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if(!event.getReactionEmote().getName().equals("\uD83D\uDD0D")) {
            return;
        }
        String id = event.getMessageId();
        event.getChannel().retrieveMessageById(id).queue((msg) -> {
            if(OffsetDateTime.now().toInstant().toEpochMilli() - msg.getTimeCreated().toInstant().toEpochMilli() >= 15*60*1000) {
                LoggerFactory.getLogger(YouTubeReactListener.class).debug("Message older than 15 minutes, not describing!");
                return;
            }
            if(YouTubeCommand.didDescribe(msg.getId())) {
                LoggerFactory.getLogger(YouTubeReactListener.class).debug("Already described this message!");
                return;
            }
            YouTubeCommand.described(msg.getId());
            String content = msg.getContentRaw();
            String video;
            if(content.contains("youtube.com")) {
                video = content.split("=")[1];
            } else if (content.contains("youtu.be")) {
                video = content.split("/")[content.split("/").length - 1];
            } else {
                return;
            }
            JSONObject url = new JSONObject(RestClient.get("https://www.googleapis.com/youtube/v3/videos?id=" + video + "&key=" + Main.getProp().getProperty("google") + "&part=snippet,contentDetails,statistics"));
            event.getChannel().sendMessage(new YouTubeCommand().response(url, video).build()).queue();
        });

    }
}
