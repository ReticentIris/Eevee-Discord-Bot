package io.reticent.eevee.exc;

import lombok.NonNull;

public class ArgumentMappingException extends RuntimeException {
    public ArgumentMappingException(@NonNull String message) {
        super(message);
    }
}
