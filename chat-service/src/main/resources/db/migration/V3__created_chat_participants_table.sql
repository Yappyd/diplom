CREATE TABLE chat_participants
(
    chat_id    UUID        NOT NULL REFERENCES chats (chat_id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL,
    role       VARCHAR(20) NOT NULL,
    nickname   VARCHAR(255),

    joined_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (chat_id, user_id)
);