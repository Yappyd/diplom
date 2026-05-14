CREATE INDEX idx_messages_chat_created_at
    ON messages (chat_id, created_at DESC);

CREATE INDEX idx_messages_sender_id
    ON messages (sender_id);