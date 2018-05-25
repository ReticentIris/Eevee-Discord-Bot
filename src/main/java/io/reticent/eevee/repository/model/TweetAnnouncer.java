package io.reticent.eevee.repository.model;

import lombok.*;
import org.bson.types.ObjectId;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TweetAnnouncer {
    private ObjectId objectId;
    @NonNull
    private String announcerId;
    @NonNull
    private String channelId;
    @NonNull
    private String user;
    private long lastTweetId;
}
