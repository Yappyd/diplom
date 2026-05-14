CREATE TABLE messages
(
    message_id UUID PRIMARY KEY,
    chat_id    UUID        NOT NULL,
    sender_id  UUID        NOT NULL,
    content    TEXT        NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    CHECK (LENGTH(TRIM(content)) > 0),
    CHECK (LENGTH(content) <= 4000)
);