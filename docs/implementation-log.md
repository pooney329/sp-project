# 쇼핑몰 서비스 구현 로그

> 프로젝트: sp-project (Spring 5.1.3 / MyBatis / MySQL / JDK 8)
> 기준 상태: Notice 조회 + GlobalExceptionHandler 뼈대

---

## 최종 패키지 구조

```
src/main/java/com/test/
├── config/
│   ├── GlobalExceptionHandler.java
│   └── exception/
│       ├── BusinessException.java
│       ├── NotFoundException.java
│       └── UnauthorizedException.java
├── controller/
│   ├── IndexController.java
│   ├── AuthController.java
│   ├── MemberController.java
│   ├── ProductController.java
│   ├── OrderController.java
│   └── PointController.java
├── service/
│   ├── EmailService.java
│   ├── MemberService.java
│   ├── ProductService.java
│   ├── OrderService.java
│   └── PointService.java
├── mapper/
│   ├── NoticeMapper.java
│   ├── MemberMapper.java
│   ├── EmailAuthMapper.java
│   ├── ProductMapper.java
│   ├── OrderMapper.java
│   └── PointMapper.java
└── dto/
    ├── MemberDto.java
    ├── ProductDto.java
    ├── OrderDto.java
    └── PointHistoryDto.java

src/main/resources/com/test/mapper/
├── NoticeMapper.xml
├── MemberMapper.xml
├── EmailAuthMapper.xml
├── ProductMapper.xml
├── OrderMapper.xml
└── PointMapper.xml

src/main/webapp/WEB-INF/
├── web.xml
├── spring/
│   ├── root-context.xml
│   └── servlet-context.xml
└── view/
    ├── error.jsp
    ├── index.jsp
    ├── list.jsp
    ├── member/
    │   ├── register.jsp
    │   └── login.jsp
    ├── product/
    │   ├── list.jsp
    │   └── detail.jsp
    ├── order/
    │   ├── list.jsp
    │   └── detail.jsp
    └── point/
        └── history.jsp

docs/
├── schema.sql
├── setup-log.md
└── implementation-log.md  ← 이 파일
```

---

## Phase 1 — ExceptionHandler 완성

### 추가된 파일

| 파일 | 설명 |
|------|------|
| `config/exception/BusinessException.java` | 커스텀 예외 베이스 클래스 |
| `config/exception/NotFoundException.java` | 리소스 없음 (404) |
| `config/exception/UnauthorizedException.java` | 인증되지 않은 접근 (401) |
| `config/GlobalExceptionHandler.java` | 예외 타입별 분기 처리 |
| `view/error.jsp` | 에러 화면 |

### 예외 계층 구조

```
RuntimeException
└── BusinessException          (code + message 필드)
    ├── NotFoundException       code = "NOT_FOUND"
    └── UnauthorizedException   code = "UNAUTHORIZED"
```

### GlobalExceptionHandler 분기

```java
@ExceptionHandler(NotFoundException.class)
// → ModelAndView("error"), statusCode=404

@ExceptionHandler(UnauthorizedException.class)
// → redirect:/member/login

@ExceptionHandler(BusinessException.class)
// → ModelAndView("error"), statusCode=400

@ExceptionHandler(Exception.class)
// → ModelAndView("error"), statusCode=500, 메시지="서버 내부 오류가 발생했습니다."
```

### error.jsp 전달 모델

| 속성 | 내용 |
|------|------|
| `statusCode` | HTTP 상태 코드 (400 / 404 / 500) |
| `errorCode` | 예외 식별 코드 (NOT_FOUND, BUSINESS_ERROR 등) |
| `errorMessage` | 사용자에게 표시할 메시지 |

---

## Phase 2 — 회원가입 + 이메일 OTP 인증 + 로그인

### pom.xml 추가 의존성

```xml
<!-- 이메일 발송 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>5.1.3.RELEASE</version>
</dependency>
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>

<!-- BCrypt 암호화 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>5.1.3.RELEASE</version>
</dependency>

<!-- JSTL -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>jstl</artifactId>
    <version>1.2</version>
</dependency>
```

### DB 테이블

```sql
CREATE TABLE member (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,       -- BCrypt 암호화
    name          VARCHAR(50)  NOT NULL,
    point_balance INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT NOW()
);

CREATE TABLE email_auth (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(100) NOT NULL,
    auth_code  VARCHAR(6)   NOT NULL,
    expired_at DATETIME     NOT NULL,          -- 유효시간 5분
    verified   TINYINT(1)   NOT NULL DEFAULT 0
);
```

### 요청 흐름

