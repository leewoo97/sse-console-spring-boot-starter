# SSE Console Spring Boot Starter

Spring Boot 프로젝트에서 SSE(Server-Sent Events) 및 일반 HTTP API를 쉽고 빠르게 테스트할 수 있도록 도와주는 Starter입니다.  
의존성 하나만 추가하면 `/sse-console.html` 웹 콘솔이 자동으로 활성화됩니다.

## 주요 특징
- `/sse-console.html` 웹 콘솔 자동 제공
- 프로젝트 내 모든 REST 엔드포인트(SSE + HTTP) 자동 탐색 및 목록화
- 대용량 응답(10만 건+)도 가상 스크롤로 UI 멈춤 없이 표시

---

## 빠른 시작

**Maven**
```xml
<dependency>
  <groupId>io.github.leewoo97</groupId>
  <artifactId>sse-console-spring-boot-starter</artifactId>
  <version>1.0.1</version>
</dependency>
```

**Gradle**
```gradle
dependencies {
  implementation "io.github.leewoo97:sse-console-spring-boot-starter:1.0.1"
}
```

애플리케이션 실행 후:
```
http://localhost:8080/sse-console.html
```

---

## 기술 상세 설명

이 프로젝트가 어떻게 만들어졌는지 기술적으로 설명합니다.

### 1. Spring Boot Starter 구조

Spring Boot Starter는 단순한 라이브러리 JAR가 아니라, **의존성을 추가하는 것만으로 기능이 자동 활성화**되는 구조입니다.

#### AutoConfiguration

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

위 파일에 `SseConsoleAutoConfiguration` 클래스명을 등록해두면, Spring Boot가 애플리케이션 시작 시 이 파일을 읽어 자동으로 설정 클래스를 로드합니다. (Spring Boot 3.x 방식)

```java
@AutoConfiguration
@ConditionalOnWebApplication(type = SERVLET)   // WebMVC 환경에서만 활성화
@ConditionalOnClass(RequestMappingHandlerMapping.class)
@Import(SseConsoleMetaController.class)        // 컨트롤러를 Bean으로 등록
public class SseConsoleAutoConfiguration {}
```

- `@ConditionalOnWebApplication` : Servlet 기반 WebMVC 환경이 아니면 아무것도 로드하지 않습니다.
- `@ConditionalOnClass` : `RequestMappingHandlerMapping`이 클래스패스에 없으면 비활성화됩니다 (spring-web 의존성 체크).
- `@Import` : `SseConsoleMetaController`를 빈으로 직접 등록합니다. 사용자 앱의 컴포넌트 스캔 범위 밖에 있어도 동작합니다.

---

### 2. 엔드포인트 자동 탐색 (`SseConsoleMetaController`)

`GET /sse-console/api-list` API가 핵심입니다. 이 API는 실행 중인 Spring 애플리케이션에 등록된 **모든 컨트롤러 메서드**를 런타임에 리플렉션으로 스캔합니다.

#### RequestMappingHandlerMapping

```java
RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
```

Spring MVC는 내부적으로 `RequestMappingHandlerMapping`이라는 객체에 **URL 패턴 → 핸들러 메서드** 매핑 정보를 모두 저장합니다. 이 객체를 ApplicationContext에서 꺼내면 현재 등록된 모든 엔드포인트 정보를 얻을 수 있습니다.

#### SSE 여부 판별

```java
boolean isSse = produces.stream().anyMatch(p -> p.contains("text/event-stream"));
```

각 매핑의 `ProducesCondition`을 확인해 `text/event-stream`을 produces하는 메서드를 SSE로 분류합니다. 그 외는 일반 HTTP로 분류합니다.

#### 파라미터 이름 추출 (Java Reflection)

```java
ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
String[] names = discoverer.getParameterNames(method);
```

