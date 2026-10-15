-- Cores de destaque salvas pelo usuário, além das 5 padrão do app: até 5 matizes (0–359°), na ordem em que
-- foram salvas. Array do Postgres em vez de outra tabela: é uma lista pequena, sempre lida e gravada inteira.
-- Usuários existentes começam sem nenhuma.
ALTER TABLE users
    ADD COLUMN accent_hues INTEGER[] NOT NULL DEFAULT '{}'
        CHECK (cardinality(accent_hues) <= 5);
