package com.nexerp.empresas.api;

import com.nexerp.empresas.domain.Empresa;

public record EmpresaResponse(
        String id,
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String tenantId,
        String criadoEm,
        String atualizadoEm
) {

    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId().toString(),
                empresa.getCnpj(),
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getTenantId().toString(),
                empresa.getCriadoEm().toString(),
                empresa.getAtualizadoEm().toString()
        );
    }
}
