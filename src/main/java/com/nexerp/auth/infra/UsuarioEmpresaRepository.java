package com.nexerp.auth.infra;

import com.nexerp.usuarios.domain.UsuarioEmpresa;
import com.nexerp.usuarios.domain.UsuarioEmpresaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UsuarioEmpresaRepository
        extends JpaRepository<UsuarioEmpresa, UsuarioEmpresaId> {

    @Query("""
            select ue
            from UsuarioEmpresa ue
            where ue.tenantId = :tenantId
              and ue.id.usuarioId = :usuarioId
            order by ue.id.empresaId
            """)
    List<UsuarioEmpresa> findAllByTenantAndUsuario(
            @Param("tenantId") UUID tenantId,
            @Param("usuarioId") UUID usuarioId);

    @Query("""
            select ue
            from UsuarioEmpresa ue
            where ue.tenantId = :tenantId
              and ue.id.empresaId = :empresaId
            order by ue.id.usuarioId
            """)
    List<UsuarioEmpresa> findAllByTenantAndEmpresa(
            @Param("tenantId") UUID tenantId,
            @Param("empresaId") UUID empresaId);
}
