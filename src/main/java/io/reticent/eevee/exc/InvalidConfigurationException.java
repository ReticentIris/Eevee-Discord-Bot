package io.reticent.eevee.exc;

import java.io.IOException;

public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException(String message) {
        super(message);
    }
}
