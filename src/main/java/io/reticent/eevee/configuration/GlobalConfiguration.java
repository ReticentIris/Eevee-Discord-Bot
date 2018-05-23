package io.reticent.eevee.configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GlobalConfiguration {
    public static final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;
    public static final String CONFIGURATION_PATH = "conf/Eevee.json";
    public static final String REMINDER_DATA_REPOSITORY_PATH = "data/reminders.json";
    public static final String HS_RELEASE_NOTIFICATION_DATA_REPOSITORY_PATH = "data/hs_release_notifications.json";
    public static final String HORRIBLE_SUBS_RELEASE_FEED_RUL = "http://horriblesubs.info/rss.php?res=all";
}
