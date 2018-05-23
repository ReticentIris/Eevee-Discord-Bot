package io.reticent.eevee.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonDeserialize(builder = HSReleaseAnnouncement.HSReleaseAnnouncementBuilder.class)
public class HSReleaseAnnouncement {
    @JsonProperty
    private String announcementId;
    @JsonProperty
    private String channelId;
    @JsonProperty
    private String anime;
    @JsonProperty
    private String quality;
    @JsonProperty
    private int lastEpisode;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class HSReleaseAnnouncementBuilder {}
}
