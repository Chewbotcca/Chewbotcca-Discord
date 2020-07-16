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
import pw.chew.chewbotcca.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

// Bot Profile class
public class Profile {
    final JSONObject data;
    public Profile(JSONObject input) {
        data = input;
    }

    /**
     * Retrieves a profile
     * @param id the user id
     * @return a Profile
     */
    public static Profile retrieveProfile(String id) {
        JSONObject response = new JSONObject(RestClient.get(
                "https://chew.pw/chewbotcca/discord/profile/" + id + "/api/get",
                PropertiesManager.getChewKey()
        ));
        return new Profile(response);
    }

    /**
     * @return their ID
     */
    public String getId() {
        return String.valueOf(data.getLong("userid"));
    }
}
