# Spring Boot REST API with JWT Authentication

## 프로젝트 개요
- Spring Boot 3.2.1 기반의 REST API 서버
- JWT 기반 인증
- H2 인메모리 데이터베이스 사용
- 사용자 관리 및 인증 기능 구현

## 시작하기

### 1. 프로젝트 실행
```bash
# Maven으로 실행
./mvnw spring-boot:run
```

### 2. 기본 제공 계정
애플리케이션 시작 시 자동으로 생성되는 테스트 계정:
- 관리자: admin/password123
- 일반사용자: user1/password123

### 3. H2 데이터베이스 콘솔 접속
1. 브라우저에서 접속: http://localhost:8080/api/h2-console
2. 접속 정보:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (비워두기)

## API 엔드포인트

### 인증
1. 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
    "userId": "newuser",
    "password": "password123",
    "email": "newuser@example.com"
}
```

2. 로그인 (토큰 발급)
```http
POST /api/auth/login
Content-Type: application/json

{
    "userId": "newuser",
    "password": "password123"
}
```

### 사용자 관리
3. 전체 사용자 조회 (인증 필요)
```http
GET /api/users
Authorization: Bearer {JWT_TOKEN}
```

## 보안 기능
- JWT 기반 인증
- 비밀번호 BCrypt 암호화
- 로그인 실패 5회시 계정 잠금 (24시간)
- 토큰 기반 API 접근 제어

## 데이터베이스 스키마
```sql
CREATE TABLE USERS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID VARCHAR(255) UNIQUE NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    EMAIL VARCHAR(255) UNIQUE NOT NULL,
    ENABLED BOOLEAN DEFAULT TRUE,
    ACCOUNT_NON_LOCKED BOOLEAN DEFAULT TRUE,
    FAILED_ATTEMPT INT DEFAULT 0,
    LOCK_TIME TIMESTAMP,
    CREATED_AT TIMESTAMP,
    UPDATED_AT TIMESTAMP
);
```

## 주의사항
- H2는 인메모리 데이터베이스로, 애플리케이션 재시작 시 데이터가 초기화됩니다.
- 실제 운영 환경에서는 영구 데이터베이스로 전환이 필요합니다.
- 초기 제공되는 테스트 계정은 개발 목적으로만 사용해야 합니다.

## 테스트
```bash
# 전체 테스트 실행
./mvnw test

# 테스트 커버리지 리포트 생성
./mvnw test jacoco:report
```
테스트 커버리지 리포트는 `target/site/jacoco/index.html`에서 확인할 수 있습니다.
