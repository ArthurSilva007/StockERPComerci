package com.nexerp.usuarios.infra;

import com.nexerp.usuarios.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface UsuariosRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmail(UUID tenantId, String Email);
}