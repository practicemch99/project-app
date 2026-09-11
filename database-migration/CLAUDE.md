# database-migration 코딩 지침

루트 CLAUDE.md와 함께 적용한다.

## 역할과 현재 기술

- RDS 스키마 변경을 한곳에서 관리하는 도구 모듈이다. 업무 서비스나 DB 계정 관리 도구가 아니다.
- Spring Boot JDBC/Flyway Starter, flyway-mysql, MySQL 드라이버를 사용한다. 세부 버전은 Boot 의존성 관리에 따른다.
- 기본 패키지: com.example.project.migration.
- 웹 서버 없이 시작 시 Flyway를 실행하고 컨텍스트를 닫는다. 앱 시작 자체가 DB 변경 실행이다.
- core와 batch에 migration SQL을 복제하지 않는다.

## SQL 작성 규칙

- 위치: src/main/resources/db/migration.
- 파일명: V{번호}__{영문_설명}.sql. 예: V2__create_member_table.sql.
- 번호 중복을 확인하고 변경 목적이 명확한 새 버전 파일을 추가한다.
- 이미 적용된 파일은 수정/삭제/재번호화하지 않는다. 적용 여부가 불확실하면 먼저 확인한다.
- V1__create_local_setup_marker.sql은 기존 연결 확인용이다. 이름에 local이 있어도 실행 시 해당 대상 DB에 적용된다. 업무 테이블이나 개발 전용 프로필 제한으로 오해하지 않는다.
- 업무 요구에 필요한 테이블·제약·인덱스만 추가한다. 문자셋, nullable, 기본값과 시간 저장 정책을 검토한다.
- 실제 비밀번호, 관리자 계정 생성, 운영 개인정보나 임의 샘플 업무 데이터를 migration에 넣지 않는다.

## 배포 호환성과 파괴적 변경

- 일반 순서는 migration 성공 → 관련 core 배포 → user/batch 배포다. migration 실패 후 앱 배포를 계속하지 않는다.
- 이전 앱과 새 앱이 함께 사용할 수 있는 추가 변경을 우선한다.
- 컬럼 삭제/타입 축소/이름 변경은 소비자와 기존 데이터 영향을 검토하고 단계적으로 수행한다.
- DROP, TRUNCATE, 대량 삭제 등 데이터 손실 변경은 대상과 영향, 복구 방법을 제시하고 해당 작업의 사용자 승인을 확인한 뒤 실행한다.
- 변경 SQL의 작성과 실제 원격 DB 적용을 구분한다. 파일 작성 요청은 RDS 적용 요청이 아니다.
- MySQL DDL 전체가 트랜잭션으로 롤백된다고 가정하지 않는다. 실패 시 적용 상태와 Flyway 이력을 먼저 조사한다.
- checksum 실패를 해결하려고 repair/clean을 자동 실행하지 않는다. 원인을 확인하고 검토된 복구 절차를 따른다.
- clean-disabled=true를 유지한다.

## 계정과 실행 검증

- DB_URL, MIGRATION_DB_USERNAME, MIGRATION_DB_PASSWORD를 외부에서 주입한다.
- migration 전용 계정은 필요한 대상 스키마의 DDL 권한을 가진다. RDS 관리자 자격 증명을 앱에 공유하지 않는다.
- 계정 생성과 관리자 접속 권한은 별도 인프라/관리 절차로 준비한다.
- 현재 local 프로필의 localhost:3307 계정 설정은 이전 로컬 MySQL 구성의 잔여 값이다. RDS 연결이 준비된 것으로 간주하지 않는다.
- 루트에서 .\gradlew.bat :database-migration:assemble로 패키징만 검증할 수 있다. 이 명령은 DB에 적용하지 않는다.
- 실제 bootRun/JAR 실행 전에는 환경, DB 주소/스키마, 적용 버전, 계정 권한과 실행 승인을 확인한다.
- 실제 적용 후 Flyway 이력과 대상 스키마를 확인한다. 패키징만 했다면 DB 적용은 미검증으로 보고한다.
