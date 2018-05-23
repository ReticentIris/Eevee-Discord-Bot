package io.reticent.eevee.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Builder
@Value
@JsonDeserialize(builder = Reminder.ReminderBuilder.class)
public class Reminder {
    @JsonProperty
    private String userTag;
    @JsonProperty
    private String userId;
    @JsonProperty
    private String reminder;
    @JsonProperty
    private Instant remindAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ReminderBuilder {}
}
