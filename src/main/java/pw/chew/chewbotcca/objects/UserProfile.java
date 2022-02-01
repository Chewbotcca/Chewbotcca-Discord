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
        var session = DatabaseHelper.getSessionFactory().openSession();
        Profile user = session.find(Profile.class, id);
        if (user == null) {
            session.beginTransaction();
            user = new Profile();
            user.setId(id);
            session.save(user);
            session.getTransaction().commit();
        }
        session.close();
        UserProfile profile = new UserProfile(user);
        cache.put(id, profile);
        LoggerFactory.getLogger(UserProfile.class).debug("Saving " + id + " to Profile cache");
        return profile;
    }

    public void saveData(String key, String value) {
        var session = DatabaseHelper.getSessionFactory().openSession();
        session.beginTransaction();
        data.setString(key, value);
        session.update(data);
        session.getTransaction().commit();
        session.close();
        cache.put(getId(), new UserProfile(data));
        LoggerFactory.getLogger(UserProfile.class).debug("Setting " + key + " to " + value + " for " + getId());
    }

    /**
     * @return their ID
     */
    public String getId() {
        return data.getId();
    }

    /**
     * The user's last.fm username, used for %^lastfm
     * @return their last.fm username
     */
    public String getLastFm() {
        return data.getLastfm();
    }

    /**
     * The user's GitHub username, used for %^ghuser
     * @return their GitHub username
     */
    public String getGitHub() {
        return data.getGithub();
    }

    public void delete() {
        String id = getId();
        var session = DatabaseHelper.getSessionFactory().openSession();
        session.beginTransaction();
        session.delete(data);
        session.getTransaction().commit();
        session.close();
        cache.remove(id);
        LoggerFactory.getLogger(UserProfile.class).debug("Removing from profile cache for " + id);
    }
}
