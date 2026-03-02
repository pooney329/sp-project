# Spring MVC 쇼핑몰 프로젝트 요건 정리

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 1.8 |
| 프레임워크 | Spring MVC 5.1.3 |
| ORM | MyBatis 3.5.9 |
| DB | MySQL 8 (`test_db`) |
| 빌드 | Maven (WAR 패키징) |
| 뷰 | JSP + JSTL |
| 인증 | JWT (jjwt 0.11.5) + HttpOnly 쿠키 |
| 암호화 | BCrypt (spring-security-crypto 5.1.3) |
| 이메일 | Gmail SMTP (spring-context-support + javax.mail) |

---

## 프로젝트 구조

```
src/main/
├── java/com/test/
│   ├── config/
│   │   ├── exception/
│   │   │   ├── BusinessException.java      # 비즈니스 로직 오류 (400)
│   │   │   ├── NotFoundException.java      # 리소스 없음 (404)
│   │   │   └── UnauthorizedException.java  # 미인증 → 로그인 리다이렉트
│   │   ├── GlobalExceptionHandler.java     # @ControllerAdvice 전역 예외 처리
│   │   ├── JwtUtil.java                    # JWT 생성/파싱 유틸리티
│   │   └── JwtInterceptor.java             # JWT 쿠키 파싱 인터셉터
│   ├── controller/
│   │   ├── IndexController.java            # GET /
│   │   ├── AuthController.java             # POST /auth/send-code, /auth/verify-code
│   │   ├── MemberController.java           # /member/register, /login, /logout
│   │   ├── ProductController.java          # /product/list, /product/{id}
│   │   ├── OrderController.java            # /order/create, /order/list, /order/{id}
│   │   └── PointController.java            # /point/history
│   ├── service/
│   │   ├── EmailService.java               # OTP 이메일 발송
│   │   ├── MemberService.java              # 회원가입, 로그인, OTP 검증
│   │   ├── ProductService.java             # 상품 목록/상세
│   │   ├── OrderService.java               # 주문 생성 (트랜잭션)
│   │   └── PointService.java               # 포인트 내역 조회
│   ├── mapper/
│   │   ├── EmailAuthMapper.java
│   │   ├── MemberMapper.java
│   │   ├── ProductMapper.java
│   │   ├── OrderMapper.java
│   │   └── PointMapper.java
│   └── dto/
│       ├── MemberDto.java
│       ├── ProductDto.java
│       ├── OrderDto.java
│       └── PointHistoryDto.java
├── resources/
│   ├── application.properties              # DB, Mail, JWT 설정
│   └── com/test/mapper/                   # MyBatis XML Mapper
└── webapp/WEB-INF/
    ├── spring/
    │   ├── root-context.xml               # 부모 컨텍스트 (Service/Mapper/인프라)
    │   └── servlet-context.xml            # 자식 컨텍스트 (Controller/Interceptor)
    └── view/
        ├── index.jsp
        ├── error.jsp
        ├── member/register.jsp, login.jsp
        ├── product/list.jsp, detail.jsp
        ├── order/list.jsp, detail.jsp
        └── point/history.jsp
```

---

## Spring 컨텍스트 구조

```
root-context.xml (부모 — ContextLoaderListener)
  ├── @Service, @Repository, @Component 스캔
  ├── DataSource (MySQL)
  ├── SqlSessionFactory + MapperScannerConfigurer (MyBatis)
  ├── DataSourceTransactionManager
  ├── JavaMailSender (Gmail SMTP)
  └── JwtUtil (@Component)

servlet-context.xml (자식 — DispatcherServlet)
  ├── @Controller, @ControllerAdvice 스캔
  ├── InternalResourceViewResolver (/WEB-INF/view/*.jsp)
  └── JwtInterceptor (XML 빈, ref="jwtUtil" → 부모 빈 참조)
```

> 자식 컨텍스트는 부모 컨텍스트의 빈을 참조할 수 있다 (역방향 불가).

---

## DB 스키마

