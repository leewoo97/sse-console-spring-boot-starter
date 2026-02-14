# SSE Console Spring Boot Starter

Spring Boot 프로젝트에서 SseEmitter 기반 SSE API를 쉽고 빠르게 테스트할 수 있도록 도와주는 Starter입니다. 내장된 웹 콘솔(tool page)에서 다양한 SSE 엔드포인트를 실시간으로 호출하고 결과를 확인할 수 있습니다.

## 주요 특징
- `/sse-console.html` 웹 콘솔 제공 (자동 포함)
- 프로젝트 내 SSE 엔드포인트 자동 탐색 및 목록화
- 다양한 테스트용 샘플 컨트롤러 제공(예제)

## 빠른 시작
1. 의존성 추가 (Maven)
```xml
<dependency>
  <groupId>io.github.leewoo97</groupId>
  <artifactId>sse-console-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

2. 애플리케이션 실행 후 브라우저에서 접속:
```
http://localhost:8080/sse-console.html
```

## Tool Page (sse-console.html)
- 프로젝트 내 REST/SSE 엔드포인트를 자동으로 탐색하여 목록화
- 파라미터 입력 및 실시간 SSE 메시지 수신 테스트 가능

## 샘플 엔드포인트 예시
- `/sse/test` : 기본 SSE 메시지 스트림
- `/sse/counter` : 카운트 업 이벤트
- `/sse/random` : 랜덤 숫자 스트림
- `/sse/json` : JSON 오브젝트 스트림 등

## 배포 및 라이선스
- Maven Central에 배포 예정
- 라이선스: MIT

## 기여 및 문의
- GitHub: https://github.com/leewoo97/sse-console-spring-boot-starter
- Issue 및 PR 환영
