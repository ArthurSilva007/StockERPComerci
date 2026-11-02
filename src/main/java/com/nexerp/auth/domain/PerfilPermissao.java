package com.nexerp.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "perfil_permissao")
public class PerfilPermissao {

    @EmbeddedId
    private PerfilPermissaoId id;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected PerfilPermissao() {

    }
    public PerfilPermissao(String permissaoCodigo, java.util.UUID perfilId) {
        this.id = new PerfilPermissaoId(perfilId, permissaoCodigo);
        this.criadoEm = Instant.now();
    }

    public PerfilPermissaoId getId() {
        return id;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
