package com.nexerp.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissao")
public class Permissao {

    @Id
    @Column(length = 80)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String descricao;

    protected Permissao() {

    }

    public Permissao(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }
    public String getDescricao() {
        return descricao;
    }
}