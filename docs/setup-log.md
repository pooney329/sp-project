# Spring MVC + MyBatis + MySQL 셋업 정리

> 프로젝트: sp-project (Spring 5.1.3 / JDK 8 / Maven WAR / Tomcat)

---

## 최종 프로젝트 구조

```
sp-project/
├── pom.xml
└── src/main/
    ├── java/com/test/
    │   ├── config/
    │   │   └── GlobalExceptionHandler.java   # 전역 예외 처리
    │   ├── controller/
    │   │   ├── IndexController.java           # / , /index
    │   │   └── NoticeController.java          # /notice
    │   ├── mapper/
    │   │   └── NoticeMapper.java              # MyBatis Mapper 인터페이스
    │   └── service/
    │       └── NoticeService.java
    ├── resources/
    │   └── com/test/mapper/
    │       └── NoticeMapper.xml               # SQL 매핑
    └── webapp/WEB-INF/
        ├── web.xml
        ├── spring/
        │   ├── root-context.xml               # 부모 컨텍스트 (DataSource, MyBatis, Service)
        │   └── servlet-context.xml            # 자식 컨텍스트 (MVC, Controller)
        └── view/
            ├── index.jsp
            └── list.jsp
```

---

## 1. MyBatis 의존성 추가 (`pom.xml`)

```xml
<!-- MyBatis Core -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.9</version>
</dependency>

<!-- MyBatis-Spring 연동 -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>2.0.7</version>
</dependency>
```

> 버전 근거: mybatis 3.5.9 / mybatis-spring 2.0.7 → JDK 8 + Spring 5.1.x 공식 호환

소스 인코딩도 함께 명시:

```xml
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

---

## 2. Spring 설정 파일 구조 정리

### 정리 전 → 후

| 파일 | 변경 |
|------|------|
| `dispatcher-servlet.xml` | **삭제** — web.xml에서 참조 안 하는 죽은 레거시 파일 |
| `security-context.xml` | **삭제** — 내용 없는 빈 파일 |
| `service-context.xml` | **삭제** → `root-context.xml` 으로 교체 |
| `root-context.xml` | **신규** — 부모 컨텍스트 |
| `servlet-context.xml` | **정리** — 중복 스캔 제거, 레거시 XML bean 제거 |

### 컨텍스트 계층 구조

```
ContextLoaderListener  →  root-context.xml    (부모: DataSource, MyBatis, @Service)
DispatcherServlet      →  servlet-context.xml  (자식: MVC 설정, @Controller)
```

자식 컨텍스트는 부모 컨텍스트의 빈을 참조할 수 있다. (반대는 불가)

### `root-context.xml`

```xml
<!-- @Service, @Repository 스캔 (Controller 계층 제외) -->
<context:component-scan base-package="com.test">
    <context:exclude-filter type="annotation"
        expression="org.springframework.stereotype.Controller"/>
    <context:exclude-filter type="annotation"
        expression="org.springframework.web.bind.annotation.ControllerAdvice"/>
</context:component-scan>

<!-- DataSource -->
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/test_db?useSSL=false
                                &amp;serverTimezone=UTC
                                &amp;characterEncoding=UTF-8
                                &amp;useUnicode=true"/>
    <property name="username" value="root"/>
    <property name="password" value="****"/>
</bean>

<!-- MyBatis SqlSessionFactory -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="typeAliasesPackage" value="com.test.dto"/>
    <property name="mapperLocations" value="classpath*:com/test/mapper/**/*.xml"/>
</bean>

<!-- Mapper 인터페이스 자동 스캔 -->
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="com.test.mapper"/>
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
</bean>
```

### `servlet-context.xml`

```xml
<!-- @Controller, @ControllerAdvice 만 스캔 -->
<context:component-scan base-package="com.test" use-default-filters="false">
    <context:include-filter type="annotation"
        expression="org.springframework.stereotype.Controller"/>
    <context:include-filter type="annotation"
        expression="org.springframework.web.bind.annotation.ControllerAdvice"/>
</context:component-scan>

<mvc:annotation-driven/>
<mvc:default-servlet-handler/>
<mvc:resources mapping="/static/**" location="/static/"/>

