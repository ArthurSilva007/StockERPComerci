package com.nexerp.shared.tenant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Aspect
@Component
public class TenantTransactionAspect {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public TenantTransactionAspect(
            JdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Around("@annotation(com.nexerp.shared.tenant.TenantTransactional) || "
            + "@within(com.nexerp.shared.tenant.TenantTransactional)")
    public Object applyTenantContext(ProceedingJoinPoint joinPoint) {
        UUID tenantId = TenantContext.requireTenantId();

        return transactionTemplate.execute(status -> {
            jdbcTemplate.queryForObject(
                    "SELECT set_config('app.tenant_id', ?, true)",
                    String.class,
                    tenantId.toString());

            try {
                return joinPoint.proceed();
            } catch (RuntimeException exception) {
                status.setRollbackOnly();
                throw exception;
            } catch (Throwable exception) {
                status.setRollbackOnly();
                throw new TenantTransactionException(
                        "Falha na operação tenant-aware", exception);
            }
        });
    }

    public static class TenantTransactionException extends RuntimeException {

        public TenantTransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
