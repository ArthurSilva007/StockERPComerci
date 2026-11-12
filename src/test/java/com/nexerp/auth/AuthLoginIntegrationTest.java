package com.nexerp.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AuthLoginIntegrationTest {

    private static final UUID TENANT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID OUTRO_TENANT_ID =
            UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final UUID EMPRESA_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID EMPRESA_SEM_ACESSO_ID =
            UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final UUID EMPRESA_OUTRO_TENANT_ID =
            UUID.fromString("77777777-7777-7777-7777-777777777777");

    private static final UUID USUARIO_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID PERFIL_ID =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final String USUARIO_EMAIL =
            "login.teste@nexerp.com";

    private static final String USUARIO_SENHA = "senha123";

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("nexerp_login_test")
                    .withUsername("nexerp_login_test")
                    .withPassword("nexerp_login_test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder
            passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void prepareDatabase() {
        String senhaHash = passwordEncoder.encode(USUARIO_SENHA);

        jdbcTemplate.update(
                """
                INSERT INTO tenant (id, nome, criado_em)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                TENANT_ID,
                "Tenant Login Teste",
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO tenant (id, nome, criado_em)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                OUTRO_TENANT_ID,
                "Outro Tenant",
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO empresa (
                    id, tenant_id, razao_social, nome_fantasia,
                    cnpj, criado_em, atualizado_em
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                EMPRESA_ID,
                TENANT_ID,
                "Empresa Login Teste LTDA",
                "Empresa Login Teste",
                "12345678000195",
                agora(),
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO empresa (
                    id, tenant_id, razao_social, nome_fantasia,
                    cnpj, criado_em, atualizado_em
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                EMPRESA_OUTRO_TENANT_ID,
                OUTRO_TENANT_ID,
                "Empresa Outro Tenant LTDA",
                "Empresa Outro Tenant",
                "98765432000195",
                agora(),
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO empresa (
                    id, tenant_id, razao_social, nome_fantasia,
                    cnpj, criado_em, atualizado_em
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                EMPRESA_SEM_ACESSO_ID,
                TENANT_ID,
                "Empresa Sem Acesso LTDA",
                "Empresa Sem Acesso",
                "98765432000195",
                agora(),
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO perfil (
                    id, tenant_id, nome, sistema,
                    criado_em, atualizado_em
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """,
                PERFIL_ID,
                TENANT_ID,
                "Operador de Caixa",
                false,
                agora(),
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO usuario (
                    id, tenant_id, nome, email, senha_hash,
                    status, tentativas_falhas,
                    dois_fatores_habilitado, criado_em, atualizado_em
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    senha_hash = EXCLUDED.senha_hash,
                    status = EXCLUDED.status,
                    tentativas_falhas = EXCLUDED.tentativas_falhas,
                    bloqueado_ate = EXCLUDED.bloqueado_ate,
                    ultimo_login_em = EXCLUDED.ultimo_login_em
                """,
                USUARIO_ID,
                TENANT_ID,
                "Usuario Login Teste",
                USUARIO_EMAIL,
                senhaHash,
                "ATIVO",
                0,
                false,
                agora(),
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO usuario_empresa (
                    tenant_id, usuario_id, empresa_id,
                    perfil_id, criado_em
                )
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (usuario_id, empresa_id) DO NOTHING
                """,
                TENANT_ID,
                USUARIO_ID,
                EMPRESA_ID,
                PERFIL_ID,
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO perfil_permissao (
                    perfil_id, permissao_codigo, criado_em
                )
                VALUES (?, ?, ?)
                ON CONFLICT (perfil_id, permissao_codigo) DO NOTHING
                """,
                PERFIL_ID,
                "venda:criar",
                agora()
        );

        jdbcTemplate.update(
                """
                INSERT INTO perfil_permissao (
                    perfil_id, permissao_codigo, criado_em
                )
                VALUES (?, ?, ?)
                ON CONFLICT (perfil_id, permissao_codigo) DO NOTHING
                """,
                PERFIL_ID,
                "empresa:ler",
                agora()
        );
    }

    @Test
    void empresasSemJwtRetornaNaoAutorizado() throws Exception {
        mockMvc
                .perform(get("/api/v1/empresas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void empresasSemPermissaoRetornaProibido() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/empresas")
                                .with(jwt().jwt(token -> token.claim(
                                        "tenant_id", TENANT_ID.toString())))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void empresasComPermissaoRetornaApenasDadosDoTenantDoToken()
            throws Exception {
        String response = mockMvc
                .perform(
                        get("/api/v1/empresas")
                                .with(jwt()
                                        .jwt(token -> token.claim(
                                                "tenant_id", TENANT_ID.toString()))
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "empresa:ler")))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode empresas = objectMapper.readTree(response);

        assertThat(empresas.isArray()).isTrue();
        assertThat(empresas.size()).isGreaterThan(0);
        for (JsonNode empresa : empresas) {
            assertThat(empresa.path("tenantId").asText())
                    .isEqualTo(TENANT_ID.toString());
            assertThat(empresa.path("id").asText())
                    .isNotEqualTo(EMPRESA_OUTRO_TENANT_ID.toString());
        }
    }

    @Test
    void loginValidoRetornaJwtComTenantEPermissao()
            throws Exception {
        String body = """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(USUARIO_EMAIL, USUARIO_SENHA);

        String response = mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("accessToken").asText())
                .isNotBlank();
        assertThat(json.path("refreshToken").asText())
                .isNotBlank();
        assertThat(json.path("tokenType").asText())
                .isEqualTo("Bearer");
        assertThat(json.path("expiresIn").asLong())
                .isEqualTo(900);

        Jwt jwt = jwtDecoder.decode(
                json.path("accessToken").asText()
        );

        assertThat(jwt.getSubject())
                .isEqualTo(USUARIO_ID.toString());
        assertThat(jwt.getClaimAsString("tenant_id"))
                .isEqualTo(TENANT_ID.toString());
        assertThat(jwt.getClaimAsStringList("permissions"))
                .contains("venda:criar");
    }

    @Test
    void refreshTokenRevogaTokenAnterior() throws Exception {
        String loginBody = """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(USUARIO_EMAIL, USUARIO_SENHA);

        String loginResponse = mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshTokenAnterior = objectMapper
                .readTree(loginResponse)
                .path("refreshToken")
                .asText();

        String refreshResponse = mockMvc
                .perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "refreshToken": "%s"
                                        }
                                        """.formatted(refreshTokenAnterior))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String novoRefreshToken = objectMapper
                .readTree(refreshResponse)
                .path("refreshToken")
                .asText();

        assertThat(novoRefreshToken)
                .isNotBlank();

        mockMvc
                .perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "refreshToken": "%s"
                                        }
                                        """.formatted(refreshTokenAnterior))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenMalformadoRetornaNaoAutorizado()
            throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "refreshToken": "token-malformado"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenVazioRetornaBadRequest()
            throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "refreshToken": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshTokenNaoFicaArmazenadoEmTextoPuro()
            throws Exception {
        String loginResponse = mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "senha": "%s"
                                        }
                                        """.formatted(USUARIO_EMAIL, USUARIO_SENHA))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper
                .readTree(loginResponse)
                .path("refreshToken")
                .asText();

        Integer plainTokenCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token WHERE token_hash = ?",
                Integer.class,
                refreshToken
        );

        String storedHash = jdbcTemplate.queryForObject(
                "SELECT token_hash FROM refresh_token "
                        + "WHERE tenant_id = ? AND usuario_id = ? "
                        + "ORDER BY criado_em DESC LIMIT 1",
                String.class,
                TENANT_ID,
                USUARIO_ID
        );

        assertThat(plainTokenCount).isZero();
        assertThat(storedHash)
                .hasSize(64)
                .isNotEqualTo(refreshToken);
    }

    @Test
    void senhaInvalidaRetornaNaoAutorizado()
            throws Exception {
        String body = """
                {
                  "email": "%s",
                  "senha": "senha-incorreta"
                }
                """.formatted(USUARIO_EMAIL);

        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void empresaSemVinculoDoUsuarioRetornaNaoAutorizado()
            throws Exception {
        String body = """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(USUARIO_EMAIL, USUARIO_SENHA);

        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_SEM_ACESSO_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void emailInvalidoRetornaBadRequest()
            throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "email-invalido",
                                          "senha": "%s"
                                        }
                                        """.formatted(USUARIO_SENHA))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void senhaVaziaRetornaBadRequest()
            throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "senha": ""
                                        }
                                        """.formatted(USUARIO_EMAIL))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void empresaHeaderAusenteRetornaBadRequest()
            throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "senha": "%s"
                                        }
                                        """.formatted(USUARIO_EMAIL, USUARIO_SENHA))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void cincoSenhasInvalidasBloqueiamUsuario()
            throws Exception {
        String senhaInvalida = "senha-incorreta";

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            mockMvc
                    .perform(
                            post("/api/v1/auth/login")
                                    .header("X-Empresa-Id", EMPRESA_ID.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                              "email": "%s",
                                              "senha": "%s"
                                            }
                                            """.formatted(USUARIO_EMAIL, senhaInvalida))
                    )
                    .andExpect(status().isUnauthorized());
        }

        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .header("X-Empresa-Id", EMPRESA_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "senha": "%s"
                                        }
                                        """.formatted(USUARIO_EMAIL, USUARIO_SENHA))
                )
                .andExpect(status().isUnauthorized());

        Integer tentativas = jdbcTemplate.queryForObject(
                "SELECT tentativas_falhas FROM usuario WHERE id = ?",
                Integer.class,
                USUARIO_ID
        );

        assertThat(tentativas).isEqualTo(5);
    }

    private static Timestamp agora() {
        return Timestamp.from(Instant.now());
    }
}
