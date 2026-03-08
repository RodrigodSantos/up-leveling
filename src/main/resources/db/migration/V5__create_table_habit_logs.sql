CREATE TABLE habit_logs (
    id SERIAL PRIMARY KEY,
    habit_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    execution_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_habit_logs_habit
        FOREIGN KEY (habit_id)
        REFERENCES habits(id),

    CONSTRAINT fk_habit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);