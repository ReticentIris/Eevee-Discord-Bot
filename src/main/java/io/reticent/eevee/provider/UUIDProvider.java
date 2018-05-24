package io.reticent.eevee.provider;

import java.util.UUID;

public class UUIDProvider {
    public static String getUUID4() {
        return UUID.randomUUID().toString();
    }
}
