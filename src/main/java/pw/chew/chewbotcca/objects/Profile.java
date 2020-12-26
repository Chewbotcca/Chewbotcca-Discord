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
package pw.chew.chewbotcca.objects;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;

// Bot Profile class
public class Profile {
    static final HashMap<String, Profile> cache = new HashMap<>();
    final JSONObject data;
    public Profile(JSONObject input) {
        data = input;
    }

    /**
     * Get a profile from the cache, or retrieve it if not set.
     * Can't use getOrDefault() because it always retrieves, even if it's in the cache.
     * @param id the user id
     * @return a Profile
     */
    public static Profile getProfile(String id) {
        if(cache.containsKey(id)) {
            return cache.get(id);
        } else {
            return retrieveProfile(id);
        }
    }

    /**
     * Retrieves a profile
     * @param id the user id
     * @return a Profile
     */
    public static Profile retrieveProfile(String id) {
        JSONObject response = new JSONObject(RestClient.get(
                "https://chew.pw/chewbotcca/discord/api/profile/" + id,
                PropertiesManager.getChewKey()
        ));
        cache.put(id, new Profile(response));
        LoggerFactory.getLogger(Profile.class).debug("Saving " + id + " to Profile cache");
        return new Profile(response);
    }

    public void saveData(String key, String value) {
        HashMap<String, Object> inputMap = new HashMap<>();
        inputMap.put(key, value);
        JSONObject response = new JSONObject(
                RestClient.post(
                        "https://chew.pw/chewbotcca/discord/api/profile/" + getId(),
                        inputMap,
                        PropertiesManager.getChewKey()
                )
        );
        cache.put(getId(), new Profile(response));
        LoggerFactory.getLogger(Profile.class).debug("Saving " + getId() + " to Profile cache");
    }

    /**
     * @return their ID
     */
    public String getId() {
        return String.valueOf(data.getLong("userid"));
    }

    /**
     * The user's last.fm username, used for %^lastfm
     * @return their last.fm username
     */
    public String getLastFm() {
        if(data.isNull("lastfm"))
            return null;
        return data.getString("lastfm");
    }

    /**
     * The user's GitHub username, used for %^ghuser
     * @return their GitHub username
     */
    public String getGitHub() {
        if(data.isNull("github"))
            return null;
        return data.getString("github");
    }

    public void delete() {
        RestClient.delete(
            "https://chew.pw/chewbotcca/discord/api/profile/" + getId(),
            PropertiesManager.getChewKey()
        );
        cache.remove(getId());
    }
}
