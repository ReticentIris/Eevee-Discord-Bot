package io.reticent.eevee.util;

import lombok.Builder;

import java.time.Instant;

@Builder
public class RateLimiter {
    private int currentHits;
    private int maxHits;
    private int duration;
    @Builder.Default
    private Instant lastReset = Instant.now();

    public boolean tryIncrement() {
        Instant now = Instant.now();
        Instant resetTime = lastReset.plusMillis(duration);

        if (now.isAfter(resetTime)) {
            currentHits = 0;
            lastReset = resetTime;
        }

        if (currentHits < maxHits) {
            currentHits++;
            return true;
        } else {
            return false;
        }
    }
}
