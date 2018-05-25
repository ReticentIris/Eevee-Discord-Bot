package io.reticent.eevee.provider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.async.client.MongoClients.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoClientProvider {
    private static MongoClient mongoClient;

    public static MongoClient getInstance() {
        if (mongoClient != null) {
            return mongoClient;
        }

        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
                                                         fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClientSettings settings = MongoClientSettings.builder()
                                                          .codecRegistry(pojoCodecRegistry)
                                                          .build();

        mongoClient = MongoClients.create(settings);

        return mongoClient;
    }
}
