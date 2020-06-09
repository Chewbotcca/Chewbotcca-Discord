/*
    Modified version of https://mkyong.com/java/java-time-elapsed-in-days-hours-minutes-seconds/
 */

package pw.chew.Chewbotcca.util;

import java.util.ArrayList;

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
        if(elapsedYears > 0)
            output.add(elapsedYears + " years");
        if(elapsedDays > 0)
            output.add(elapsedDays + " days");
        if(elapsedHours > 0)
            output.add(elapsedHours + " hours");
        if(elapsedMinutes > 0)
            output.add(elapsedMinutes + " minutes");
        if(elapsedSeconds > 0 && useSeconds)
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
