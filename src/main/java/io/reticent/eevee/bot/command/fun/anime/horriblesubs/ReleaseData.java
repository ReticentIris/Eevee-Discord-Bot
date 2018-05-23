package io.reticent.eevee.bot.command.fun.anime.horriblesubs;

import io.reticent.eevee.session.Session;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
@Value
@Log4j2
class ReleaseData {
    private static final Pattern DETAIL_EXTRACTION_PATTERN = Pattern.compile(Session.getConfiguration().readString("animeReleaseDetailExtractionRegex"));

    @NonNull
    private String subber;
    @NonNull
    private String title;
    private int episode;
    @NonNull
    private String quality;
    @NonNull
    private String format;

    public static ReleaseData fromString(String str) {
        Matcher matcher = DETAIL_EXTRACTION_PATTERN.matcher(str);

        if (!matcher.matches()) {
            log.error(String.format("No match found for string: %s.", str));
            return null;
        }

        return ReleaseData.builder()
                          .subber(matcher.group(1))
                          .title(matcher.group(2))
                          .episode(Integer.parseInt(matcher.group(3)))
                          .quality(matcher.group(6))
                          .format(matcher.group(10).toUpperCase())
                          .build();
    }

    public String toString() {
        return String.format(
            "[%s] %s - %s (%s) | %s", subber, title, episode, quality, format
        );
    }
}
