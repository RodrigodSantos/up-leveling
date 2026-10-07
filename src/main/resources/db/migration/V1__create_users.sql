CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL, -- o hash do BCrypt tem 60 caracteres
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Único ignorando maiúsculas: "Ana@Mail.com" e "ana@mail.com" são a mesma conta
CREATE UNIQUE INDEX uk_users_email ON users (LOWER(email));
