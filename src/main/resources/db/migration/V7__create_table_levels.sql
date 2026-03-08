CREATE TABLE levels (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    level_number INTEGER NOT NULL,
    xp_required INTEGER NOT NULL,
    status status_type DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_levels_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT unique_user_level
        UNIQUE (user_id, level_number)
);