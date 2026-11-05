/** Módulo auth, camada application. */
package com.nexerp.auth.application;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class RefreshTokenCodec {

    private final SecureRandom secureRandom = new SecureRandom();

    public String issue (UUID tenantId, UUID empresaId) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String random = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        return tenantId + "." + empresaId + "." + random;
    }

    public String hash (String token) {
        try{
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        }catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não é suportado", exception);
        }
    }
    public TokenContext parseContext(String token) {
        String[] parts = token.split("\\.", -1);

        if (parts.length != 3 || parts [2].isBlank()) {
            throw new IllegalArgumentException("Token de refresh inválido");
        }

        try{
            return new TokenContext(
                    UUID.fromString(parts[0]),
                    UUID.fromString(parts[1])
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Token de refresh inválido", exception);
        }
    }

    public record TokenContext(UUID tenantId, UUID empresaId) {}
}
