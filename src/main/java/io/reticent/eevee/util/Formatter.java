package io.reticent.eevee.util;

import lombok.NonNull;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    public static String formatPermissions(Permission[] permissions) {
        if (permissions.length == 0) {
            return "None";
        }
        
        return Arrays.stream(permissions).map(Permission::getName).collect(Collectors.joining(", "));
    }

    public static String formatTwitterUser(String user) {
        if (!user.startsWith("@")) {
            return String.format("@%s", user);
        }

        return user;
    }
}