```sql
member          -- 회원 (id, email, password, name, point_balance, created_at)
email_auth      -- 이메일 OTP (id, email, auth_code, expired_at, verified)
product         -- 상품 (id, name, price, stock, description, created_at)
orders          -- 주문 헤더 (id, member_id, total_amount, point_used, payment_amount, status, ordered_at)
order_item      -- 주문 상품 (id, order_id, product_id, quantity, price)
point_history   -- 포인트 내역 (id, member_id, order_id, amount, type, created_at)
```

---

## 구현 기능

### Phase 1 — 전역 예외 처리

| 예외 클래스 | 처리 결과 |
|-------------|-----------|
| `NotFoundException` | error.jsp (statusCode: 404) |
| `UnauthorizedException` | redirect:/member/login |
| `BusinessException` | error.jsp (statusCode: 400, errorCode 포함) |
| `Exception` | error.jsp (statusCode: 500) |

---

### Phase 2 — 회원 인증

#### 회원가입 (이메일 OTP 인증 필수)

```
1. POST /auth/send-code (email)
   → 6자리 OTP 생성 → email_auth INSERT → Gmail SMTP 발송
   → 유효 시간: 5분

2. POST /auth/verify-code (email, code)
   → email_auth에서 미만료 + 미검증 코드 조회 → verified = 1 UPDATE

3. POST /member/register (email, password, name)
   → email_auth.verified 확인 → 중복 이메일 확인 → BCrypt 암호화 → member INSERT
```

#### 로그인 / 로그아웃 (JWT 쿠키)

```
로그인: POST /member/login
  → memberService.login() (BCrypt 검증)
  → jwtUtil.generate(id, email, name) → HS256, 24h 만료
  → HttpOnly 쿠키 "jwt" 설정 (Path=/, MaxAge=86400)

로그아웃: GET /member/logout
  → 쿠키 "jwt" MaxAge=0 으로 삭제
```

---

### Phase 3 — 상품 & 주문

#### 상품

| URL | 설명 |
|-----|------|
| `GET /product/list` | 전체 상품 목록 |
| `GET /product/{id}` | 상품 상세 + 로그인 시 포인트 잔액 포함 주문 폼 |

#### 주문 생성 (`@Transactional`)

```
POST /order/create (productId, quantity, pointUsed)
  1. 재고 확인 (product.stock >= quantity)
  2. 포인트 사용량 검증 (pointUsed >= 0, pointUsed <= totalAmount)
  3. orders INSERT
  4. order_item INSERT + product.stock 차감
  5. 포인트 사용 기록 (type: USE, amount: -pointUsed)
  6. 포인트 적립 (결제금액 × 1%, 소수점 버림, type: EARN)
  7. member.point_balance += (적립 - 사용)  ← delta 방식 SQL UPDATE
```

> `ProductMapper.decreaseStock` : SQL WHERE 절에 `stock >= quantity` 조건으로 동시성 보호

---

### Phase 4 — 포인트 내역

```
GET /point/history
  → memberService.findById() 로 최신 point_balance 조회
  → pointMapper.findByMemberId() 로 전체 내역 조회
  → 잔액 + 내역 목록 모델에 전달
```

포인트 타입:
- `EARN` — 결제금액 1% 자동 적립 (amount: 양수)
- `USE` — 주문 시 사용 (amount: 음수)

---

### Phase 5 — JWT 인증 전환

#### JwtUtil

```java
@Component  // root-context 스캔
public class JwtUtil {
    // @Value("${jwt.secret}") 로 비밀키 주입
    // generate(id, email, name) → JWT 문자열 (HS256, 24h)
    // parse(token) → Claims (유효하지 않으면 예외)
}
```

#### JwtInterceptor

```java
// HandlerInterceptor 구현, XML로 빈 등록 (servlet-context.xml)
// preHandle():
//   jwt 쿠키 추출 → jwtUtil.parse() → MemberDto(id, email, name) 생성
//   → request.setAttribute("loginMember", member)
//   파싱 실패 시 쿠키 삭제 후 통과 (컨트롤러가 인증 판단)
```

