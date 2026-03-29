# 상품 · 좋아요 · 주문 서비스 API

상품, 좋아요, 주문을 처리하는 이커머스 백엔드 서비스입니다.

## 기술 스택

- Java 25, Spring Boot 4.0.5, Spring Data JPA
- MySQL 8.0, H2 (테스트)
- QueryDSL 7.1
- Caffeine Cache
- Docker, Docker Compose

## 실행 방법

```bash
docker compose up -d
```

MySQL과 애플리케이션이 함께 실행됩니다. (포트: 8080)

초기 데이터(유저 3명, 상품 8개)가 자동으로 삽입됩니다.

### 테스트 계정

| loginId | loginPw | 이름 |
|---------|---------|------|
| user1 | password123 | 김철수 |
| user2 | password123 | 이영희 |
| user3 | password123 | 박민수 |

## API 명세

### 유저 (Users)

| Method | URI | 인증 | 설명 |
|--------|-----|------|------|
| POST | /api/v1/users | - | 회원가입 |
| GET | /api/v1/users/me | O | 내 정보 조회 |
| PUT | /api/v1/users/me/password | O | 비밀번호 변경 |

### 상품 (Products)

| Method | URI | 인증 | 설명 |
|--------|-----|------|------|
| POST | /api/v1/products | O | 상품 등록 |
| GET | /api/v1/products | - | 상품 목록 조회 |
| GET | /api/v1/products/{productId} | - | 상품 상세 조회 |

상품 목록 조회 파라미터: `sort` (latest/price_asc/likes_desc), `page`, `size`

### 좋아요 (Likes)

| Method | URI | 인증 | 설명 |
|--------|-----|------|------|
| POST | /api/v1/products/{productId}/likes | O | 좋아요 토글 |
| GET | /api/v1/products/liked | O | 좋아요한 상품 목록 |

### 주문 (Orders)

| Method | URI | 인증 | 설명 |
|--------|-----|------|------|
| POST | /api/v1/orders | O | 주문 생성 |
| GET | /api/v1/orders | O | 주문 목록 조회 |
| GET | /api/v1/orders/{orderId} | O | 주문 상세 조회 |

주문 생성 시 `Idempotency-Key` 헤더 필수

### Swagger UI

애플리케이션 실행 후 아래 주소에서 API 문서를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

### 인증 방식

인증이 필요한 API는 아래 헤더를 통해 사용자를 식별합니다.

```
X-Huuim-LoginId: user1
X-Huuim-LoginPw: password123
```

## 기술 고려사항

### 동시성 (Concurrency)

- **주문 재고 차감**: 비관적 락(PESSIMISTIC_WRITE)을 사용하여 동시 주문 시 재고 정합성 보장
- **좋아요 카운트**: QueryDSL 원자적 UPDATE(`SET like_count = like_count + 1`)로 동시 좋아요 정합성 보장
- **좋아요 중복 방지**: (user_id, product_id) 유니크 제약조건

### 멱등성 (Idempotency)

- 주문 생성 시 `Idempotency-Key` 헤더를 통해 멱등성 보장
- **(user_id + idempotency_key) 복합 유니크** 제약으로 사용자별 멱등성 키 관리
- 동일 사용자가 같은 멱등성 키로 재요청 시 기존 주문 결과를 반환 (중복 주문 방지)
- 서로 다른 사용자는 같은 멱등성 키를 사용해도 각각 별도 주문으로 처리

### 일관성 (Consistency)

- `@Transactional`을 통한 트랜잭션 관리
- 비관적 락으로 동시 접근 시 데이터 정합성 유지
- 좋아요 카운트는 DB 레벨 원자적 연산으로 일관성 보장

### 느린 조회 (Slow Query)

**인덱스 전략**

- `products`: created_at, price, like_count 정렬 조회용 인덱스
- `orders`: (user_id, created_at) 복합 인덱스 - 내 주문 목록 조회
- `likes`: (user_id, created_at) 복합 인덱스 - 좋아요 목록 조회
- FK 컬럼: MySQL InnoDB가 자동으로 인덱스 생성

**캐싱 전략**

