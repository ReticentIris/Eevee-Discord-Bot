package io.reticent.eevee.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reticent.eevee.configuration.GlobalConfiguration;
import io.reticent.eevee.exc.DataRepositoryException;
import io.reticent.eevee.repository.model.Reminder;
import io.reticent.eevee.session.Session;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Log4j2
public class ReminderDataRepository extends DataRepository {
    @JsonProperty
    @Getter
    private final List<Reminder> reminders;

    private ReminderDataRepository() {
        super();
        reminders = new LinkedList<>();
    }

    public static ReminderDataRepository getInstance() throws IOException {
        File dataRepositoryFile = new File(GlobalConfiguration.REMINDER_DATA_REPOSITORY_PATH);

        if (!dataRepositoryFile.exists()) {
            log.info("Could not find existing reminder data repository file. Using new data repository.");
            return new ReminderDataRepository();
        }

        log.info("Loading from existing reminder data repository file.");

        return Session.getObjectMapper().readValue(dataRepositoryFile, ReminderDataRepository.class);
    }

    public synchronized void add(Reminder reminder) throws DataRepositoryException {
        reminders.add(reminder);
        commitAndFlush(GlobalConfiguration.REMINDER_DATA_REPOSITORY_PATH);
    }

    public synchronized void remove(Reminder reminder) throws DataRepositoryException {
        reminders.remove(reminder);
        commitAndFlush(GlobalConfiguration.REMINDER_DATA_REPOSITORY_PATH);
    }
}
