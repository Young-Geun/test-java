# H2 Database 사용 가이드

## H2 Console 접속 방법
1. 애플리케이션 실행 후 브라우저에서 다음 URL 접속:
   ```
   http://localhost:8080/api/h2-console
   ```

## H2 Console 접속 정보
아래 정보로 로그인:
- JDBC URL: `jdbc:h2:mem:testdb`
- User Name: `sa`
- Password: (비워두기)

## 초기 테스트 사용자 생성
H2 Console에 접속 후 아래 SQL을 실행:
```sql
-- users 테이블 확인
SELECT * FROM USERS;

-- 테스트용 사용자 생성 (password: password123)
INSERT INTO USERS 
(USER_ID, PASSWORD, EMAIL, ENABLED, ACCOUNT_NON_LOCKED, FAILED_ATTEMPT, CREATED_AT, UPDATED_AT) 
VALUES 
('testuser', '$2a$10$8KxX3XZm3CS3Zw3HExwz7.wPLt76E1GkTJr5xV3lq8Jq/GRA2K8.q', 'test@example.com', true, true, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 생성된 사용자 확인
SELECT * FROM USERS;
```

## API 테스트 순서
1. 새로운 사용자 등록:
   ```
   POST http://localhost:8080/api/auth/signup
   Content-Type: application/json

   {
     "userId": "newuser",
     "password": "password123",
     "email": "newuser@example.com"
   }
   ```

2. 로그인하여 JWT 토큰 발급:
   ```
   POST http://localhost:8080/api/auth/login
   Content-Type: application/json

   {
     "userId": "newuser",
     "password": "password123"
   }
   ```

3. 발급받은 토큰으로 API 호출:
   ```
   GET http://localhost:8080/api/users
   Authorization: Bearer {발급받은_토큰}
   ```

## 데이터베이스 테이블 구조
```sql
-- 테이블 구조 확인
SHOW COLUMNS FROM USERS;

-- 예상 결과:
-- ID (BIGINT, PK)
-- USER_ID (VARCHAR, UNIQUE)
-- PASSWORD (VARCHAR)
-- EMAIL (VARCHAR, UNIQUE)
-- ENABLED (BOOLEAN)
-- ACCOUNT_NON_LOCKED (BOOLEAN)
-- FAILED_ATTEMPT (INTEGER)
-- LOCK_TIME (TIMESTAMP)
-- CREATED_AT (TIMESTAMP)
-- UPDATED_AT (TIMESTAMP)
```

## 주의사항
- H2는 인메모리 데이터베이스이므로 애플리케이션을 재시작하면 데이터가 초기화됩니다.
- 실제 운영 환경에서는 영구 데이터베이스(예: MySQL, PostgreSQL)를 사용해야 합니다.
- 비밀번호는 BCrypt로 암호화되어 저장됩니다.
