package com.example.api.auth;

public class TenantContext {
    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();
    public static void setTenantId(Long tenantId) {
        CURRENT.set(tenantId);
    }

    public static Long getTenantId() {
        Long tenantId = CURRENT.get();

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
