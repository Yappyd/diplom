CREATE TABLE chats
(
    chat_id    UUID PRIMARY KEY,
    type       VARCHAR(20) NOT NULL,
    title      VARCHAR(255),

    created_by UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (
        (type = 'GROUP' AND title IS NOT NULL)
            OR
        (type = 'PRIVATE')
        )
);

