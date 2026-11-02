/** Módulo usuarios, camada domain. */
package com.nexerp.usuarios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UsuarioEmpresaId implements Serializable {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    public UsuarioEmpresaId() {
    }

    public UsuarioEmpresaId(UUID usuarioId, UUID empresaId) {
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UsuarioEmpresaId that)) {
            return false;
        }
        return Objects.equals(usuarioId, that.usuarioId)
                && Objects.equals(empresaId, that.empresaId);
        }
        @Override
    public int hashCode() {
        return Objects.hash(usuarioId, empresaId);
    }
}