- 구현체: Caffeine (단일 서버 환경에 적합한 고성능 로컬 캐시)
- 읽기 전략: Cache-Aside (캐시 미스 시 DB 조회 후 캐시 저장)
- 쓰기 전략: Write-Around (DB에 직접 쓰고 캐시 무효화)
- 대상: 상품 목록 조회, 상품 상세 조회 (공개 데이터, 높은 읽기 빈도, 캐시 공유 가능)
- TTL: 5분, 최대 1000건 (LRU)
- 상품 등록 시 목록 캐시 즉시 무효화 (`@CacheEvict`)
- 좋아요 등록/취소 시 해당 상품 상세 캐시 단건 무효화 (목록 캐시는 TTL 자연 만료)
- 실무에서는 정적 데이터(상품명, 가격)와 동적 카운터(좋아요 수)를 분리 캐싱하는 것이 이상적이나, 단일 서버 환경의 과제 특성상 단건 캐시 evict로 처리

**페이징 최적화**

- 주문 목록 조회 시 1:N fetchJoin + offset/limit의 메모리 페이징 문제를 방지하기 위해 **ID 먼저 조회 → In 절 fetchJoin** 2단계 쿼리 분리
- count 쿼리 분리로 불필요한 조인 제거

**N+1 방지**

- QueryDSL fetch join을 사용하여 주문 목록/상세, 좋아요 목록 조회 시 N+1 문제 해결

### 동시 주문

- 비관적 락(PESSIMISTIC_WRITE)으로 상품별 재고 차감 직렬화
- 재고 부족 시 예외 발생으로 초과 주문 방지
- 동시성 테스트를 통해 10건 동시 주문 시 재고 정확성 검증

## 테스트

```bash
./gradlew test
```

### 테스트 구성

| 분류 | 설명 |
|------|------|
| 단위 테스트 | UserService, ProductService, LikeService, OrderService, Product 엔티티 |
| 통합 테스트 | UserController, ProductController, LikeController, OrderController |
| 동시성 테스트 | 동시 주문 재고 차감, 재고 초과 동시 주문, 동시 좋아요 |

