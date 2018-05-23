package io.reticent.eevee.repository;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.reticent.eevee.provider.MongoClientProvider;
import io.reticent.eevee.repository.model.HSReleaseAnnouncer;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Log4j2
public class HSReleaseAnnouncerDataRepository extends DataRepository {
    private static HSReleaseAnnouncerDataRepository hsReleaseAnnouncerDataRepository;
    private final MongoCollection<HSReleaseAnnouncer> MONGO_COLLECTION;

    private final String DATABASE_NAME = Session.getConfiguration().readString("mongoDatabaseName");
    private final String COLLECTION_NAME = Session.getConfiguration().readString("mongoHSReleaseAnnouncerCollectionName");

    private HSReleaseAnnouncerDataRepository() {
        super();
        MongoClient mongoClient = MongoClientProvider.getInstance();
        MONGO_COLLECTION = mongoClient.getDatabase(DATABASE_NAME)
                                      .getCollection(COLLECTION_NAME, HSReleaseAnnouncer.class);
    }

    public static HSReleaseAnnouncerDataRepository getInstance() {
        if (hsReleaseAnnouncerDataRepository == null) {
            hsReleaseAnnouncerDataRepository = new HSReleaseAnnouncerDataRepository();
        }

        return hsReleaseAnnouncerDataRepository;
    }

    public List<HSReleaseAnnouncer> getAnnouncers() {
        return ImmutableList.copyOf(MONGO_COLLECTION.find());
    }

    public void add(HSReleaseAnnouncer announcer) {
        MONGO_COLLECTION.insertOne(announcer);
    }

    public void update(HSReleaseAnnouncer announcer) {
        MONGO_COLLECTION.replaceOne(eq("announcerId", announcer.getAnnouncerId()), announcer);
    }

    public void remove(HSReleaseAnnouncer announcer) {
        MONGO_COLLECTION.deleteOne(eq("announcerId", announcer.getAnnouncerId()));
    }
}