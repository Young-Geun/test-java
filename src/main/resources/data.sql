-- Initial test users
INSERT INTO USERS 
(USER_ID, PASSWORD, EMAIL, ENABLED, ACCOUNT_NON_LOCKED, FAILED_ATTEMPT, CREATED_AT, UPDATED_AT) 
VALUES 
('admin', '$2a$10$8KxX3XZm3CS3Zw3HExwz7.wPLt76E1GkTJr5xV3lq8Jq/GRA2K8.q', 'admin@example.com', true, true, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('user1', '$2a$10$8KxX3XZm3CS3Zw3HExwz7.wPLt76E1GkTJr5xV3lq8Jq/GRA2K8.q', 'user1@example.com', true, true, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Passwords for both users are 'password123'
-- You can generate new BCrypt passwords using: https://bcrypt-generator.com/ (use 10 rounds)
