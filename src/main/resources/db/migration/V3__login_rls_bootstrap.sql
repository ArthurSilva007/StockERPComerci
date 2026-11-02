CREATE OR REPLACE FUNCTION app_current_empresa_id()
RETURNS uuid
LANGUAGE sql
STABLE
AS $$
SELECT NULLIF(current_setting('app.empresa_id', true), '')::uuid
$$;

DROP POLICY empresa_tenant_isolation ON empresa;

CREATE POLICY empresa_tenant_isolation ON empresa
    USING (
        tenant_id = app_current_tenant_id()
        OR id = app_current_empresa_id()
    )
    WITH CHECK (
        tenant_id = app_current_tenant_id()
    );