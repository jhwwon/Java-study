-- 은행 계좌 관리 시스템 DB 설정 스크립트
-- Oracle Database 기준

-- 1. 기존 테이블 및 시퀀스 삭제 (초기화)
DROP TABLE accounts CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;
DROP SEQUENCE SEQ_ACCOUNT;

-- 2. 사용자 테이블 생성
CREATE TABLE users (
    user_id VARCHAR2(50) PRIMARY KEY,
    user_name VARCHAR2(100) NOT NULL,
    user_password VARCHAR2(100) NOT NULL,
    user_email VARCHAR2(100) NOT NULL,
    user_phone VARCHAR2(20) NOT NULL,
    join_date DATE DEFAULT SYSDATE
);

-- 3. 계좌 테이블 생성
CREATE TABLE accounts (
    account_id VARCHAR2(20) PRIMARY KEY,
    account_name VARCHAR2(100) NOT NULL,
    account_type VARCHAR2(20) NOT NULL,
    account_password VARCHAR2(4) NOT NULL,
    balance NUMBER(15,2) DEFAULT 0,
    user_id VARCHAR2(50) NOT NULL,
    create_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_balance CHECK (balance >= 0),
    CONSTRAINT chk_account_type CHECK (account_type IN ('보통예금', '정기예금', '적금')),
    CONSTRAINT chk_account_password CHECK (LENGTH(account_password) = 4 AND REGEXP_LIKE(account_password, '^[0-9]+$'))
);

-- 4. 계좌번호 시퀀스 생성
CREATE SEQUENCE SEQ_ACCOUNT START WITH 1 INCREMENT BY 1;

-- 5. 샘플 데이터 삽입
-- 관리자 계정
INSERT INTO users VALUES ('admin', '관리자', 'password', 'admin@bank.com', '010-0000-0000', SYSDATE);

-- 일반 사용자
INSERT INTO users VALUES ('hong123', '홍길동', 'hong1234', 'hong@email.com', '010-1111-1111', SYSDATE);
INSERT INTO users VALUES ('kim456', '김철수', 'kim12345', 'kim@email.com', '010-2222-2222', SYSDATE);

-- 샘플 계좌
INSERT INTO accounts VALUES ('110-234-000001', '보통예금 계좌_홍길동', '보통예금', '1234', 1000000, 'hong123', SYSDATE);
INSERT INTO accounts VALUES ('110-234-000002', '적금 계좌_김철수', '적금', '5678', 500000, 'kim456', SYSDATE);

-- 6. 데이터 확인
SELECT '데이터베이스 설정 완료' AS MESSAGE FROM DUAL;

-- 사용자 목록 확인
SELECT * FROM users;

-- 계좌 목록 확인
SELECT 
    a.account_id,
    a.account_name,
    a.account_type,
    a.balance,
    u.user_name
FROM accounts a
JOIN users u ON a.user_id = u.user_id;

COMMIT;