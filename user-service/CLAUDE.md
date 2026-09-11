# user-service 코딩 지침

루트 CLAUDE.md와 함께 적용한다.

## 역할과 현재 기술

- 브라우저 요청, Thymeleaf SSR 화면, 사용자 입력, 로그인·세션을 담당한다.
- 업무 데이터 처리는 core-service REST API에 위임한다. DB 드라이버, JPA, MyBatis, Flyway 및 DB 자격 증명을 추가하지 않는다.
- 현재 기술: Spring Boot Web MVC, Thymeleaf, Spring Security, Actuator, RestClient.
- 기본 패키지: com.example.project.user. 기본 포트는 8080이다.
- 현재 HomeController와 CoreClient는 /api/v1/setup 연결 확인용이다. 실제 업무 화면은 아직 없다.

## 코드 배치와 화면 규칙

- presentation/controller: 라우팅, 입력 검증, 응답 화면 선택.
- presentation/viewmodel: 화면 전용 데이터. Entity를 화면 모델로 사용하지 않는다.
- application: 화면 유스케이스 조정. 핵심 업무 규칙은 core에 둔다.
- client/core: core 호출 및 외부 API DTO 변환.
- security, config: 인증·인가와 설정.
- templates: Thymeleaf, static/css와 static/js: 화면 정적 리소스.
- 위 경로는 새 코드 배치 기준이다. 사용하는 기능이 생길 때 디렉터리를 만든다.
- 사용자 값은 Thymeleaf의 기본 이스케이프를 사용한다. 검증되지 않은 값을 th:utext나 innerHTML로 출력하지 않는다.
- jQuery/DataTables는 예정 기술이다. 도입할 때 고정 버전과 라이선스를 확인해 static/vendor에 둔다. CDN latest나 별도 SPA 프레임워크로 임의 전환하지 않는다.

## 인증과 API 호출

- LocalSecurityConfig는 local 프로필 전용 개발 로그인이다. 실제 회원 인증으로 간주하지 않는다.
- 서버 세션, CSRF 보호, POST 로그아웃을 유지한다. 문제 해결을 위해 CSRF나 모든 경로의 인증을 일괄 해제하지 않는다.
- 현재 세션은 메모리에 있고 재시작 시 사라진다. Redis 등 외부 세션 저장소는 미정이다.
- 기본 쿠키는 HttpOnly, SameSite=Lax, Secure이며 local에서만 Secure=false다.
- CORE_BASE_URL로 호출 대상을 주입한다. 현재 CoreClient는 연결 3초, 읽기 5초 타임아웃을 사용한다.
- 새 API는 타입이 있는 요청/응답 DTO로 처리한다. 현재 setup의 문자열 응답 처리는 연결 확인 코드다.
- core 장애 시 사용자에게 이해 가능한 실패를 알리고 내부 예외를 노출하지 않는다. 변경 요청을 무조건 재시도하지 않는다.
- 사용자 인증과 서비스 간 인증은 별도다. 서비스 간 인증 방식은 미정이며 임의로 사용자 세션 쿠키를 전달하지 않는다.

## 검증과 실행

루트에서 .\gradlew.bat :user-service:assemble을 실행한다.
local 프로필 실행은 .\gradlew.bat :user-service:bootRun --args="--spring.profiles.active=local"이다.

현재 local 프로필은 127.0.0.1에 바인딩하고 CORE_BASE_URL의 기본값은 http://localhost:8081이다. 배포에서는 local 프로필을 사용하지 않는다. local 외 환경은 CORE_BASE_URL과 운영 인증 구성이 필요하다.

화면 변경 시 로그인, CSRF, 템플릿 렌더링, core 실패 처리를 영향 범위에 맞게 확인한다. 헬스 경로는 /actuator/health다. 현재 core가 없을 때 안내 화면을 보여주는 것은 DB 연동 성공을 의미하지 않는다.
