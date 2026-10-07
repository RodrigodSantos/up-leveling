-- Cada linha é um "cumpri o hábito". O XP total do usuário é a soma destas linhas (não existe saldo guardado):
-- desfazer um check-in é apagar a linha, e o XP se ajusta sozinho.
CREATE TABLE check_ins (
    id            BIGSERIAL PRIMARY KEY,
    habit_id      BIGINT    NOT NULL REFERENCES habits (id),
    user_id       BIGINT    NOT NULL REFERENCES users (id), -- repetido do hábito para somar o XP sem JOIN
    check_in_date DATE      NOT NULL,
    xp            INTEGER   NOT NULL CHECK (xp > 0),
    bonus_xp      INTEGER   NOT NULL DEFAULT 0 CHECK (bonus_xp >= 0), -- bônus de streak (a cada 7 dias seguidos)
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_check_ins_habit_date ON check_ins (habit_id, check_in_date);
CREATE INDEX idx_check_ins_user ON check_ins (user_id);
