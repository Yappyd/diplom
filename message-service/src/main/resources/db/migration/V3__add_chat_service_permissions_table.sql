CREATE TABLE chat_message_permissions
(
    chat_id UUID NOT NULL,
    user_id UUID NOT NULL,

    can_delete_any_messages BOOLEAN NOT NULL DEFAULT FALSE,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (chat_id, user_id)
);

CREATE INDEX idx_chat_message_permissions_user_id
    ON chat_message_permissions (user_id);