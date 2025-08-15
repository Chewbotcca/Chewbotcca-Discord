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
package pw.chew.chewbotcca.objects.services;

import org.json.JSONObject;
import pw.chew.chewbotcca.util.MiscUtil;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A wrapper for a YouTube video.
 * <p>
 * This class is used in the YouTubeCommand.
 */
public class YouTubeVideo {
    private final JSONObject data;

    /**
     * Constructor
     *
     * @param input A sole video from a search result
     */
    public YouTubeVideo(JSONObject input) {
        data = input;
    }

    public String getId() {
        return data.getString("id");
    }

    public String getURL() {
        return "https://youtu.be/" + getId();
    }

    public String getTitle() {
        return data.getJSONObject("snippet").getString("title");
    }

    public String getViews() {
        if (getStats().has("viewCount")) {
            long views = Long.parseLong(getStats().getString("viewCount"));
            return MiscUtil.delimitNumber(views);
        } else {
            return null;
        }
    }

    public Integer getLikes() {
        if (getStats().has("likeCount")) {
            return Integer.parseInt(getStats().getString("likeCount"));
        } else {
            return null;
        }
    }

    public Integer getDislikes() {
        if (getStats().has("dislikeCount")) {
            return Integer.parseInt(getStats().getString("dislikeCount"));
        } else {
            return null;
        }
    }

    /**
     * Parse the duration as it appears on the website
     * @return The parsed duration
     */
    public String getDuration() {
        if (isLive()) {
            return "Live";
        }

        JSONObject details = data.getJSONObject("contentDetails");
        if (!details.has("duration")) {
            return "Unknown";
        }

        String duration = details.getString("duration");
        List<String> timesTemp = Arrays.asList(duration.replace("PT", "").split("[A-Z]"));
        List<String> times = new ArrayList<>(timesTemp);
        if (times.size() == 1) {
            times.addFirst("0");
        }
        for (int i = 1; i < times.size(); i++) {
            if (times.get(i).length() == 1) {
                times.set(i, "0" + times.get(i));
            }
        }
        return String.join(":", times);
    }

    /**
     * Checks if the video is a live stream.
     * @return true if the video is a live stream, false otherwise
     */
    public boolean isLive() {
        return data.getJSONObject("snippet").getString("liveBroadcastContent").equals("live");
    }

    public OffsetDateTime getUploadDate() {
        return MiscUtil.dateParser(data.getJSONObject("snippet").getString("publishedAt"), "uuuu-MM-dd'T'HH:mm:ssX");
    }

    public JSONObject getThumbnails() {
        return data.getJSONObject("snippet").getJSONObject("thumbnails");
    }

    /**
     * @return If a video is Age-restricted by YouTube
     */
    public boolean isRestricted() {
        return data.getJSONObject("contentDetails")
            .getJSONObject("contentRating")
            .has("ytRating");
    }

    public String getUploaderName() {
        return data.getJSONObject("snippet").getString("channelTitle");
    }

    public String getUploaderURL() {
        return "https://youtube.com/channel/" + data.getJSONObject("snippet").getString("channelId");
    }

    private JSONObject getStats() {
        return data.getJSONObject("statistics");
    }
}
