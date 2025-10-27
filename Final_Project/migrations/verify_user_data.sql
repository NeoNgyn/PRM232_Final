-- Script to verify and update User table for login testing

-- 1. Check current data
SELECT user_id, fullname, email, password, CHAR_LENGTH(password) as pwd_length, is_active
FROM `User`;

-- 2. If data doesn't exist, insert test users
-- Make sure passwords are stored as plain text for testing
INSERT INTO `User` (user_id, fullname, email, password, is_active, create_at, update_at)
VALUES
    ('U001', 'Nguyễn Văn An', 'an@gmail.com', 'hashed_pw_1', 1, NOW(), NOW()),
    ('U002', 'Lê Thị Hoa', 'hoa@gmail.com', 'hashed_pw_2', 1, NOW(), NOW()),
    ('U003', 'Trần Minh Khang', 'khang@gmail.com', 'hashed_pw_3', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    update_at = NOW();

-- 3. Verify the data was inserted correctly
SELECT user_id, fullname, email, password, is_active
FROM `User`
WHERE email IN ('an@gmail.com', 'hoa@gmail.com', 'khang@gmail.com');

-- 4. Test login queries (these are what the app will run)
SELECT password FROM `User` WHERE LOWER(email) = 'an@gmail.com';
SELECT password FROM `User` WHERE LOWER(email) = 'hoa@gmail.com';
SELECT password FROM `User` WHERE LOWER(email) = 'khang@gmail.com';

