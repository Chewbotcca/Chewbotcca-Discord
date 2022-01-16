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
package pw.chew.chewbotcca.objects.services;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * A wrapper for the discord.bio user response
 */
public class DBioUser {
    private final JSONObject values;

    public DBioUser(JSONObject input) {
        values = input.getJSONObject("payload");
    }

    /**
     * Gets a user from discord.bio
     *
     * @param id The Discord User ID
     * @return A user
     */
    public static DBioUser getUser(String id) {
        try {
            JSONObject response = new JSONObject(RestClient.get("https://api.discord.bio/user/details/" + id));
            return response.has("message") ? null : new DBioUser(response);
        } catch (JSONException e) {
            // Could not parse JSON
            return null;
        }
    }

    /**
     * Gets a user's specific detail
     *
     * @return A specific detail, if it exists
     */
    @Nullable
    public String get(String detail) {
        return getUserDetails().optString(detail, null);
    }

    /**
     * @return This user's Gender
     */
    public String getGender() {
        if (getUserDetails().isNull("gender")) return "Undisclosed";

        return new String[]{"Male", "Female", "Non-Binary", "Undisclosed"}[getUserDetails().getInt("gender")];
    }

    /**
     * @return This user's birthday as DoY, Month Day, Year.
     */
    @Nullable
    public String getBirthday() {
        if (getUserDetails().isNull("birthday")) return null;

        String[] date = getUserDetails().getString("birthday").split("-");
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]), 0, 0, 0);

        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMMM dd, yyyy");
        return outputFormat.format(calendar.getTime());
    }

    /**
     * @return Get user details
     */
    private JSONObject getUserDetails() {
        return values.getJSONObject("user").getJSONObject("details");
    }
}
