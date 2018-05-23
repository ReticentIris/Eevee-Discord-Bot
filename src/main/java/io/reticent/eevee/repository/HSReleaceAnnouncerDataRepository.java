package io.reticent.eevee.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.reticent.eevee.provider.MongoClientProvider;
import io.reticent.eevee.repository.model.HSReleaseAnnouncer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

@Log4j2
public class HSReleaceAnnouncerDataRepository extends DataRepository {
    @Getter
    private final List<HSReleaseAnnouncer> announcers;
    private final MongoCollection<HSReleaseAnnouncer> MONGO_COLLECTION;

    private HSReleaceAnnouncerDataRepository() {
        super();
        announcers = new LinkedList<>();
        MongoClient mongoClient = MongoClientProvider.getInstance();

        MONGO_COLLECTION = mongoClient.getDatabase("eevee")
                                      .getCollection("hsReleaseAnnouncements", HSReleaseAnnouncer.class);

        MONGO_COLLECTION.find().forEach((Consumer<HSReleaseAnnouncer>) announcers::add);
    }

    public static HSReleaceAnnouncerDataRepository getInstance() {
        return new HSReleaceAnnouncerDataRepository();
    }

    public void add(HSReleaseAnnouncer announcer) {
        announcers.add(announcer);
        MONGO_COLLECTION.insertOne(announcer);
    }

    public void update(HSReleaseAnnouncer announcer) {
        MONGO_COLLECTION.replaceOne(eq("announcerId", announcer.getAnnouncerId()), announcer);
    }

    public void remove(HSReleaseAnnouncer announcer) {
        announcers.remove(announcer);
        MONGO_COLLECTION.deleteOne(eq("announcerId", announcer.getAnnouncerId()));
    }
}
