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

import java.util.ArrayList;

// Find time difference between 2 date in words
public class DateTime {
    static boolean useSeconds = true;

    public static String timeAgo(long different) {

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
        if(elapsedYears > 1)
            output.add(elapsedYears + " years");
        else if(elapsedYears > 0)
            output.add(elapsedYears + " year");
        if(elapsedDays > 1)
            output.add(elapsedDays + " days");
        else if(elapsedDays > 0)
            output.add(elapsedDays + " days");
        if(elapsedHours > 1)
            output.add(elapsedHours + " hours");
        else if(elapsedHours > 0)
            output.add(elapsedHours + " hours");
        if(elapsedMinutes > 1)
            output.add(elapsedMinutes + " minutes");
        else if(elapsedMinutes > 0)
            output.add(elapsedMinutes + " minutes");
        if(elapsedSeconds > 1 && useSeconds)
            output.add(elapsedSeconds + " seconds");
        else if(elapsedSeconds > 0 && useSeconds)
            output.add(elapsedSeconds + " seconds");

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

    public static String timeAgo(long different, boolean seconds) {
        useSeconds = seconds;
        String timeago = timeAgo(different);
        useSeconds = true;
        return timeago;
    }
}
