CREATE INDEX idx_private_chats_user_a
    ON private_chats (user_a);

CREATE INDEX idx_private_chats_user_b
    ON private_chats (user_b);

CREATE INDEX idx_chat_participants_user_id
    ON chat_participants (user_id);