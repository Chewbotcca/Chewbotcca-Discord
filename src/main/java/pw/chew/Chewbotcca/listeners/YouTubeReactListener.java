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

public class YouTubeReactListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        LoggerFactory.getLogger(YouTubeReactListener.class).debug("GuildMessageReactAddEvent received: " + event.getReactionEmote().getName());
        if(!event.getReactionEmote().getName().equals("\uD83D\uDD0D")) {
            return;
        }
        String id = event.getMessageId();
        event.getChannel().retrieveMessageById(id).queue((msg) -> {
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