```
POST /auth/send-code   (email)
  → 6자리 OTP 생성 (Random) → email_auth INSERT → 이메일 발송
  → redirect:/member/register (FlashAttribute: message, email)

POST /auth/verify-code (email, code)
  → email_auth UPDATE verified=1 (expired_at > NOW() AND verified=0 조건)
  → 업데이트 행=0 이면 BusinessException("INVALID_CODE")
  → redirect:/member/register (FlashAttribute: verified=true, email)

POST /member/register  (email, password, name)
  → email_auth.countVerified(email) = 0 이면 예외
  → member.findByEmail 존재하면 DUPLICATE_EMAIL 예외
  → BCrypt 암호화 후 member INSERT
  → redirect:/member/login

POST /member/login     (email, password)
  → findByEmail → passwordEncoder.matches 검증
  → 실패 시 UnauthorizedException (→ redirect:/member/login)
  → 성공 시 session.setAttribute("loginMember", memberDto)
  → redirect:/

GET  /member/logout
  → session.invalidate() → redirect:/
```

### JavaMailSender 빈 설정 (root-context.xml)

```xml
<bean id="mailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
    <property name="host"     value="smtp.gmail.com"/>
    <property name="port"     value="587"/>
    <property name="username" value="your-email@gmail.com"/>   <!-- 교체 필요 -->
    <property name="password" value="your-app-password"/>      <!-- 교체 필요 -->
    <property name="javaMailProperties">
        <props>
            <prop key="mail.smtp.auth">true</prop>
            <prop key="mail.smtp.starttls.enable">true</prop>
        </props>
    </property>
</bean>
```

> Gmail 사용 시 계정 설정 → 앱 비밀번호(16자리) 발급 후 대입.

### 추가된 파일

| 파일 | 역할 |
|------|------|
| `dto/MemberDto.java` | id, email, password, name, pointBalance, createdAt |
| `mapper/MemberMapper.java` | findByEmail, insert, updatePointBalance, addPointBalance |
| `mapper/EmailAuthMapper.java` | insert, verify, countVerified |
| `resources/.../MemberMapper.xml` | SELECT/INSERT/UPDATE SQL |
| `resources/.../EmailAuthMapper.xml` | INSERT / UPDATE verified=1 / COUNT |
| `service/EmailService.java` | JavaMailSender → MimeMessage 발송 |
| `service/MemberService.java` | sendOtp / verifyOtp / register / login |
| `controller/AuthController.java` | POST /auth/send-code, /auth/verify-code |
| `controller/MemberController.java` | GET/POST /member/register, /login, /logout |
| `view/member/register.jsp` | 3단계 폼 (발송→검증→가입) |
| `view/member/login.jsp` | 로그인 폼 |

---

## Phase 3 — 상품 목록/상세 + 주문 시스템

### DB 테이블

```sql
CREATE TABLE product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    price       INT          NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    description TEXT,
    created_at  DATETIME     NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT      NOT NULL,
    total_amount   INT         NOT NULL,
    point_used     INT         NOT NULL DEFAULT 0,
    payment_amount INT         NOT NULL,          -- total_amount - point_used
    status         VARCHAR(20) NOT NULL DEFAULT 'ORDERED',
    ordered_at     DATETIME    NOT NULL DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE order_item (
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL,
    price      INT    NOT NULL,                   -- 주문 시점 가격
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);
```

### 요청 흐름

```
GET  /product/list      → 전체 상품 목록 (created_at DESC)
GET  /product/{id}      → 상품 상세 + 주문 폼 (로그인 시 포인트 사용 입력 포함)
POST /order/create      → 주문 생성 (로그인 체크, @Transactional)
GET  /order/list        → 내 주문 내역 (loginMember 기준)
GET  /order/{id}        → 주문 상세 (order_item JOIN product)
```

### OrderService.createOrder() 트랜잭션 처리

```
1. pointUsed 음수 검증
2. 상품별 재고 확인 (부족 시 BusinessException "OUT_OF_STOCK")
3. totalAmount 계산 (price × quantity 합산)
4. paymentAmount = totalAmount - pointUsed (음수이면 "INVALID_POINT" 예외)
5. orders INSERT (useGeneratedKeys → orderId 획득)
6. 상품별: order_item INSERT + product.stock 차감
   - decreaseStock SQL: WHERE stock >= quantity (동시성 보호)
7. pointUsed > 0 이면: point_history INSERT (type=USE, amount=-N)
8. earnPoint = paymentAmount * 1% (소수점 버림)
   earnPoint > 0 이면: point_history INSERT (type=EARN, amount=+M)
9. member.point_balance += (earnPoint - pointUsed)
   → addPointBalance SQL: SET point_balance = point_balance + delta
   → 하나라도 실패 시 전체 롤백
```

### root-context.xml 추가 (트랜잭션)

```xml
<bean id="transactionManager"
      class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource"/>
</bean>
<tx:annotation-driven transaction-manager="transactionManager"/>
```

네임스페이스:
```xml
xmlns:tx="http://www.springframework.org/schema/tx"
xsi:schemaLocation="...
    http://www.springframework.org/schema/tx
    http://www.springframework.org/schema/tx/spring-tx.xsd"
```

### 추가된 파일

