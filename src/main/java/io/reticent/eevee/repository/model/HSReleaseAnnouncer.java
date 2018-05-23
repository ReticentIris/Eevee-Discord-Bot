package io.reticent.eevee.repository.model;

import lombok.*;
import org.bson.types.ObjectId;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HSReleaseAnnouncer {
    private ObjectId objectId;
    @NonNull
    private String announcerId;
    @NonNull
    private String channelId;
    @NonNull
    private String anime;
    @NonNull
    private String quality;
    private int lastEpisode;
}
