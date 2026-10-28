package com.example.api.auth;

import java.util.List;
import java.util.UUID;

public class RoleContext {
    private static final ThreadLocal<List<String>> CURRENT = new ThreadLocal<>();
    public static void setRoles(List<String> role) {
        CURRENT.set(role);
    }

    public static List<String> getRoles() {
        List<String> roles = CURRENT.get();

        if (roles == null) {
            throw new IllegalStateException(
                    "No hay un role asociado a la petición"
            );
        }

        return roles;
    }

    public static boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
