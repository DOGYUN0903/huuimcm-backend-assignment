-- 테스트용 초기 데이터
-- 모든 유저의 비밀번호: password123

-- 유저
INSERT INTO users (login_id, login_pw, name, created_at, updated_at) VALUES
('user1', '$2a$10$iwlGIHkNjgehRF2ih8pbr..Sx4oKyphK2T35nS6IBEMLj7tdd11oK', '김철수', NOW(), NOW()),
('user2', '$2a$10$iwlGIHkNjgehRF2ih8pbr..Sx4oKyphK2T35nS6IBEMLj7tdd11oK', '이영희', NOW(), NOW()),
('user3', '$2a$10$iwlGIHkNjgehRF2ih8pbr..Sx4oKyphK2T35nS6IBEMLj7tdd11oK', '박민수', NOW(), NOW());

-- 상품
INSERT INTO products (seller_id, name, description, price, stock, brand, like_count, created_at, updated_at) VALUES
(1, '나이키 에어맥스 90', '클래식한 디자인의 나이키 에어맥스 90 운동화', 159000, 100, '나이키', 0, NOW(), NOW()),
(1, '나이키 덩크 로우', '레트로 감성의 나이키 덩크 로우', 129000, 50, '나이키', 0, NOW(), NOW()),
(2, '아디다스 울트라부스트', '편안한 착용감의 아디다스 울트라부스트', 199000, 80, '아디다스', 0, NOW(), NOW()),
(2, '아디다스 스탠스미스', '깔끔한 디자인의 아디다스 스탠스미스', 119000, 120, '아디다스', 0, NOW(), NOW()),
(3, '뉴발란스 993', '프리미엄 뉴발란스 993 러닝화', 259000, 30, '뉴발란스', 0, NOW(), NOW()),
(3, '뉴발란스 530', '뉴발란스 530 레트로 러닝화', 139000, 200, '뉴발란스', 0, NOW(), NOW()),
(1, '나이키 에어포스 1', '나이키의 대표 스니커즈 에어포스 1', 139000, 150, '나이키', 0, NOW(), NOW()),
(2, '아디다스 삼바', '클래식 축구 스타일의 아디다스 삼바', 129000, 90, '아디다스', 0, NOW(), NOW());
