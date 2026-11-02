package com.nexerp.empresas.infra;

import com.nexerp.empresas.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    List<Empresa> findAllByTenantIdOrderByRazaoSocialAsc(UUID tenantId);

    boolean existsByTenantIdAndCnpj(UUID tenantId, String cnpj);
}