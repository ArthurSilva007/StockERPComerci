package com.nexerp.auth.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "perfil"
)
public class Perfil {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

@Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false)
    private boolean sistema;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Perfil() {
    }

    public Perfil(UUID tenantId, String nome, boolean sistema) {
        this.tenantId = tenantId;
        this.nome = nome;
        this.sistema = sistema;
        this.criadoEm = Instant.now();
        this.atualizadoEm = this.criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getNome() {
        return nome;
    }

    public boolean isSistema() {
        return sistema;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
     public  Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}