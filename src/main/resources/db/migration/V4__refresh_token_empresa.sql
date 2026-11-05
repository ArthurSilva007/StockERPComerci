ALTER TABLE refresh_token
    ADD COLUMN empresa_id uuid;

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_empresa
        FOREIGN KEY (tenant_id, empresa_id)
        REFERENCES empresa (tenant_id, id);
