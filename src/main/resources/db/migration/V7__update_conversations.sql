-- V7__update_conversations.sql
ALTER TABLE conversations 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN last_message_id BIGINT,
ADD COLUMN last_message_at TIMESTAMP;
