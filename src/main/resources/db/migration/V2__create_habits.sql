CREATE TABLE habits (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    name       VARCHAR(100) NOT NULL,
    xp_reward  INTEGER      NOT NULL CHECK (xp_reward BETWEEN 1 AND 100),
    -- DELETED mantém a linha (exclusão lógica), para os check-ins e o XP antigos continuarem apontando para ela
    status     VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PAUSED', 'DELETED')),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_habits_user ON habits (user_id);

-- Dias da semana em que o hábito vale (nenhuma linha = todos os dias)
CREATE TABLE habit_schedule (
    habit_id    BIGINT   NOT NULL REFERENCES habits (id),
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1 = segunda (ISO, igual ao java.time.DayOfWeek)
    PRIMARY KEY (habit_id, day_of_week)
);
