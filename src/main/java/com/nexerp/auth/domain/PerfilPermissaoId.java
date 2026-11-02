package com.nexerp.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.core.PriorityOrdered;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PerfilPermissaoId {

    @Column(name = "perfil_id", nullable = false)
    private UUID perfilId;

    @Column(name = "permissao_codigo", nullable = false, length = 80)
    private String permissaoCodigo;

    protected  PerfilPermissaoId() {
    }

    public PerfilPermissaoId(UUID perfilId, String permissaoCodigo) {
        this.perfilId = perfilId;
        this.permissaoCodigo = permissaoCodigo;
    }

    public UUID getPerfilId() {
        return perfilId;
    }
    public String getPermissaoCodigo() {
        return permissaoCodigo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PerfilPermissaoId that)) {
            return false;
        }
        return Objects.equals(perfilId, that.perfilId)
                && Objects.equals(permissaoCodigo, that.permissaoCodigo);
    }
    @Override
    public int hashCode() {
        return Objects.hash(perfilId, permissaoCodigo);
    }
}
