INSERT INTO permissao (codigo, descricao)
VALUES ('empresa:ler', 'Consultar empresas')
    ON CONFLICT (codigo) DO NOTHING;