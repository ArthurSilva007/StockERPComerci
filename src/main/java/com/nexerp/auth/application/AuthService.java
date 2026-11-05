/** Módulo auth, camada application. */
package com.nexerp.auth.application;

import com.nexerp.auth.api.AuthLoginRequest;
import com.nexerp.auth.api.AuthLoginResponse;
import com.nexerp.auth.domain.RefreshToken;
import com.nexerp.auth.domain.PerfilPermissao;
import com.nexerp.auth.infra.PerfilPermissaoRepository;
import com.nexerp.auth.infra.RefreshTokenRepository;
import com.nexerp.auth.infra.UsuarioEmpresaRepository;
import com.nexerp.shared.security.JwtProperties;
import com.nexerp.shared.security.JwtTokenService;
import com.nexerp.usuarios.domain.Usuario;
import com.nexerp.usuarios.domain.UsuarioEmpresa;
import com.nexerp.usuarios.infra.UsuariosRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final String CREDENCIAIS_INVALIDAS = "Credenciais inválidas";
    private static final short LIMITE_TENTATIVAS = 5;
    private static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

    private final JdbcTemplate jdbcTemplate;
    private final UsuariosRepository usuariosRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final PerfilPermissaoRepository perfilPermissaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenCodec refreshTokenCodec;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService (
            JdbcTemplate jdbcTemplate,
            UsuariosRepository usuariosRepository,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            PerfilPermissaoRepository perfilPermissaoRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties,
            RefreshTokenCodec refreshTokenCodec,
            RefreshTokenRepository refreshTokenRepository
    ){
        this.jdbcTemplate = jdbcTemplate;
        this.usuariosRepository = usuariosRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.perfilPermissaoRepository = perfilPermissaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenCodec = refreshTokenCodec;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthLoginResponse login(
            UUID empresaId,
            AuthLoginRequest request
    ){
        configurarEmpresaParaLogin(empresaId);

        UUID tenantId = buscarTenantDaEmpresa(empresaId);

        configurarTenant(tenantId);

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuariosRepository
                .findByTenantIdAndEmail(tenantId, email)
                .orElseThrow(this::credenciaisInvalidas);

        validarUsuario(usuario, request.senha());

        UsuarioEmpresa acessoEmpresa =
                usuarioEmpresaRepository
                        .findAllByTenantAndUsuario(
                                tenantId,
                                usuario.getId()
                        )
                        .stream()
                        .filter(acesso ->
                                acesso.getId()
                                        .getEmpresaId()
                                        .equals(empresaId)
                        )
                        .findFirst()
                        .orElseThrow(this::credenciaisInvalidas);

        List<String> permissoes = perfilPermissaoRepository
                .findAllByIdPerfilId(acessoEmpresa.getPerfilId())
                .stream()
                .map(PerfilPermissao :: getId)
                .map(id -> id.getPermissaoCodigo())
                .distinct()
                .sorted()
                .toList();

        return emitirTokens(usuario.getId(), tenantId, empresaId, permissoes);
    }

    @Transactional
    public AuthLoginResponse refresh(String refreshToken) {
        RefreshTokenCodec.TokenContext contexto =
                refreshTokenCodec.parseContext(refreshToken);

        configurarEmpresaParaLogin(contexto.empresaId());
        configurarTenant(contexto.tenantId());

        RefreshToken tokenAnterior = refreshTokenRepository
                .findByTokenHashAndRevogadoEmIsNull(
                        refreshTokenCodec.hash(refreshToken)
                )
                .filter(token -> token.getExpiraEm().isAfter(Instant.now()))
                .filter(token -> token.getTenantId().equals(contexto.tenantId()))
                .filter(token -> token.getEmpresaId().equals(contexto.empresaId()))
                .orElseThrow(this::credenciaisInvalidas);

        Usuario usuario = usuariosRepository.findById(tokenAnterior.getUsuarioId())
                .orElseThrow(this::credenciaisInvalidas);

        UsuarioEmpresa acessoEmpresa =
                usuarioEmpresaRepository
                        .findAllByTenantAndUsuario(
                                contexto.tenantId(),
                                usuario.getId()
                        )
                        .stream()
                        .filter(acesso -> acesso.getId().getEmpresaId()
                                .equals(contexto.empresaId()))
                        .findFirst()
                        .orElseThrow(this::credenciaisInvalidas);

        List<String> permissoes = buscarPermissoes(acessoEmpresa);
        String novoRefreshToken = refreshTokenCodec.issue(
                contexto.tenantId(),
                contexto.empresaId()
        );
        RefreshToken novoToken = refreshTokenRepository.save(
                new RefreshToken(
                        contexto.tenantId(),
                        usuario.getId(),
                        contexto.empresaId(),
                        refreshTokenCodec.hash(novoRefreshToken),
                        Instant.now().plus(
                                Duration.ofDays(jwtProperties.getRefreshTokenDays())
                        )
                )
        );
        tokenAnterior.revogar(Instant.now(), novoToken.getId());
        refreshTokenRepository.save(tokenAnterior);

        String accessToken = jwtTokenService.issueAccessToken(
                usuario.getId(),
                contexto.tenantId(),
                permissoes
        );
        return new AuthLoginResponse(
                accessToken,
                "Bearer",
                novoRefreshToken,
                jwtProperties.getAccessTokenMinutes() * 60
        );
    }

    private AuthLoginResponse emitirTokens(
            UUID usuarioId,
            UUID tenantId,
            UUID empresaId,
            List<String> permissoes
    ) {
        String accessToken = jwtTokenService.issueAccessToken(
                usuarioId,
                tenantId,
                permissoes
        );
        String refreshToken = refreshTokenCodec.issue(tenantId, empresaId);
        refreshTokenRepository.save(
                new RefreshToken(
                        tenantId,
                        usuarioId,
                        empresaId,
                        refreshTokenCodec.hash(refreshToken),
                        Instant.now().plus(
                                Duration.ofDays(jwtProperties.getRefreshTokenDays())
                        )
                )
        );
        return new AuthLoginResponse(
                accessToken,
                "Bearer",
                refreshToken,
                jwtProperties.getAccessTokenMinutes() * 60
        );
    }

    private List<String> buscarPermissoes(UsuarioEmpresa acessoEmpresa) {
        return perfilPermissaoRepository
                .findAllByIdPerfilId(acessoEmpresa.getPerfilId())
                .stream()
                .map(PerfilPermissao::getId)
                .map(id -> id.getPermissaoCodigo())
                .distinct()
                .sorted()
                .toList();
    }
     private void configurarEmpresaParaLogin(UUID empresaId) {
        jdbcTemplate.queryForObject(
                "select set_config('app.empresa_id', ?, true)",
                String.class,
                empresaId.toString()
        );
     }

     private UUID buscarTenantDaEmpresa(UUID empresaId) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                            select tenant_id
                            from empresa
                            where id = ?
                            """,
                    UUID.class,
                    empresaId
            );
        }catch (Exception exception){
            throw credenciaisInvalidas();
        }
     }

     private void configurarTenant(UUID tenantId) {
        jdbcTemplate.queryForObject(
                "select set_config('app.tenant_id', ?, true)",
                String.class,
                tenantId.toString()
        );
     }

     private void validarUsuario(
             Usuario usuario,
             String senha
     ) {
        Instant agora = Instant.now();
        boolean bloqueado = usuario.getBloqueadoAte() != null
                && usuario.getBloqueadoAte().isAfter(agora);

        if (!"Ativo".equalsIgnoreCase(usuario.getStatus()) || bloqueado) {
            throw credenciaisInvalidas();
        }

        boolean senhaValida = passwordEncoder.matches(
                senha,
                usuario.getSenhaHash()

        );

        if (!senhaValida) {
            usuario.registrarFalhaDeLogin(
                    agora,
                    LIMITE_TENTATIVAS,
                    agora.plus(DURACAO_BLOQUEIO)
            );
            usuariosRepository.save(usuario);
            throw credenciaisInvalidas();
        }

        usuario.registrarLoginComSucesso(agora);
        usuariosRepository.save(usuario);
     }

     private ResponseStatusException credenciaisInvalidas(){
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                CREDENCIAIS_INVALIDAS
        );
     }
}