#### 컨트롤러 인증 패턴

```java
// 보호된 엔드포인트
MemberDto member = (MemberDto) request.getAttribute("loginMember");
if (member == null) throw new UnauthorizedException();
```

#### 데이터 흐름

```
요청
  → JwtInterceptor.preHandle()
      └─ jwt 쿠키 파싱 → request.setAttribute("loginMember")
  → Controller
      └─ request.getAttribute("loginMember") 로 인증 확인
      └─ null이면 UnauthorizedException
  → GlobalExceptionHandler
      └─ UnauthorizedException → redirect:/member/login
```

---

## 설정 파일

### application.properties

```properties
# DataSource
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC&...
db.username=root
db.password=1234

# Mail (Gmail SMTP) — 실제 앱 비밀번호로 교체 필요
mail.host=smtp.gmail.com
mail.port=587
mail.username=your@gmail.com
mail.password=앱비밀번호

# JWT
jwt.secret=MyJwtSecretKey2024ForSpringProject123456  # 최소 32자
```

### pom.xml 주요 의존성

```xml
spring-webmvc, spring-jdbc, spring-tx       <!-- Spring MVC -->
mybatis, mybatis-spring                      <!-- MyBatis -->
mysql-connector-java                         <!-- MySQL -->
spring-context-support, javax.mail           <!-- 이메일 -->
spring-security-crypto                       <!-- BCrypt -->
jstl                                         <!-- JSP JSTL -->
jjwt-api, jjwt-impl (runtime), jjwt-jackson (runtime)  <!-- JWT -->
```

---

## URL 목록

| Method | URL | 인증 필요 | 설명 |
|--------|-----|-----------|------|
| GET | `/` | X | 홈 |
| GET | `/member/register` | X | 회원가입 폼 |
| POST | `/member/register` | X | 회원가입 처리 |
| POST | `/auth/send-code` | X | OTP 이메일 발송 |
| POST | `/auth/verify-code` | X | OTP 검증 |
| GET | `/member/login` | X | 로그인 폼 |
| POST | `/member/login` | X | 로그인 처리 (JWT 쿠키 발급) |
| GET | `/member/logout` | X | 로그아웃 (JWT 쿠키 삭제) |
| GET | `/product/list` | X | 상품 목록 |
| GET | `/product/{id}` | X | 상품 상세 (로그인 시 주문 폼 포함) |
| POST | `/order/create` | **O** | 주문 생성 |
| GET | `/order/list` | **O** | 내 주문 목록 |
| GET | `/order/{id}` | **O** | 주문 상세 |
| GET | `/point/history` | **O** | 포인트 내역 |

---

## 주요 설계 결정

### 포인트 잔액 관리 — Delta 방식
`member.point_balance`를 직접 SELECT 후 UPDATE하는 대신, SQL에서 `point_balance = point_balance + delta` 방식으로 상대적 업데이트. 동시 요청 시 덮어쓰기 방지.

### 재고 차감 — SQL WHERE 조건 보호
```sql
UPDATE product SET stock = stock - #{quantity}
WHERE id = #{id} AND stock >= #{quantity}
```
애플리케이션 레벨에서 재고를 확인했더라도, DB 레벨에서 한 번 더 보호.

### JWT 인터셉터 — 통과 방식
인터셉터는 토큰 파싱 실패 시 예외를 던지지 않고 통과시킨다. 인증 필요 여부 판단은 각 컨트롤러에서 `UnauthorizedException`으로 처리. 공개 페이지(상품 목록 등)는 인증 없이도 정상 작동.

### 컨텍스트 분리 이유
- **root-context**: Service/Mapper는 웹 계층과 무관 → 테스트 및 재사용 용이
- **servlet-context**: Controller/Interceptor는 DispatcherServlet에 종속
- 자식이 부모 빈을 참조할 수 있어 `JwtUtil`을 root에 두고 servlet에서 `ref` 가능
