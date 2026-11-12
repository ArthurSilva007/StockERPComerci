/** Módulo empresas, camada application. */
package com.nexerp.empresas.application;

import com.nexerp.empresas.api.EmpresaResponse;
import com.nexerp.empresas.infra.EmpresaRepository;
import com.nexerp.shared.tenant.TenantContext;
import com.nexerp.shared.tenant.TenantTransactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService{

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @TenantTransactional
    @PreAuthorize("hasAuthority('empresa:ler')")
    public List<EmpresaResponse> listar(){
        return empresaRepository
                .findAllByTenantIdOrderByRazaoSocialAsc(
                        TenantContext.requireTenantId()
                )
                .stream()
                .map(EmpresaResponse::from)
                .toList();
    }
}
