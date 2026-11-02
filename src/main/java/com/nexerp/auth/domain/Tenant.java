package com.nexerp.auth.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "nome", nullable = false, length = 200)
    private String name;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Tenant() {
    }

    public Tenant(String name) {
        this.name = name;
        this.criadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}