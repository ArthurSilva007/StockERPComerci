package com.nexerp.auth.infra;

import com.nexerp.auth.domain.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilRepository  extends JpaRepository<Perfil, UUID> {

    List<Perfil> findAllByTenantIdOrderByNomeAsc(UUID tenantId);

    Optional<Perfil> findByTenantIdAndNome(UUID tenantId, String nome);
}
