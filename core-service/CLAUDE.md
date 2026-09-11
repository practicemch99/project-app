# core-service 코딩 지침

루트 CLAUDE.md와 함께 적용한다.

## 역할과 현재 기술

- 핵심 비즈니스 규칙, 데이터 검증, REST/JSON API, RDS 업무 데이터 접근을 담당한다.
- user와 batch는 이 모듈의 API를 호출한다. 화면 렌더링과 배치 스케줄링은 여기서 구현하지 않는다.
- Spring Boot Web MVC, Validation, Data JPA, Actuator, MyBatis Starter 4.0.0, OpenFeign QueryDSL 7.0, MySQL 드라이버를 사용한다.
- QueryDSL은 io.github.openfeign.querydsl 의존성과 com.querydsl Java 패키지를 사용한다. Jakarta annotation processor를 유지한다.
- 기본 패키지: com.example.project.core. 기본 포트는 8081이다.

## 코드 배치와 업무 규칙

- presentation/api, request, response, error: HTTP 경계와 DTO.
- application/service: 유스케이스와 트랜잭션 경계.
- application/port: 실제로 필요한 외부 의존 계약.
- domain/model, policy: 업무 모델과 규칙.
- infrastructure/persistence/jpa, querydsl, mybatis: 저장 기술 구현.
- config: 프레임워크 설정.
- 새 업무 Controller에서 Repository/Mapper를 직접 호출하지 않는다. application 서비스에 위임한다.
- 기존 SetupController의 JdbcTemplate 직접 조회는 연결 확인용 예외다. 이를 업무 기능의 구조로 복제하지 않는다.
- Entity를 API 응답으로 반환하지 않는다. 요청 DTO에 검증을 적용하고 업무 제약은 서비스/도메인에서 검증한다.

## 영속성과 트랜잭션

- 일반 CRUD는 JPA, 동적 조건 조회는 QueryDSL, 복잡한 SQL·집계·대량 처리는 MyBatis를 기준으로 선택한다.
- 하나의 기능에 세 기술을 불필요하게 혼합하지 않는다.
- 쓰기 트랜잭션은 application/service에 둔다. 여러 저장 작업의 원자성과 실패 전파를 명확히 한다.
- 같은 트랜잭션에서 JPA와 SQL 기반 쓰기를 섞으면 flush/clear 및 영속성 컨텍스트 불일치를 검토한다.
- open-in-view=false와 ddl-auto=validate를 유지한다. 스키마 불일치를 update/create로 우회하지 않는다.
- Q 클래스는 build 아래 생성 결과다. 직접 편집하거나 커밋하지 않는다.
- MyBatis XML은 src/main/resources/mapper 아래에 두고 #{...} 바인딩을 사용한다. 정렬 컬럼 등 동적 식별자는 허용 목록으로 제한한다.
- 목록 API는 데이터량에 맞는 페이징과 정렬을 설계하고 N+1 조회를 확인한다.

## RDS와 API 계약

- DB_URL, DB_USERNAME, DB_PASSWORD로 접속 정보를 주입한다. 앱 계정에 관리자/DDL 권한을 요구하지 않는다.
- 스키마 변경은 database-migration에 추가한다. core의 Flyway 자동 실행을 활성화하지 않는다.
- 현재 local 프로필의 localhost:3307은 이전 설정이며 RDS 연결 구현이 끝났다는 뜻이 아니다.
- API 변경 시 user와 batch 호환성을 검토한다. 응답 필드 삭제/타입 변경을 소비자 수정 없이 진행하지 않는다.
- 적절한 HTTP 상태와 사용자에게 필요한 오류만 반환한다. 공통 오류 상세 규격과 서비스 인증은 아직 미정이다.
- /api/v1/setup 및 setup_marker는 초기 연결 확인용이다. 실제 업무 모델로 간주하지 않는다.

## 검증

루트에서 .\gradlew.bat :core-service:assemble을 실행한다.
DB 설정과 접속 경로가 준비된 환경에서만 기동·통합 확인한다. 확인 항목은 /actuator/health, 변경 API, 트랜잭션 실패 처리 및 migration 호환성이다. 컴파일 성공과 실제 DB 동작 검증을 구분해 보고한다.
