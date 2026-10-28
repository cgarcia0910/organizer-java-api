package com.example.api.auth;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();
    public static void setTenantId(UUID tenantId) {
        CURRENT.set(tenantId);
    }

    public static UUID getTenantId() {
        UUID tenantId = CURRENT.get();

        if (tenantId == null) {
            throw new IllegalStateException(
                    "No hay un tenant asociado a la petición"
            );
        }

        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
