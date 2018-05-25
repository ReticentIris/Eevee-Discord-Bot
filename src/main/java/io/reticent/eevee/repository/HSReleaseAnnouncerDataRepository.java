package io.reticent.eevee.repository;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import io.reticent.eevee.provider.MongoClientProvider;
import io.reticent.eevee.repository.model.HSReleaseAnnouncer;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Log4j2
public class HSReleaseAnnouncerDataRepository extends DataRepository {
    private static HSReleaseAnnouncerDataRepository hsReleaseAnnouncerDataRepository;
    private final MongoCollection<HSReleaseAnnouncer> MONGO_COLLECTION;

    private final String DATABASE_NAME = Session.getSession().getConfiguration().readString("mongoDatabaseName");
    private final String COLLECTION_NAME = Session.getSession().getConfiguration().readString("mongoHSReleaseAnnouncerCollectionName");

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

    public Optional<HSReleaseAnnouncer> getAnnouncer(String anime, String quality, String channelId) {
        return Optional.ofNullable(MONGO_COLLECTION.find(
            and(
                eq("channelId", channelId),
                eq("anime", anime),
                eq("quality", quality)
            )
        ).collation(
            Collation.builder()
                     .locale("en")
                     .collationStrength(CollationStrength.PRIMARY)
                     .build()
        ).first());
    }

    public void add(HSReleaseAnnouncer announcer) {
        MONGO_COLLECTION.insertOne(announcer);
    }

    public void update(HSReleaseAnnouncer announcer) {
        MONGO_COLLECTION.replaceOne(eq("announcerId", announcer.getAnnouncerId()), announcer);
    }

    public void remove(String announcerId) {
        MONGO_COLLECTION.deleteOne(eq("announcerId", announcerId));
    }

    public void remove(HSReleaseAnnouncer announcer) {
        MONGO_COLLECTION.deleteOne(eq("announcerId", announcer.getAnnouncerId()));
    }
}
