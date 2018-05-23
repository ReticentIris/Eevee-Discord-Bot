package io.reticent.eevee.provider.model;

import io.reticent.eevee.session.Session;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
@Log4j2
public class HSReleaseData {
    private static final Pattern DETAIL_EXTRACTION_PATTERN = Pattern.compile(Session.getConfiguration().readString("animeReleaseDetailExtractionRegex"));
    private static Matcher detailMatcher = null;

    @Getter
    @NonNull
    private String subber;
    @Getter
    @NonNull
    private String title;
    @Getter
    private int episode;
    @Getter
    @NonNull
    private String quality;
    @Getter
    @NonNull
    private String format;

    public static Optional<HSReleaseData> fromString(String str) {
        if (detailMatcher == null) {
            detailMatcher = DETAIL_EXTRACTION_PATTERN.matcher(str);
        }

        detailMatcher.reset(str);

        if (!detailMatcher.matches()) {
            log.error(String.format("No match found for string: %s.", str));
            return Optional.empty();
        }

        return Optional.of(HSReleaseData.builder()
                                        .subber(detailMatcher.group(1))
                                        .title(detailMatcher.group(2))
                                        .episode(Integer.parseInt(detailMatcher.group(3)))
                                        .quality(detailMatcher.group(6))
                                        .format(detailMatcher.group(10).toUpperCase())
                                        .build());
    }

    public String toString() {
        return String.format(
            "[%s] %s [Ep. %s | %s | %s]", subber, title, episode, quality, format
        );
    }
}
