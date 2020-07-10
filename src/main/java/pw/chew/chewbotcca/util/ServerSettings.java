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
package pw.chew.chewbotcca.util;

import org.json.JSONObject;

// Server settings
public class ServerSettings {
    final JSONObject data;
    public ServerSettings(JSONObject input) {
        data = input;
    }

    /**
     * Retrieve server info
     * @param id the server id
     * @return a server settings
     */
    public static ServerSettings retrieveServer(String id) {
        JSONObject response = new JSONObject(RestClient.get(
                "https://chew.pw/chewbotcca/discord/server/" + id + "/api/get",
                PropertiesManager.getChewKey()
        ));
        return new ServerSettings(response);
    }

    /**
     * @return the server settings's ID
     */
    public String getId() {
        return String.valueOf(data.getLong("serverid"));
    }
}
