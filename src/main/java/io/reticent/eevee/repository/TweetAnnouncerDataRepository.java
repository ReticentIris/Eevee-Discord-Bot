package io.reticent.eevee.repository;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.reticent.eevee.provider.MongoClientProvider;
import io.reticent.eevee.repository.model.TweetAnnouncer;
import io.reticent.eevee.session.Session;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Log4j2
public class TweetAnnouncerDataRepository extends DataRepository {
    private static TweetAnnouncerDataRepository tweetAnnouncerDataRepository;
    private final MongoCollection<TweetAnnouncer> MONGO_COLLECTION;

    private final String DATABASE_NAME = Session.getSession().getConfiguration().readString("mongoDatabaseName");
    private final String COLLECTION_NAME = Session.getSession().getConfiguration().readString("mongoTweetAnnouncerCollectionName");

    private TweetAnnouncerDataRepository() {
        super();
        MongoClient mongoClient = MongoClientProvider.getInstance();
        MONGO_COLLECTION = mongoClient.getDatabase(DATABASE_NAME)
                                      .getCollection(COLLECTION_NAME, TweetAnnouncer.class);
    }

    public static TweetAnnouncerDataRepository getInstance() {
        if (tweetAnnouncerDataRepository == null) {
            tweetAnnouncerDataRepository = new TweetAnnouncerDataRepository();
        }

        return tweetAnnouncerDataRepository;
    }

    public List<TweetAnnouncer> getAnnouncers() {
        return ImmutableList.copyOf(MONGO_COLLECTION.find());
    }

    public Optional<TweetAnnouncer> getAnnouncer(String user, String channelId) {
        return getAnnouncers().stream()
                              .filter(a -> a.getUser().equalsIgnoreCase(user))
                              .filter(a -> a.getChannelId().equals(channelId))
                              .findFirst();
    }

    public void add(TweetAnnouncer announcer) {
        MONGO_COLLECTION.insertOne(announcer);
    }

    public void update(TweetAnnouncer announcer) {
        MONGO_COLLECTION.replaceOne(eq("announcerId", announcer.getAnnouncerId()), announcer);
    }

    public void remove(String announcerId) {
        MONGO_COLLECTION.deleteOne(eq("announcerId", announcerId));
    }

    public void remove(TweetAnnouncer announcer) {
        MONGO_COLLECTION.deleteOne(eq("announcerId", announcer.getAnnouncerId()));
    }
}