- 테스트 환경: H2 인메모리 DB (`@ActiveProfiles("test")`)
- 통합 테스트: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`
- 동시성 테스트: `ExecutorService` + `CountDownLatch`, `@AfterEach` 데이터 정리

## AI 활용

본 프로젝트는 **Claude Code (Anthropic Claude Opus 4.6)**와 **OpenAI Codex**를 활용하여 개발하였습니다.

### 1. 플랜 모드 (Plan Mode) — 요구사항 분석 및 설계

프로젝트 시작 시 Claude Code의 **플랜 모드**를 활용하여 채용 과제 PDF를 분석하고 구현 계획을 수립했습니다.

- 과제 요구사항(상품, 좋아요, 주문, 동시성, 멱등성 등)을 체계적으로 정리
- 도메인별 엔티티 설계, API 설계, 기술 스택 선정을 플랜으로 작성
- 플랜을 기반으로 구현 순서를 결정하고, 단계별로 진행하며 플랜을 업데이트

### 2. 웹 리서치 — 근거 기반 기술 의사결정

기술적 판단이 필요한 부분에서 AI의 웹 리서치 기능을 활용하여 업계 표준과 근거를 수집했습니다.

- **캐시 TTL 설정**: "이커머스에서 상품 데이터 캐싱 시 TTL을 몇으로 설정하는지" 웹 리서치를 요청하여 업계 사례(Amazon, eBay 등)를 조사한 뒤 5분으로 결정
- **캐시 읽기/쓰기 전략**: Cache-Aside + Write-Around 전략의 적합성을 이커머스 도메인 특성과 함께 검토
- **재고 캐싱 여부**: 실무에서 재고를 캐싱하는지 조사 → 표시용으로는 캐싱하되, 주문 시점에 비관적 락으로 실시간 검증하는 방식 채택
- **FK 인덱스 자동 생성**: MySQL InnoDB의 FK 인덱스 자동 생성 여부를 Docker로 실제 MySQL을 띄워 `SHOW INDEX`로 직접 검증

### 3. 대화형 설계 논의 — 캐싱, 동시성, 로깅 전략

AI에게 코드를 바로 작성시키지 않고, 먼저 **왜 그렇게 하는지** 질문하며 설계를 함께 논의했습니다.

- **캐싱 대상 선정**: 읽기 빈도, 변경 빈도, 정확도 요구, 공유 가능성 4가지 기준으로 각 API를 평가하여 상품 목록/상세만 캐싱 대상으로 선정
- **캐시 무효화 전략**: 좋아요가 빈번한 인기 상품에서 캐시가 계속 초기화되는 문제를 지적 → 좋아요 시 캐시 evict 제거, TTL 자연 만료로 변경
- **로깅 전략**: 도메인별로 유의미한 비즈니스 로그만 남기기로 결정. 좋아요는 빈도가 너무 높아 로깅 제외
- **동시성 처리**: 데드락 방지(productId 정렬 후 락 획득)까지 논의했으나, 현재 스코프에서는 과도하다고 판단하여 미적용

### 4. 스킬 검색 (find-skills) — 도구 확장 탐색

캐싱 적용 시 Claude Code의 **find-skills** 기능을 활용하여 캐싱 관련 전문 스킬이 있는지 탐색했습니다. 적합한 스킬이 없어 직접 구현했지만, AI 도구의 확장 가능성을 적극적으로 탐색하는 자세로 접근했습니다.

### 5. 도메인별 점진적 개발 — 커밋 단위 관리

AI와 함께 도메인 단위로 작업하고 각각 커밋하는 방식으로 진행했습니다.

- **User → Product → Like → Order** 순서로 도메인별 구현
- 로깅, Swagger 문서화 등 횡단 관심사도 도메인별로 분리 커밋
- 커밋 컨벤션 논의: 로깅은 사용자 기능이 아니므로 `feat`이 아닌 `chore`로 커밋

### 6. 교차 검증 — 여러 AI 도구 활용

하나의 AI에 의존하지 않고, **Claude Code와 OpenAI Codex를 함께 활용**하여 코드 품질을 높였습니다.

- Claude Code가 놓친 멱등성 키 스코프 문제(글로벌 → 사용자별)를 Codex가 발견하여 수정
- fetchJoin + offset/limit 메모리 페이징 문제를 Codex가 2단계 쿼리 분리로 해결
- 입력 오류 예외를 400으로 처리하는 GlobalExceptionHandler 보강 (Codex)

### 7. 테스트 주도 품질 관리

- **기능 추가 시 테스트가 깨지면 설계가 잘못된 것**이라는 원칙으로 접근
  - 예: UserCacheService를 UserService에 주입했더니 22개 테스트 실패 → 설계 자체를 롤백
- 동시성 테스트(`ExecutorService` + `CountDownLatch`)를 작성하여 비관적 락과 원자적 UPDATE의 정합성을 실제로 검증
- 통합 테스트에서 필드 레벨 테스트 데이터 대신 헬퍼 메서드 + 지역 변수 패턴을 적용하여 테스트 격리성 확보

### AI와의 협업에서 중요하게 생각한 점

- AI가 제안한 코드를 그대로 수용하지 않고, **왜 그렇게 하는지 근거를 확인한 뒤 적용**
- 기술적 판단이 필요한 부분에서 **직접 질문하고 논의하며 의사결정** (AI는 도구, 판단은 사람)
- 하나의 AI에 의존하지 않고 **여러 AI 도구를 활용하여 교차 검증**
- AI의 실수를 발견했을 때 **원인을 분석하고 피드백**하여 같은 실수가 반복되지 않도록 관리

## 프로젝트 구조

```
src/main/java/com/huuimcm/assignment
├── domain
│   ├── user          # 유저 (회원가입, 인증, 비밀번호 변경)
│   ├── product       # 상품 (등록, 목록/상세 조회)
│   ├── like          # 좋아요 (토글, 목록 조회)
│   └── order         # 주문 (생성, 목록/상세 조회)
└── global
    ├── config        # QueryDSL, PasswordEncoder, Cache, Swagger 설정
    ├── entity        # BaseEntity (JPA Auditing)
    ├── exception     # 글로벌 예외 처리
    └── response      # 공통 API 응답 구조
```
