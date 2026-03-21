CREATE TABLE private_chats
(
    chat_id UUID PRIMARY KEY REFERENCES chats (chat_id) ON DELETE CASCADE,
    user_a  UUID NOT NULL,
    user_b  UUID NOT NULL,

    CONSTRAINT uq_private_pair UNIQUE (user_a, user_b),
    CHECK (user_a < user_b)
);