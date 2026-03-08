CREATE TABLE xp_transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    habit_log_id INTEGER,
    xp_amount INTEGER NOT NULL,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_xp_transactions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_xp_transactions_habit_log
        FOREIGN KEY (habit_log_id)
        REFERENCES habit_logs(id)
);