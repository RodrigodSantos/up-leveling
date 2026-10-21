-- Código de amigo: o que uma pessoa passa para a outra adicioná-la (ex.: UPL-7K3Q9M).
-- Código em vez de e-mail: ninguém descobre quais e-mails têm conta, e o e-mail não fica exposto para estranhos.
-- Alfabeto sem 0/O e 1/I, que se confundem quando alguém dita ou copia o código à mão.
ALTER TABLE users ADD COLUMN friend_code VARCHAR(10);

-- Usuários existentes ganham um código sorteado. O "WHERE users.id IS NOT NULL" liga a subconsulta à linha:
-- sem ele, o Postgres sorteia uma vez só e daria o mesmo código para todos. Os novos são gerados no Java (FriendCode).
UPDATE users
SET friend_code = 'UPL-' || (SELECT string_agg(substr('23456789ABCDEFGHJKLMNPQRSTUVWXYZ', 1 + floor(random() * 32)::int, 1), '')
                             FROM generate_series(1, 6)
                             WHERE users.id IS NOT NULL);

ALTER TABLE users ALTER COLUMN friend_code SET NOT NULL;
CREATE UNIQUE INDEX uk_users_friend_code ON users (friend_code);
