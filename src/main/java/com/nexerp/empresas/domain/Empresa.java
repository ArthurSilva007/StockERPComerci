package com.nexerp.empresas.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "empresa",
        indexes = {
                @Index(name = "idx_empresa_tenant", columnList = "tenant_id")
        }
)
public class Empresa {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;


    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "razao.social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome.fantasia",length = 200)
    private String nomeFantasia;

    @Column(length = 14)
    private String cnpj;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Empresa() {

    }

    public Empresa(UUID tenantId, String razaoSocial, String nomeFantasia, String cnpj) {
        this.tenantId = tenantId;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.criadoEm = Instant.now();
        this.atualizadoEm = this.criadoEm;
    }

    public UUID getId() {
        return id;
    }
    public UUID getTenantId() {
        return tenantId;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
