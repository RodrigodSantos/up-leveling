-- Meta diária: quantos check-ins o hábito pede por dia (ex.: "Beber água" 8 vezes). Hábitos existentes ficam com 1.
ALTER TABLE habits
    ADD COLUMN daily_target INTEGER NOT NULL DEFAULT 1 CHECK (daily_target BETWEEN 1 AND 20);
