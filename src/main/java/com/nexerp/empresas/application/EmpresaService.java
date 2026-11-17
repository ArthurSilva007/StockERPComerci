/** Módulo empresas, camada application. */
package com.nexerp.empresas.application;

import com.nexerp.empresas.api.EmpresaResponse;
import com.nexerp.empresas.domain.Empresa;
import com.nexerp.empresas.infra.EmpresaRepository;
import com.nexerp.shared.tenant.TenantContext;
import com.nexerp.shared.tenant.TenantTransactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.nexerp.empresas.api.EmpresaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

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

    @TenantTransactional
    @PreAuthorize("hasAuthority('empresa:criar')")
    public EmpresaResponse criar(EmpresaRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        if (request.cnpj() != null
                && empresaRepository.existsByTenantIdAndCnpj(
                tenantId,
                request.cnpj()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CNPJ já cadastrado"
            );
        }

        Empresa empresa = new Empresa(
                tenantId,
                request.razaoSocial(),
                request.nomeFantasia(),
                request.cnpj()
        );

        return EmpresaResponse.from(
                empresaRepository.save(empresa)
        );
    }
}
