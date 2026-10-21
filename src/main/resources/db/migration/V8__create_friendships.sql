-- Uma linha por par de pessoas: nasce PENDING (pedido enviado) e vira ACCEPTED quando a outra aceita.
-- Recusar, cancelar o pedido ou desfazer a amizade apaga a linha (dá para mandar um pedido de novo depois).
-- É a base da futura rotina de amizade: só quem tem uma linha ACCEPTED poderá entrar na rotina do outro.
CREATE TABLE friendships (
    id           BIGSERIAL   PRIMARY KEY,
    requester_id BIGINT      NOT NULL REFERENCES users (id), -- quem enviou o pedido
    addressee_id BIGINT      NOT NULL REFERENCES users (id), -- quem recebeu (só ele pode aceitar)
    status       VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED')),
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    accepted_at  TIMESTAMP,
    CHECK (requester_id <> addressee_id)
);

-- Um par só, nos dois sentidos: se Ana já pediu Bruno, Bruno não cria um segundo pedido para Ana.
-- LEAST/GREATEST põe o par sempre na mesma ordem (menor id, maior id), qualquer que seja quem pediu.
CREATE UNIQUE INDEX uk_friendships_pair ON friendships (LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id));
CREATE INDEX idx_friendships_requester ON friendships (requester_id);
CREATE INDEX idx_friendships_addressee ON friendships (addressee_id);
