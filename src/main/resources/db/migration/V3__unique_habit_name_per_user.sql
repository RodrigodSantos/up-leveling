-- Nome de hábito único por usuário, ignorando maiúsculas.
-- Índice parcial (WHERE): os excluídos não contam, então dá para recriar um hábito com o nome de um que foi excluído.
-- Migration nova em vez de editar a V2: o Flyway guarda o checksum de cada arquivo já aplicado e recusa subir se ele mudar.
CREATE UNIQUE INDEX uk_habits_user_name ON habits (user_id, LOWER(name)) WHERE status <> 'DELETED';
