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
import pw.chew.chewbotcca.models.Server;
import pw.chew.chewbotcca.util.DatabaseHelper;

import java.util.HashMap;

// Server settings
public class ServerSettings {
    static final HashMap<String, ServerSettings> cache = new HashMap<>();
    final Server data;
    public ServerSettings(Server input) {
        data = input;
    }

    /**
     * Get a server from the cache, or retrieve it if not set.
     * Can't use getOrDefault() because it always retrieves, even if it's in the cache.
     * @param id the server id
     * @return a Server
     */
    public static ServerSettings getServer(String id) {
        if(cache.containsKey(id)) {
            return cache.get(id);
        } else {
            return retrieveServer(id);
        }
    }

    /**
     * Get a server from the cache, and only the cache.
     * Don't attempt retrieving it.
     * @param id the server ID
     * @return a possibly null server
     */
    public static ServerSettings getServerIfCached(String id) {
        return cache.get(id);
    }

    /**
     * Retrieve server info
     * @param stringId the server id
     * @return a server settings
     */
    public static ServerSettings retrieveServer(String stringId) {
        long id = Long.parseLong(stringId);
        var session = DatabaseHelper.getSessionFactory().openSession();
        Server server = session.find(Server.class, id);
        if (server == null) {
            session.beginTransaction();
            server = new Server();
            server.setId(id);
            session.save(server);
            session.getTransaction().commit();
        }
        session.close();
        ServerSettings settings = new ServerSettings(server);
        cache.put(stringId, settings);
        LoggerFactory.getLogger(ServerSettings.class).debug("Saving " + id + " to Server cache");
        return settings;
    }

    public void saveData(String key, String value) {
        var session = DatabaseHelper.getSessionFactory().openSession();
        session.beginTransaction();
        data.setString(key, value);
        session.update(data);
        session.getTransaction().commit();
        session.close();
        cache.put(getId(), new ServerSettings(data));
        LoggerFactory.getLogger(ServerSettings.class).debug("Setting " + key + " to " + value + " for " + getId());
    }

    /**
     * @return the server's ID
     */
    public String getId() {
        return data.getId().toString();
    }

    /**
     * @return the server's prefix, if there is one
     */
    public String getPrefix() {
        return data.getPrefix();
    }
}
