/*
 * Copyright (C) 2023 Chewbotcca
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

package pw.chew.chewbotcca.unfurls;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import pw.chew.chewbotcca.commands.services.google.YouTubeCommand;
import pw.chew.chewbotcca.objects.services.YouTubeVideo;

public class YouTubeLinkUnfurler implements GenericUnfurler {
    public boolean checkLink(String link) {
        return link.contains("youtube.com") || link.contains("youtu.be");
    }

    @Override
    public @Nullable MessageEmbed unfurl(String link) {
        return unfurlVideo(link);
    }

    public MessageEmbed unfurlVideo(String link) {
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
}
