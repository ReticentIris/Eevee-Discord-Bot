package io.reticent.eevee.provider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.BsonType;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.async.client.MongoClients.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoClientProvider {
    private static MongoClient mongoClient;

    public static MongoClient getInstance() {
        if (mongoClient != null) {
            return mongoClient;
        }

        Map<BsonType, Class<?>> replacements = new HashMap<BsonType, Class<?>>();
        replacements.put(BsonType.DATE_TIME, Instant.class);
        BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap(replacements);

        DocumentCodecProvider documentCodecProvider = new DocumentCodecProvider(bsonTypeClassMap);

        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
                                                         fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoClientSettings settings = MongoClientSettings.builder()
                                                          .codecRegistry(pojoCodecRegistry)
                                                          .build();

        mongoClient = MongoClients.create(settings);

        return mongoClient;
    }
}
