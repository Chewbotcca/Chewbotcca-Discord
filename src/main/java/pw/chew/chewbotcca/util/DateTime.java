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
/*
    Modified version of https://mkyong.com/java/java-time-elapsed-in-days-hours-minutes-seconds/
 */

package pw.chew.chewbotcca.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Find time difference between 2 date in words
 */
public class DateTime {
    /**
     * Finds the difference of time in words.
     *
     * @param different The time to calculate, in milliseconds
     * @param useSeconds Whether to include seconds in the output
     * @return A string of the difference, e.g. "1 day, 5 hours"
     */
    public static String timeAgo(long different, boolean useSeconds) {
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long yearsInMilli = daysInMilli * 365;

        long elapsedYears = different / yearsInMilli;
        different = different % yearsInMilli;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        ArrayList<String> output = new ArrayList<>();
        if(elapsedYears > 0)
            output.add(elapsedYears + " year" + (elapsedYears > 1 ? "s" : ""));
        if(elapsedDays > 0)
            output.add(elapsedDays + " day" + (elapsedDays > 1 ? "s" : ""));
        if(elapsedHours > 0)
            output.add(elapsedHours + " hour" + (elapsedHours > 1 ? "s" : ""));
        if(elapsedMinutes > 0)
            output.add(elapsedMinutes + " minute" + (elapsedMinutes > 1 ? "s" : ""));
        if(elapsedSeconds > 0 && useSeconds)
            output.add(elapsedSeconds + " second" + (elapsedSeconds > 1 ? "s" : ""));

        StringBuilder response = new StringBuilder();
        for (String module : output) {
            if(response.toString().equals("")) {
                response = new StringBuilder(module);
            } else if(module.equals(output.get(output.size() - 1))) {
                response.append(", and ").append(module);
            } else {
                response.append(", ").append(module);
            }
        }

        return response.toString();
    }

    /**
     * Finds the difference of time in words.
     *
     * @param different The time to calculate, in milliseconds
     * @return A string of the difference, e.g. "1 day, 5 hours"
     */
    public static String timeAgo(long different) {
        return timeAgo(different, true);
    }

    /**
     * Finds the difference of time in words.
     *
     * @param time The time to compare against now ran as now minus this
     * @return A string of the difference, e.g. "1 day, 5 hours"
     */
    public static String timeAgoFromNow(Instant time) {
        long diff = Instant.now().toEpochMilli() - time.toEpochMilli();
        return timeAgo(diff, true);
    }

    public static String timeAgoShort(long different, boolean useSeconds) {
        String timeAgo = timeAgo(different, useSeconds);
        String[] parts = timeAgo.replace(" and", "").split(", ");
        List<String> response = new ArrayList<>();
        for (String part : parts) {
            String[] values = part.split(" ");
            response.add(values[0] + "" + values[1].charAt(0));
        }
        return String.join(" ", response);
    }

    public static String timeAgoShort(Instant time, boolean useSeconds) {
        long diff = Instant.now().toEpochMilli() - time.toEpochMilli();
        return timeAgoShort(diff, useSeconds);
    }
}
