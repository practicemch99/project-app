# batch-service 코딩 지침

루트 CLAUDE.md와 함께 적용한다.

## 역할과 현재 기술

- 배치 실행의 입력과 순서, core API 호출, 실행 결과 처리를 담당한다.
- 업무 규칙과 데이터 변경은 core API에 위임한다. DB 직접 접근, DB 자격 증명, JPA/MyBatis 의존성을 추가하지 않는다.
- 현재 기술은 Spring Boot Starter, Spring Web RestClient, CommandLineRunner다. Spring Batch는 도입하지 않았다.
- 기본 패키지: com.example.project.batch.
- web-application-type=none이며 상주 웹 서버가 아니다. 현재는 실행 후 컨텍스트를 닫는 1회 실행 프로세스다.
- SetupCheckRunner는 core의 /api/v1/setup 조회 후 결과를 기록하는 연결 확인 작업이다.

## 코드 배치와 호출

- job/<작업명>: 작업 실행 코드와 해당 작업의 구성.
- application: 여러 단계의 실행 조정.
- client/core: REST 호출 및 API DTO.
- config: 실행 설정.
- CORE_BASE_URL로 대상을 주입한다. 현재 클라이언트의 연결 3초/읽기 5초 타임아웃을 기준으로 작업에 맞게 검토한다.
- 업무 처리 로직을 core에서 복사하지 않는다. 필요한 API가 없으면 core의 계약을 함께 설계한다.
- 새 작업이 추가되면 작업명/실행 인자로 실행 대상을 명시한다. 여러 CommandLineRunner가 모든 작업을 무조건 실행하는 구조를 만들지 않는다.

## 실행과 실패 처리

- 실패를 잡아서 성공 종료로 바꾸지 않는다. 실패 상태가 실행 주체에 전달되도록 한다.
- 작업별 입력, 처리 범위, 성공 조건, 재실행 방법을 문서화한다.
- 변경 API의 재시도 전에 멱등성, 중복 요청 및 부분 성공을 설계한다. 무조건 재시도하지 않는다.
- 실행 식별자, 작업명, 처리 결과와 실패 요약을 기록하되 비밀정보/불필요한 개인정보를 기록하지 않는다.
- 스케줄러, 재시도, 중복 실행 방지, 실행 이력 저장, 대량 처리 프레임워크는 아직 미정이다.
- Spring Batch, Quartz, @Scheduled, 별도 DB를 임의 도입하지 않는다. 실제 작업 요구가 생기면 실행 정책을 정한다.

## 검증

루트에서 .\gradlew.bat :batch-service:assemble을 실행한다.
현재 local 실행: .\gradlew.bat :batch-service:bootRun --args="--spring.profiles.active=local"

이 명령은 실제 core API를 호출하므로 대상 CORE_BASE_URL을 확인한다. 현재 기본값은 http://localhost:8081이다. 성공 종료와 core 장애 시 실패 종료를 구분해 확인한다. 실제 데이터 변경 작업이 추가되면 단순 빌드 확인을 위해 실행하지 않는다.
