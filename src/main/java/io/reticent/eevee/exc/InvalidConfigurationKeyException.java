package io.reticent.eevee.exc;

import lombok.NonNull;

public class InvalidConfigurationKeyException extends InvalidConfigurationException {
    public InvalidConfigurationKeyException(@NonNull String message) {
        super(message);
    }
}
