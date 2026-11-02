package com.nexerp.usuarios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario_empresa")
public class UsuarioEmpresa {

    @EmbeddedId
    private UsuarioEmpresaId id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "perfil_id", nullable = false)
    private UUID perfilId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected UsuarioEmpresa() {

    }

    public UsuarioEmpresa(UUID usuarioId, UUID empresaId, UUID tenantId, UUID perfilId) {
        this.id = new UsuarioEmpresaId(usuarioId, empresaId);
        this.tenantId = tenantId;
        this.perfilId = perfilId;
        this.criadoEm = Instant.now();
    }

    public UsuarioEmpresaId getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getPerfilId() {
        return perfilId;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
