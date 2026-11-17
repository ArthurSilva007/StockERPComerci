package com.nexerp.empresas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
        @NotBlank
        @Size(max = 200)
        String razaoSocial,


        @Size(max = 200)
        String nomeFantasia,

        @Pattern(regexp = "\\d{14}")
        String cnpj
) {
}
