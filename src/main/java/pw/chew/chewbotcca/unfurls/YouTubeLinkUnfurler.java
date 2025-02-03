/*
 * Copyright (C) 2025 Chewbotcca
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
import pw.chew.chewbotcca.util.ResponseHelper;

public class YouTubeLinkUnfurler implements GenericUnfurler {
    public boolean checkLink(String link) {
        return link.contains("youtube.com") || link.contains("youtu.be");
    }

    @Override
    public @Nullable MessageEmbed unfurl(String link) {
        return unfurlVideo(link);
    }

    public MessageEmbed unfurlVideo(String link) {
        String id = YouTubeCommand.extractVideoId(link);

        // If one couldn't be found for whatever reason
        if (id == null)
            return null;

        // Get the video
        try {
            YouTubeVideo youTubeVideo = YouTubeCommand.getVideoById(id);

            // make a YouTube video embed response
            return YouTubeCommand.buildVideoEmbed(youTubeVideo).build();
        } catch (IllegalArgumentException e) {
            return ResponseHelper.generateFailureEmbed("Video not found", "The video could not be found. Please try again.");
        }
    }
}
