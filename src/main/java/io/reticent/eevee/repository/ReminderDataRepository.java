package io.reticent.eevee.repository;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.reticent.eevee.provider.MongoClientProvider;
import io.reticent.eevee.repository.model.Reminder;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Log4j2
public class ReminderDataRepository extends DataRepository {
    private static ReminderDataRepository reminderDataRepository;
    private final MongoCollection<Reminder> MONGO_COLLECTION;

    private final String DATABASE_NAME = Session.getConfiguration().readString("mongoDatabaseName");
    private final String COLLECTION_NAME = Session.getConfiguration().readString("mongoReminderCollectionName");

    private ReminderDataRepository() {
        super();
        MongoClient mongoClient = MongoClientProvider.getInstance();
        MONGO_COLLECTION = mongoClient.getDatabase(DATABASE_NAME)
                                      .getCollection(COLLECTION_NAME, Reminder.class);
    }

    public static ReminderDataRepository getInstance() {
        if (reminderDataRepository == null) {
            reminderDataRepository = new ReminderDataRepository();
        }

        return reminderDataRepository;
    }

    public List<Reminder> getReminders() {
        return ImmutableList.copyOf(MONGO_COLLECTION.find());
    }

    public void add(Reminder reminder) {
        MONGO_COLLECTION.insertOne(reminder);
    }

    public void remove(Reminder reminder) {
        MONGO_COLLECTION.deleteOne(
            and(
                eq("userId", reminder.getUserId()),
                eq("remindAt", reminder.getRemindAt()),
                eq("reminder", reminder.getReminder())
            )
        );
    }
}
