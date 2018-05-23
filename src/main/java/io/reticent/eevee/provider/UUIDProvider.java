package io.reticent.eevee.provider;

import java.util.UUID;

public class UUIDProvider {
    public static String genUUID4() {
        return UUID.randomUUID().toString();
    }
}
