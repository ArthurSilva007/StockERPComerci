package com.nexerp.shared.tenant;

import java.util.UUID;
import java.util.function.Supplier;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId não pode ser nulo");
        }
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID requireTenantId() {
        UUID tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant não definido no contexto da requisição");
        }
        return tenantId;
    }

    public static UUID get() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static <T> T withTenant(UUID tenantId, Supplier<T> action) {
        UUID previousTenant = CURRENT_TENANT.get();
        set(tenantId);
        try {
            return action.get();
        } finally {
            if (previousTenant == null) {
                clear();
            } else {
                set(previousTenant);
            }
        }
    }
}
