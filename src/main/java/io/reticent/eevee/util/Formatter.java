package io.reticent.eevee.util;

import net.dv8tion.jda.core.entities.User;

public class Formatter {
    public static String formatTag(User user) {
        return String.format("%s#%s", user.getName(), user.getDiscriminator());
    }
}
