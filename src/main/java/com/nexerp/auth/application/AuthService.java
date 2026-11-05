/** Módulo auth, camada application. */
package com.nexerp.auth.application;

import com.nexerp.auth.api.AuthLoginRequest;
import com.nexerp.auth.api.AuthLoginResponse;
import com.nexerp.auth.domain.PerfilPermissao;
import com.nexerp.auth.infra.PerfilPermissaoRepository;
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

    public AuthService (
            JdbcTemplate jdbcTemplate,
            UsuariosRepository usuariosRepository,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            PerfilPermissaoRepository perfilPermissaoRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties
    ){
        this.jdbcTemplate = jdbcTemplate;
        this.usuariosRepository = usuariosRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.perfilPermissaoRepository = perfilPermissaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
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

        String token = jwtTokenService.issueAccessToken(
                usuario.getId(),
                tenantId,
                permissoes
        );
        return new AuthLoginResponse(
                token,
                "Bearer",
                jwtProperties.getAccessTokenMinutes() * 60
        );
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
