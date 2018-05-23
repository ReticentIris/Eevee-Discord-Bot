package io.reticent.eevee.repository.model;

import lombok.*;
import org.bson.types.ObjectId;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
    private ObjectId id;
    @NonNull
    private String userTag;
    @NonNull
    private String userId;
    @NonNull
    private String reminder;
    @NonNull
    private Instant remindAt;
}
