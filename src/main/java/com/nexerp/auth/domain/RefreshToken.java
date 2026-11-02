package com.nexerp.auth.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_token_token", columnList = "tenant_id,usuario_id"),
                @Index(name = "idx_refresh_token_expiracao", columnList = "expira_em")
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "revogado_em")
    private Instant revogadoEm;

    @Column(name = "substituido_por_id")
    private UUID substituidoPorId;

    protected  RefreshToken() {
    }

    public RefreshToken(UUID tenantId, UUID usuarioId, String tokenHash, Instant expiraEm) {
        this.tenantId = tenantId;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.criadoEm = Instant.now();
        this.expiraEm = expiraEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getRevogadoEm() {
        return revogadoEm;
    }

    public UUID getSubstituidoPorId() {
        return substituidoPorId;
    }
}
