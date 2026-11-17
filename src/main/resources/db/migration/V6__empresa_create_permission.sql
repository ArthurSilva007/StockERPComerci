INSERT INTO permissao (codigo, descricao)
VALUES ('empresa:criar', 'Criar empresas')
    ON CONFLICT (codigo) DO NOTHING;