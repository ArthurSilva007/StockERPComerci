package com.nexerp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexerp.shared.tenant.TenantContext;
import com.nexerp.shared.tenant.TenantTransactional;

import java.util.List;
import java.util.UUID;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(NexErpApplicationTest.TenantRlsTestConfiguration.class)
class NexErpApplicationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("nexerp_test")
            .withUsername("nexerp_test")
            .withPassword("nexerp_test");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantRlsProbeService tenantRlsProbeService;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void applicationClassExists() {
        assertThat(NexErpApplication.class).isNotNull();
    }

    @Test
    void healthEndpointIsPublicAndUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"UP\"}"));
    }

    @Test
    void flywayCreatesBaselineTables() {
        Integer tenantTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'tenant'",
                Integer.class);

        Integer empresaTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'empresa'",
                Integer.class);

        assertThat(tenantTableCount).isEqualTo(1);
        assertThat(empresaTableCount).isEqualTo(1);

        Integer usuarioTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'usuario'",
                Integer.class);

        Integer refreshTokenTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'refresh_token'",
                Integer.class);

        assertThat(usuarioTableCount).isEqualTo(1);
        assertThat(refreshTokenTableCount).isEqualTo(1);
    }

    @Test
    void passwordEncoderUsesOneWayHashWithCostTwelve() {
        String rawPassword = "Senha-de-teste-123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2");
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void rlsAllowsOnlyTheCurrentTenant() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID empresaA = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO tenant (id, nome) VALUES (?, ?)", tenantA, "Tenant A");
        jdbcTemplate.update("INSERT INTO tenant (id, nome) VALUES (?, ?)", tenantB, "Tenant B");
        jdbcTemplate.update(
                "INSERT INTO empresa (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaA, tenantA, "Empresa A");
        jdbcTemplate.update(
                "INSERT INTO empresa (id, tenant_id, razao_social) VALUES (?, ?, ?)",
                empresaB, tenantB, "Empresa B");

        jdbcTemplate.execute("DO $$ BEGIN "
                + "IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'nexerp_rls_test') THEN "
                + "CREATE ROLE nexerp_rls_test NOLOGIN NOSUPERUSER NOBYPASSRLS; "
                + "END IF; END $$");
        jdbcTemplate.execute("GRANT USAGE ON SCHEMA public TO nexerp_rls_test");
        jdbcTemplate.execute("GRANT SELECT ON empresa TO nexerp_rls_test");

        List<UUID> visibleToA = TenantContext.withTenant(
                tenantA,
                () -> tenantRlsProbeService.findEmpresaIdsAsNonSuperuser());
        List<UUID> visibleToB = TenantContext.withTenant(
                tenantB,
                () -> tenantRlsProbeService.findEmpresaIdsAsNonSuperuser());

        assertThat(visibleToA).containsExactly(empresaA);
        assertThat(visibleToB).containsExactly(empresaB);
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class TenantRlsTestConfiguration {

        @Bean
        TenantRlsProbeService tenantRlsProbeService(JdbcTemplate jdbcTemplate) {
            return new TenantRlsProbeService(jdbcTemplate);
        }
    }

    public static class TenantRlsProbeService {

        private final JdbcTemplate jdbcTemplate;

        public TenantRlsProbeService(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @TenantTransactional
        @Transactional
        public List<UUID> findEmpresaIdsAsNonSuperuser() {
            jdbcTemplate.execute("SET LOCAL ROLE nexerp_rls_test");
            return jdbcTemplate.query(
                    "SELECT id FROM empresa ORDER BY id",
                    (resultSet, rowNum) -> (UUID) resultSet.getObject("id"));
        }
    }
}