<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="prefix" value="/WEB-INF/view/"/>
    <property name="suffix" value=".jsp"/>
</bean>
```

> `use-default-filters="false"` + `include-filter` 조합:
> 기본 스캔(@Component 등 전체)을 끄고, 명시한 어노테이션만 정확히 등록.

---

## 3. MyBatis Mapper 구현

### `NoticeMapper.java`

```java
package com.test.mapper;

import java.util.List;

public interface NoticeMapper {
    List<String> getNoticeList();
}
```

### `NoticeMapper.xml` (`src/main/resources/com/test/mapper/`)

```xml
<mapper namespace="com.test.mapper.NoticeMapper">
    <select id="getNoticeList" resultType="string">
        SELECT title FROM notice
    </select>
</mapper>
```

> `mapperLocations` 경로(`classpath*:com/test/mapper/**/*.xml`)와 패키지 구조를 일치시켜야 함.

---

## 4. NoticeController 리팩토링

레거시 `Controller` 인터페이스 → 어노테이션 방식으로 전환.

### 변경 전 (레거시)

```java
public class NoticeController implements Controller {
    private NoticeService noticeService;

    @Override
    public ModelAndView handleRequest(...) { ... }

    public void setNoticeService(NoticeService noticeService) { ... }
}
```

- XML에서 `<bean id="/notice">` 로 URL 매핑
- setter 주입

### 변경 후 (어노테이션)

```java
@Controller
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @RequestMapping("/notice")
    public ModelAndView noticeList() { ... }
}
```

---

## 5. 한글 인코딩 설정

한글 깨짐이 발생하는 계층과 해결책:

| 계층 | 원인 | 해결 |
|------|------|------|
| DB → Java | JDBC 연결 charset 미지정 | URL에 `characterEncoding=UTF-8&useUnicode=true` 추가 |
| 브라우저 → 서버 | HTTP 요청/응답 인코딩 미설정 | `web.xml`에 `CharacterEncodingFilter` 추가 |
| Maven 빌드 | 소스파일 인코딩 미지정 | `pom.xml`에 `project.build.sourceEncoding=UTF-8` 추가 |
| 콘솔 출력 | Windows JVM 기본 인코딩 MS949 | IntelliJ VM 옵션에 `-Dfile.encoding=UTF-8` 추가 |

### `web.xml` CharacterEncodingFilter

```xml
<!-- 반드시 다른 filter/listener 보다 먼저 선언 -->
<filter>
    <filter-name>encodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
    <init-param>
        <param-name>forceEncoding</param-name>
        <param-value>true</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>encodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

### IntelliJ VM 옵션

```
Run → Edit Configurations → Tomcat Server → VM options:
-Dfile.encoding=UTF-8
```

---

## 6. Root Path 404 해결

| 문제 | 원인 | 해결 |
|------|------|------|
| `/` 접근 시 404 | 매핑된 컨트롤러 없음 | `IndexController`에 `@RequestMapping("/")` 추가 |
| 미매핑 경로 모두 404 | DispatcherServlet이 처리 못한 요청을 Tomcat에 위임 안 함 | `servlet-context.xml`에 `<mvc:default-servlet-handler/>` 추가 |

---

## 7. 요청 흐름 요약

```
브라우저 GET /notice
  → DispatcherServlet (servlet-context.xml)
    → NoticeController.noticeList()
      → NoticeService.getNoticeList()           [root-context.xml 빈]
        → NoticeMapper.getNoticeList()           [MyBatis Mapper]
          → MySQL: SELECT title FROM notice
        ← List<String>
      ← List<String>
    ← ModelAndView("list")
  → InternalResourceViewResolver
    → /WEB-INF/view/list.jsp
```

---

## 8. DB 초기 세팅 SQL

```sql
CREATE DATABASE IF NOT EXISTS test_db CHARACTER SET utf8mb4;
USE test_db;

CREATE TABLE IF NOT EXISTS notice (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL
);

INSERT INTO notice (title) VALUES ('공지사항 1'), ('공지사항 2'), ('테스트');
```
