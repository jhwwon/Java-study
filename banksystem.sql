DROP TABLE transactions CASCADE CONSTRAINTS;
DROP TABLE accounts CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;

DROP SEQUENCE seq_account;
DROP SEQUENCE seq_transaction;

DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM users;

-- 사용자 테이블 
CREATE TABLE users (
    user_id VARCHAR2(8) PRIMARY KEY,          -- 아이디
    user_name VARCHAR2(20) NOT NULL,		  -- 이름	
    user_password VARCHAR2(12) NOT NULL,	  -- 비밀번호
    user_email VARCHAR2(100) NOT NULL UNIQUE, -- 이메일
    user_phone VARCHAR2(13) NOT NULL UNIQUE,  -- 전화번호
    join_date DATE DEFAULT SYSDATE            -- 회원가입 날짜
);


-- 계좌 테이블
CREATE TABLE accounts (
    account_id VARCHAR2(15) PRIMARY KEY,					-- 계좌번호                               
    account_name VARCHAR2(50) NOT NULL,						-- 계좌명
    account_type VARCHAR2(20) NOT NULL 						-- 계좌종류
    	CHECK (account_type IN ('보통예금', '정기예금', '적금')),
    account_password VARCHAR2(4) NOT NULL,					-- 계좌 비밀번호
    balance NUMBER(15,2) DEFAULT 0 CHECK (balance >= 0),	-- 잔액 (음수 불가)
    user_id VARCHAR2(8) NOT NULL,							-- 계좌 소유자
    create_date DATE DEFAULT SYSDATE,						-- 계좌 개설일
    CONSTRAINT fk_accounts_user_id                          -- accounts 테이블의 user_id는 반드시 users 테이블에 존재하는 user_id
    	FOREIGN KEY (user_id) REFERENCES users(user_id)
);


-- 거래내역 테이블
CREATE TABLE transactions (
    transaction_id      VARCHAR2(10)    PRIMARY KEY,        				-- 거래번호
    transaction_date    DATE            DEFAULT SYSDATE,    				-- 거래일시
    account_id          VARCHAR2(15)    NOT NULL,           				-- 거래 대상 계좌
    transaction_type    VARCHAR2(20)    NOT NULL,           				-- 거래유형 (입금/출금/이체입금/이체출금)
    amount              NUMBER(15,2)    NOT NULL CHECK (amount > 0),        -- 거래금액 (양수만)
    balance_after       NUMBER(15,2)    NOT NULL CHECK (balance_after >= 0),-- 거래 후 잔액
    counterpart_account VARCHAR2(15),                       				-- 상대방 계좌번호 (이체시만)
    counterpart_name    VARCHAR2(20),                       				-- 상대방 이름 (이체시)
    depositor_name      VARCHAR2(20),                       				-- 입금자명 (입금시)
    transaction_memo    VARCHAR2(100),                      				-- 거래 메모
    CONSTRAINT fk_transactions_account_id 
        FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE  
);  -- 거래는 실제 계좌에서만 가능, 계좌를 지우면 그 계좌의 거래내역도 사라짐


-- 시퀀스 생성
-- 계좌번호 생성용 시퀀스 (110-234-000001 형태)
CREATE SEQUENCE seq_account
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 999999
    NOCYCLE
    NOCACHE;

-- 거래번호 생성용 시퀀스 (T00000001 형태)
CREATE SEQUENCE seq_transaction
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 99999999
    NOCYCLE
    NOCACHE;

COMMIT;