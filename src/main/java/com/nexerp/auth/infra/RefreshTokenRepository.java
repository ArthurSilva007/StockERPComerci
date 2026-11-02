package com.nexerp.auth.infra;

import com.nexerp.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevogadoEmIsNull(
            String tokenHash
    );

    long deleteByExpiraEmBefore(Instant limite);
}