| 파일 | 역할 |
|------|------|
| `dto/ProductDto.java` | id, name, price, stock, description, createdAt |
| `dto/OrderDto.java` | orders 필드 + 내부 클래스 OrderItemDto / OrderItemResultDto |
| `mapper/ProductMapper.java` | findAll, findById, decreaseStock |
| `mapper/OrderMapper.java` | insertOrder, insertOrderItem, findByMemberId, findById, findItemsByOrderId |
| `resources/.../ProductMapper.xml` | SELECT + UPDATE stock |
| `resources/.../OrderMapper.xml` | INSERT orders/order_item + SELECT with JOIN |
| `service/ProductService.java` | getProductList / getProduct (없으면 NotFoundException) |
| `service/OrderService.java` | createOrder(@Transactional) / getOrderList / getOrder |
| `controller/ProductController.java` | GET /product/list, /product/{id} |
| `controller/OrderController.java` | POST /order/create, GET /order/list, /order/{id} |
| `view/product/list.jsp` | 상품 목록 테이블 |
| `view/product/detail.jsp` | 상품 상세 + 주문 폼 (수량, 포인트 입력) |
| `view/order/list.jsp` | 주문 내역 테이블 |
| `view/order/detail.jsp` | 주문 상세 + 주문 상품 목록 |

---

## Phase 4 — 포인트 적립/사용

### DB 테이블

```sql
CREATE TABLE point_history (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    order_id   BIGINT,                           -- NULL 허용
    amount     INT         NOT NULL,             -- 양수: 적립, 음수: 사용
    type       VARCHAR(10) NOT NULL,             -- 'EARN' | 'USE'
    created_at DATETIME    NOT NULL DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (order_id)  REFERENCES orders(id)
);
```

### 적립/사용 규칙

| 구분 | 기준 | 예시 |
|------|------|------|
| 적립 | `payment_amount × 1%` (소수점 버림) | 15,000원 결제 → 150P |
| 사용 | 주문 시 `pointUsed` 파라미터 입력 | 500P 사용 → 결제금액 500원 차감 |
| 잔액 관리 | `member.point_balance` (addPointBalance delta 방식) | 음수가 되지 않도록 SQL 조건 추가 권장 |

### 요청 흐름

```
GET /point/history
  → 세션 loginMember 체크 (없으면 UnauthorizedException)
  → point_history WHERE member_id ORDER BY created_at DESC
  → view: point/history.jsp (보유 포인트 + 내역 테이블)

(적립/사용은 OrderService.createOrder() 내부에서 자동 처리)
```

### MemberMapper 확장 (Phase 4에서 추가)

```java
// 기존
void updatePointBalance(Long memberId, int pointBalance);

// 신규 — delta 방식 (동시성 안전)
void addPointBalance(Long memberId, int delta);
```

```xml
<update id="addPointBalance">
    UPDATE member
    SET point_balance = point_balance + #{param2}
    WHERE id = #{param1}
</update>
```

### 추가된 파일

| 파일 | 역할 |
|------|------|
| `dto/PointHistoryDto.java` | id, memberId, orderId, amount, type, createdAt |
| `mapper/PointMapper.java` | insertHistory, findByMemberId |
| `resources/.../PointMapper.xml` | INSERT + SELECT |
| `service/PointService.java` | getHistory(memberId) |
| `controller/PointController.java` | GET /point/history |
| `view/point/history.jsp` | 보유 포인트 + 적립/사용 내역 테이블 (색상 구분) |

---

## URL 전체 목록

| Method | URL | 설명 | 로그인 필요 |
|--------|-----|------|:-----------:|
| GET    | `/`                  | 홈 (index.jsp)      |             |
| GET    | `/notice`            | 공지사항 목록        |             |
| POST   | `/auth/send-code`    | OTP 발송             |             |
| POST   | `/auth/verify-code`  | OTP 검증             |             |
| GET    | `/member/register`   | 회원가입 폼          |             |
| POST   | `/member/register`   | 회원가입 처리        |             |
| GET    | `/member/login`      | 로그인 폼            |             |
| POST   | `/member/login`      | 로그인 처리          |             |
| GET    | `/member/logout`     | 로그아웃             | ✓           |
| GET    | `/product/list`      | 상품 목록            |             |
| GET    | `/product/{id}`      | 상품 상세            |             |
| POST   | `/order/create`      | 주문 생성            | ✓           |
| GET    | `/order/list`        | 내 주문 내역         | ✓           |
| GET    | `/order/{id}`        | 주문 상세            | ✓           |
| GET    | `/point/history`     | 포인트 내역          | ✓           |

---

## 시작 전 체크리스트

- [ ] `docs/schema.sql` 을 MySQL에서 실행 (DB: test_db)
- [ ] `root-context.xml` 의 DataSource username/password 확인
- [ ] `root-context.xml` 의 JavaMailSender username/password → Gmail 앱 비밀번호로 교체
- [ ] `mvn clean package` 빌드 후 Tomcat에 WAR 배포
