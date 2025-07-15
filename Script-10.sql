-- 은행 시스템 데이터베이스 스키마 생성 스크립트

-- 기존 테이블과 제약조건 삭제 (존재할 경우)
DROP TABLE transactions CASCADE CONSTRAINTS;
DROP TABLE accounts CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;

-- 시퀀스 삭제 (존재할 경우)
DROP SEQUENCE seq_transaction_id;

-- 1. 사용자 테이블 생성
CREATE TABLE users (
    user_id VARCHAR2(20) PRIMARY KEY,
    user_name VARCHAR2(50) NOT NULL,
    password VARCHAR2(100) NOT NULL,
    created_date DATE DEFAULT SYSDATE
);

-- 2. 계좌 테이블 생성
CREATE TABLE accounts (
    account_id VARCHAR2(20) PRIMARY KEY,
    owner_id VARCHAR2(20) NOT NULL,
    account_holder_name VARCHAR2(50) NOT NULL,
    account_type VARCHAR2(50) NOT NULL,        -- 계좌 타입 길이 확장
    account_password VARCHAR2(4) NOT NULL,
    balance NUMBER(15,2) DEFAULT 0,
    account_alias VARCHAR2(50),
    created_date DATE DEFAULT SYSDATE,
    interest_rate NUMBER(5,2) DEFAULT 0,
    last_interest_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_accounts_owner FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. 거래내역 테이블 생성
CREATE TABLE transactions (
    transaction_id NUMBER PRIMARY KEY,
    account_id VARCHAR2(20) NOT NULL,
    transaction_type VARCHAR2(20) NOT NULL,
    amount NUMBER(15,2) NOT NULL,
    detail VARCHAR2(200),
    transaction_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- 4. 거래내역 ID용 시퀀스 생성
CREATE SEQUENCE seq_transaction_id 
    START WITH 1 
    INCREMENT BY 1 
    NOCACHE;

-- 5. 성능 향상을 위한 인덱스 생성
CREATE INDEX idx_accounts_owner ON accounts(owner_id);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);

-- 6. 테스트용 샘플 사용자 생성
INSERT INTO users (user_id, user_name, password) VALUES ('test01', '테스트사용자', '1234');
INSERT INTO users (user_id, user_name, password) VALUES ('kim123', '김철수', 'password');

-- 7. 커밋
COMMIT;

-- 8. 확인 쿼리
SELECT 'USERS' as TABLE_NAME, COUNT(*) as COUNT FROM users
UNION ALL
SELECT 'ACCOUNTS' as TABLE_NAME, COUNT(*) as COUNT FROM accounts
UNION ALL
SELECT 'TRANSACTIONS' as TABLE_NAME, COUNT(*) as COUNT FROM transactions;

-- 테이블 구조 확인
-- SELECT * FROM users;
-- SELECT * FROM accounts; 
-- SELECT * FROM transactions;