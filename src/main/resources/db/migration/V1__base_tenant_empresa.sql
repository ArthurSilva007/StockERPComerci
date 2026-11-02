CREATE TABLE tenant (
    id uuid PRIMARY KEY,
    nome varchar(200) NOT NULL,
    criado_em timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE empresa (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL REFERENCES tenant(id),
    razao_social varchar(200) NOT NULL,
    nome_fantasia varchar(200),
    cnpj varchar(14),
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_empresa_tenant_cnpj UNIQUE (tenant_id, cnpj)
);

CREATE INDEX idx_empresa_tenant ON empresa (tenant_id);

COMMENT ON TABLE tenant IS 'Conta contratante do NexERP.';
COMMENT ON TABLE empresa IS 'Estabelecimento/CNPJ pertencente a um tenant.';
COMMENT ON COLUMN empresa.cnpj IS 'Somente dígitos; validação de documento ocorrerá na camada de aplicação.';

-- RLS e políticas completas serão criadas na Etapa 1,
-- junto com o contexto transacional de tenant e os testes de isolamento.
