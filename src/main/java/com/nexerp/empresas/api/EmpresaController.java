package com.nexerp.empresas.api;

import com.nexerp.auth.domain.Tenant;
import com.nexerp.empresas.application.EmpresaService;
import com.nexerp.shared.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }
    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> listar(
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(
                jwt.getClaimAsString("tenant_id")
        );
        return TenantContext.withTenant(
                tenantId,
                () -> ResponseEntity.ok(empresaService.listar())
        );
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse>criar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EmpresaRequest request
    ){
        UUID tenantId = UUID.fromString(
                jwt.getClaimAsString("tenant_id")
        );

        return TenantContext.withTenant(
                tenantId,
                () -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(empresaService.criar(request))
        );
    }
}
