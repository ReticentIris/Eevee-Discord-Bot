package io.reticent.eevee.provider;

import com.google.common.collect.ImmutableList;
import io.reticent.eevee.provider.model.HSReleaseData;
import io.reticent.eevee.rss.HorribleSubsReleaseReader;
import io.reticent.eevee.rss.model.HorribleSubsReleaseItem;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

@Log4j2
public class HSReleaseDataProvider {
    public static Optional<List<HSReleaseData>> getData() {
        log.debug("Checking for new HS releases.");

        Optional<List<HorribleSubsReleaseItem>> releasesOptional = new HorribleSubsReleaseReader().readFeed();

        if (!releasesOptional.isPresent()) {
            log.debug("Found no HS release items.");
            return Optional.empty();
        }

        List<HorribleSubsReleaseItem> releases = releasesOptional.get();

        log.debug(String.format("Found %s HS release items.", releases.size()));

        return Optional.of(
            releases.stream()
                    .map(HorribleSubsReleaseItem::getTitle)
                    .map(HSReleaseData::fromString)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(ImmutableList.toImmutableList())
        );
    }
}
