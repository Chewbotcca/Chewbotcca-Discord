/*
 * Copyright (C) 2021 Chewbotcca
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

import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.models.Profile;
import pw.chew.chewbotcca.util.DatabaseHelper;

import java.util.HashMap;

// Bot Profile class
public class UserProfile {
    static final HashMap<String, UserProfile> cache = new HashMap<>();
    final Profile data;
    public UserProfile(Profile input) {
        data = input;
    }

    /**
     * Get a profile from the cache, or retrieve it if not set.
     * Can't use getOrDefault() because it always retrieves, even if it's in the cache.
     * @param id the user id
     * @return a Profile
     */
    public static UserProfile getProfile(String id) {
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
    public static UserProfile retrieveProfile(String id) {
        DatabaseHelper.openConnectionIfClosed();
        Profile user = Profile.findOrCreateIt("userid", id);
        if (!user.exists()) {
            user.insert();
        }
        UserProfile profile = new UserProfile(user);
        cache.put(id, profile);
        LoggerFactory.getLogger(UserProfile.class).debug("Saving " + id + " to Profile cache");
        return profile;
    }

    public void saveData(String key, String value) {
        DatabaseHelper.openConnectionIfClosed();
        data.setString(key, value).saveIt();
        cache.put(getId(), new UserProfile(data));
        LoggerFactory.getLogger(UserProfile.class).debug("Setting " + key + " to " + value + " for " + getId());
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
        if(data.get("lastfm") == null)
            return null;
        return data.getString("lastfm");
    }

    /**
     * The user's GitHub username, used for %^ghuser
     * @return their GitHub username
     */
    public String getGitHub() {
        if(data.get("github") == null)
            return null;
        return data.getString("github");
    }

    public void delete() {
        DatabaseHelper.openConnectionIfClosed();
        String id = getId();
        data.delete();
        cache.remove(id);
        LoggerFactory.getLogger(UserProfile.class).debug("Removing from profile cache for " + id);
    }
}
