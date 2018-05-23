package io.reticent.eevee.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.reticent.eevee.provider.MongoClientProvider;
import io.reticent.eevee.repository.model.Reminder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Log4j2
public class ReminderDataRepository extends DataRepository {
    @Getter
    private final List<Reminder> reminders;
    private final MongoCollection<Reminder> MONGO_COLLECTION;

    private ReminderDataRepository() {
        super();
        reminders = new LinkedList<>();
        MongoClient mongoClient = MongoClientProvider.getInstance();

        MONGO_COLLECTION = mongoClient.getDatabase("eevee")
                                      .getCollection("reminders", Reminder.class);

        MONGO_COLLECTION.find().forEach((Consumer<Reminder>) reminders::add);
    }

    public static ReminderDataRepository getInstance() {
        return new ReminderDataRepository();
    }

    public void add(Reminder reminder) {
        reminders.add(reminder);
        MONGO_COLLECTION.insertOne(reminder);
    }

    public void remove(Reminder reminder) {
        reminders.remove(reminder);
        MONGO_COLLECTION.deleteOne(
            and(
                eq("userId", reminder.getUserId()),
                eq("remindAt", reminder.getRemindAt()),
                eq("reminder", reminder.getReminder())
            )
        );
    }
}
