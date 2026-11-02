package com.nexerp.usuarios.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "usuario",
        indexes = {
                @Index(name = "idx_usuario_status", columnList = "tenant_status")
        }
)
public class Usuario {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false, length = 254)
    private  String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private  String senhaHash;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "tentativas_falhas", nullable = false)
    private short tentativasFalhas;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    @Column(name = "dois_fatores_habilitado", nullable = false)
    private boolean doisFatoresHabilitado;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "ultimo_login_em")
    private Instant ultimoLoginEm;

    protected Usuario() {

    }

    public Usuario(UUID tenantId, String nome, String email, String senhaHash) {
        this.tenantId = tenantId;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.status = "ATIVO";
        this.tentativasFalhas = 0;
        this.doisFatoresHabilitado = false;
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
    public String getEmail() {
        return email;
    }
    public String getSenhaHash() {
        return senhaHash;
    }
    public String getStatus() {
        return status;
    }
    public short getTentativasFalhas() {
        return tentativasFalhas;
    }
    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }
    public boolean isDoisFatoresHabilitado() {
        return doisFatoresHabilitado;
    }
    public Instant getCriadoEm() {
        return criadoEm;
    }
    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
    public Instant getUltimoLoginEm() {
        return ultimoLoginEm;
    }
}

