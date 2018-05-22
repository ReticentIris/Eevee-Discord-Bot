package io.reticent.eevee.util;

import lombok.NonNull;

import java.time.Duration;

public class TimeUtil {
    public static String durationToDDHHMMSS(@NonNull Duration duration) {
        int durationSeconds = (int) duration.getSeconds();

        int days = durationSeconds / (24 * 3600);
        int hours = (durationSeconds - (days * 24 * 3600)) / 3600;
        int minutes = (durationSeconds - (days * 24 * 3600) - (hours * 3600)) / 60;
        int seconds = durationSeconds - (days * 24 * 3600) - (hours * 3600) - (minutes * 60);

        return String.format("%s days %s hours %s minutes %s seconds", days, hours, minutes, seconds);
    }

    public static long dhmsToMilli(double days, double hours, double minutes, double seconds) {
        return (long) ((days * 24 * 60 * 60 * 1000) + (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000));
    }
}
