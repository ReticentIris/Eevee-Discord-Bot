package io.reticent.eevee.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.exc.DataRepositoryException;
import io.reticent.eevee.repository.model.HSReleaseAnnouncement;
import io.reticent.eevee.session.Session;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Log4j2
public class HSReleaseAnnouncementDataRepository extends DataRepository {
    @JsonProperty
    @Getter
    private final List<HSReleaseAnnouncement> announcements;

    private HSReleaseAnnouncementDataRepository() {
        super();
        announcements = new LinkedList<>();
    }

    public static HSReleaseAnnouncementDataRepository getInstance() throws IOException {
        File dataRepositoryFile = new File(GlobalConfiguration.HS_RELEASE_NOTIFICATION_DATA_REPOSITORY_PATH);

        if (!dataRepositoryFile.exists()) {
            log.info("Could not find existing reminder data repository file. Using new data repository.");
            return new HSReleaseAnnouncementDataRepository();
        }

        log.info("Loading from existing reminder data repository file.");

        return Session.getObjectMapper().readValue(dataRepositoryFile, HSReleaseAnnouncementDataRepository.class);
    }

    public synchronized void add(HSReleaseAnnouncement notification) throws DataRepositoryException {
        announcements.add(notification);
        commitAndFlush(GlobalConfiguration.HS_RELEASE_NOTIFICATION_DATA_REPOSITORY_PATH);
    }

    public synchronized void remove(HSReleaseAnnouncement notification) throws DataRepositoryException {
        announcements.remove(notification);
        commitAndFlush(GlobalConfiguration.HS_RELEASE_NOTIFICATION_DATA_REPOSITORY_PATH);
    }
}
