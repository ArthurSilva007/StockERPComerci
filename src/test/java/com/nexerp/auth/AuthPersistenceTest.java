package com.nexerp.auth;

import com.nexerp.auth.domain.Perfil;
import com.nexerp.auth.domain.Tenant;
import com.nexerp.auth.infra.PerfilRepository;
import com.nexerp.auth.infra.TenantRepository;
import com.nexerp.empresas.domain.Empresa;
import com.nexerp.empresas.infra.EmpresaRepository;
import com.nexerp.usuarios.domain.Usuario;
import com.nexerp.usuarios.infra.UsuariosRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class AuthPersistenceTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("nexerp_auth_test")
                    .withUsername("nexerp_auth_test")
                    .withPassword("nexerp_auth_test");

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @DynamicPropertySource
    static void configureDataSource(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRESQL::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRESQL::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRESQL::getPassword
        );
    }

    @Test
    void persistsTenantEmpresaUsuarioAndPerfil() {
        Tenant tenant = tenantRepository.save(
                new Tenant("Test Tenant")
        );

        Empresa empresa = empresaRepository.save(
                new Empresa(
                        tenant.getId(),
                        "Empresa TESTE LTDA",
                        "Empresa teste",
                        "12345678000195"
                )
        );

        Usuario usuario = usuariosRepository.save(
                new Usuario(
                        tenant.getId(),
                        "Usuario Teste",
                        "usuario@teste.com",
                        "$2a$12$hash-de-teste"
                )
        );

        Perfil perfil = perfilRepository.save(
                new Perfil(
                        tenant.getId(),
                        "Operador de Caixa",
                        false
                )
        );

        assertThat(tenant.getId()).isNotNull();
        assertThat(empresa.getId()).isNotNull();
        assertThat(usuario.getId()).isNotNull();
        assertThat(perfil.getId()).isNotNull();

        assertThat(
                empresaRepository.findAllByTenantIdOrderByRazaoSocialAsc(
                        tenant.getId()
                )
        )
                .singleElement()
                .extracting(Empresa::getRazaoSocial)
                .isEqualTo("Empresa TESTE LTDA");

        assertThat(
                usuariosRepository.findByTenantIdAndEmail(
                        tenant.getId(),
                        "usuario@teste.com"
                )
        ).isPresent();

        assertThat(
                perfilRepository.findByTenantIdAndNome(
                        tenant.getId(),
                        "Operador de Caixa"
                )
        ).isPresent();
    }
}
