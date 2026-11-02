ALTER TABLE empresa
    ADD CONSTRAINT uq_empresa_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE perfil (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL REFERENCES tenant(id),
    nome varchar(80) NOT NULL,
    sistema boolean NOT NULL DEFAULT false,
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_perfil_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_perfil_tenant_nome UNIQUE (tenant_id, nome)
);

CREATE TABLE permissao (
    codigo varchar(80) PRIMARY KEY,
    descricao varchar(200) NOT NULL
);

CREATE TABLE perfil_permissao (
    perfil_id uuid NOT NULL REFERENCES perfil(id),
    permissao_codigo varchar(80) NOT NULL REFERENCES permissao(codigo),
    criado_em timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (perfil_id, permissao_codigo)
);

CREATE TABLE usuario (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL REFERENCES tenant(id),
    nome varchar(160) NOT NULL,
    email varchar(254) NOT NULL,
    senha_hash varchar(255) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'ATIVO',
    tentativas_falhas smallint NOT NULL DEFAULT 0,
    bloqueado_ate timestamptz,
    dois_fatores_habilitado boolean NOT NULL DEFAULT false,
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(),
    ultimo_login_em timestamptz,
    CONSTRAINT uq_usuario_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_usuario_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT ck_usuario_status CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO')),
    CONSTRAINT ck_usuario_tentativas CHECK (tentativas_falhas >= 0)
);

CREATE INDEX idx_usuario_tenant_status ON usuario (tenant_id, status);

CREATE TABLE usuario_empresa (
    tenant_id uuid NOT NULL REFERENCES tenant(id),
    usuario_id uuid NOT NULL,
    empresa_id uuid NOT NULL,
    perfil_id uuid NOT NULL,
    criado_em timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (usuario_id, empresa_id),
    CONSTRAINT fk_usuario_empresa_usuario
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuario (tenant_id, id),
    CONSTRAINT fk_usuario_empresa_empresa
        FOREIGN KEY (tenant_id, empresa_id)
        REFERENCES empresa (tenant_id, id),
    CONSTRAINT fk_usuario_empresa_perfil
        FOREIGN KEY (tenant_id, perfil_id)
        REFERENCES perfil (tenant_id, id)
);

CREATE INDEX idx_usuario_empresa_empresa ON usuario_empresa (tenant_id, empresa_id);

CREATE TABLE refresh_token (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL REFERENCES tenant(id),
    usuario_id uuid NOT NULL,
    token_hash varchar(255) NOT NULL UNIQUE,
    criado_em timestamptz NOT NULL DEFAULT now(),
    expira_em timestamptz NOT NULL,
    revogado_em timestamptz,
    substituido_por_id uuid REFERENCES refresh_token(id),
    CONSTRAINT fk_refresh_token_usuario
        FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuario (tenant_id, id),
    CONSTRAINT ck_refresh_token_expiracao CHECK (expira_em > criado_em)
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token (tenant_id, usuario_id);
CREATE INDEX idx_refresh_token_expira ON refresh_token (expira_em);

INSERT INTO permissao (codigo, descricao) VALUES
    ('usuario:admin', 'Administrar usuários, perfis e permissões'),
    ('auditoria:ler', 'Consultar auditoria'),
    ('config:admin', 'Administrar configurações'),
    ('produto:ler', 'Consultar produtos'),
    ('estoque:ler', 'Consultar estoque'),
    ('venda:criar', 'Criar vendas'),
    ('relatorio:ler', 'Consultar relatórios')
ON CONFLICT (codigo) DO NOTHING;

CREATE OR REPLACE FUNCTION app_current_tenant_id()
RETURNS uuid
LANGUAGE sql
STABLE
AS $$
    SELECT NULLIF(current_setting('app.tenant_id', true), '')::uuid
$$;

ALTER TABLE empresa ENABLE ROW LEVEL SECURITY;
ALTER TABLE empresa FORCE ROW LEVEL SECURITY;
CREATE POLICY empresa_tenant_isolation ON empresa
    USING (tenant_id = app_current_tenant_id())
    WITH CHECK (tenant_id = app_current_tenant_id());

ALTER TABLE perfil ENABLE ROW LEVEL SECURITY;
ALTER TABLE perfil FORCE ROW LEVEL SECURITY;
CREATE POLICY perfil_tenant_isolation ON perfil
    USING (tenant_id = app_current_tenant_id())
    WITH CHECK (tenant_id = app_current_tenant_id());

ALTER TABLE usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuario FORCE ROW LEVEL SECURITY;
CREATE POLICY usuario_tenant_isolation ON usuario
    USING (tenant_id = app_current_tenant_id())
    WITH CHECK (tenant_id = app_current_tenant_id());

ALTER TABLE usuario_empresa ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuario_empresa FORCE ROW LEVEL SECURITY;
CREATE POLICY usuario_empresa_tenant_isolation ON usuario_empresa
    USING (tenant_id = app_current_tenant_id())
    WITH CHECK (tenant_id = app_current_tenant_id());

ALTER TABLE refresh_token ENABLE ROW LEVEL SECURITY;
ALTER TABLE refresh_token FORCE ROW LEVEL SECURITY;
CREATE POLICY refresh_token_tenant_isolation ON refresh_token
    USING (tenant_id = app_current_tenant_id())
    WITH CHECK (tenant_id = app_current_tenant_id());

-- O usuário da aplicação receberá permissões mínimas e não será superusuário
-- durante o hardening/deploy. O contexto app.tenant_id será aplicado pelo
-- interceptor transacional da Etapa 1.2.
