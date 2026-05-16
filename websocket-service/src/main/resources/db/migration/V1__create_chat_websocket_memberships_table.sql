CREATE TABLE chat_websocket_memberships
(
    chat_id    UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (chat_id, user_id)
);

CREATE INDEX idx_chat_websocket_memberships_user_id
    ON chat_websocket_memberships (user_id);