package com.nexerp.auth.infra;

import com.nexerp.auth.domain.PerfilPermissao;
import com.nexerp.auth.domain.PerfilPermissaoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PerfilPermissaoRepository
        extends JpaRepository<PerfilPermissao, PerfilPermissaoId> {

    List<PerfilPermissao> findAllByIdPerfilId(UUID perfilId);
}
