CREATE TABLE habit_schedule (
    id SERIAL PRIMARY KEY,
    habit_id INTEGER NOT NULL,
    day_of_week INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_habit_schedule_habit
        FOREIGN KEY (habit_id)
        REFERENCES habits(id),

    CONSTRAINT unique_habit_day
        UNIQUE (habit_id, day_of_week),

    CONSTRAINT check_day_of_week
        CHECK (day_of_week BETWEEN 1 AND 7)
);