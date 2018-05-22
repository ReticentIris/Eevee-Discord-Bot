package io.reticent.eevee.util;

import lombok.NonNull;
import net.dv8tion.jda.core.entities.User;

public class Formatter {
    public static String formatTag(@NonNull User user) {
        return String.format("%s#%s", user.getName(), user.getDiscriminator());
    }

    public static String formatBoolean(boolean bool) {
        return bool ? "Yes" : "No";
    }

    public static String formatRateLimit(RateLimiter rateLimiter) {
        if (rateLimiter == null) {
            return "None";
        }

        return String.format("%s every %s seconds", rateLimiter.getMaxHits(), rateLimiter.getDuration() / 1000);
    }
}