Java 리플렉션의 `Parameter.getName()`은 컴파일 옵션에 따라 `arg0`, `arg1` 같은 의미 없는 이름을 반환할 수 있습니다.  
Spring의 `DefaultParameterNameDiscoverer`는 바이트코드 분석 + `@RequestParam(name=...)` 등의 어노테이션을 종합해 실제 파라미터 이름을 복원합니다.  
(`maven.compiler.parameters=true` 옵션이 있어야 바이트코드에 파라미터 이름이 보존됩니다.)

---

### 3. 정적 리소스 자동 서빙

`src/main/resources/static/sse-console.html`에 콘솔 HTML을 두면 Spring Boot의 정적 리소스 자동 서빙 기능에 의해 별도 설정 없이 `/sse-console.html`로 접근할 수 있습니다.

---

### 4. 프론트엔드 - 가상 스크롤 (Virtual Scroll)

10만 건 이상의 응답도 브라우저가 멈추지 않도록 **가상 스크롤**을 직접 구현했습니다.

#### 핵심 아이디어

```
전체 데이터(logBuffers) — 메모리에만 존재, 줄 수 무제한
      ↓
스크롤 위치 계산
      ↓
현재 보이는 범위(~30줄)만 DOM에 렌더링
```

#### 구현 방식

```
log-container (overflow-y: scroll, height: 260px)
  ├── log-spacer  ← height = 전체 줄 수 × LINE_HEIGHT (스크롤바 높이 확보)
  └── .log-line × N  ← position:absolute, top = i × LINE_HEIGHT
```

1. `log-spacer`의 height를 `줄 수 × 18px`로 설정해 스크롤바가 전체 범위를 표현하게 합니다.
2. 스크롤 이벤트가 발생할 때마다 `repaint()` 함수가 현재 `scrollTop`을 기준으로 보여야 할 줄 범위(`first ~ last`)를 계산합니다.
3. 해당 범위의 줄만 `position:absolute; top: i*18px`로 DOM에 추가합니다.
4. 나머지 줄은 DOM에 존재하지 않으므로 브라우저 렌더링 부담이 없습니다.

```javascript
function repaint(idx, autoScroll = false) {
  const totalH = lines.length * LINE_H;
  spacer.style.height = totalH + 'px';          // 스크롤 범위 확보

  const first = Math.floor(scrollTop / LINE_H) - OVERSCAN;
  const last  = Math.ceil((scrollTop + viewH) / LINE_H) + OVERSCAN;

  box.querySelectorAll('.log-line').forEach(el => el.remove());  // 이전 줄 제거
  for (let i = first; i < last; i++) {
    const el = document.createElement('div');
    el.style.top = (i * LINE_H) + 'px';          // 절대 위치로 배치
    el.textContent = lines[i];
    box.appendChild(el);
  }
}
```

#### SSE 스트리밍

SSE는 `EventSource` 대신 `fetch` + `ReadableStream`을 사용합니다.

```javascript
const reader = response.body.getReader();
while (true) {
  const { value, done } = await reader.read();
  // chunk 단위로 SSE 메시지 파싱 → appendLog()
}
```

`EventSource`는 자동 재연결 등 부가 기능이 있지만 세밀한 제어가 어렵습니다.  
`fetch + ReadableStream` 방식은 `AbortController`로 연결을 직접 제어할 수 있고, HTTP 에러 코드도 직접 처리할 수 있습니다.

---

### 5. 동작 조건 요약

| 조건 | 설명 |
|------|------|
| Spring Boot 3.x | AutoConfiguration.imports 방식 사용 (2.x는 spring.factories) |
| WebMVC (Servlet) | WebFlux(Reactor) 환경은 지원하지 않음 |
| Java 17+ | records, sealed class 등 미사용, 17 이상이면 동작 |
| `spring-boot-starter-web` | `RequestMappingHandlerMapping` 클래스가 필요 |

---

## 배포 및 라이선스
- Maven Central 배포
- 라이선스: MIT

## 기여 및 문의
- GitHub: https://github.com/leewoo97/sse-console-spring-boot-starter
- Issue 및 PR 환영
