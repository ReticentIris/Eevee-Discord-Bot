package io.reticent.eevee.exc;

import lombok.NonNull;

public class InvalidConfigurationKeyException extends InvalidConfigurationException {
    public InvalidConfigurationKeyException(String message) {
        super(message);
    }
}
