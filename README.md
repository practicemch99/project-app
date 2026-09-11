# 프로젝트 로컬 개발

기획 초안을 바탕으로 만든 개발 시작점입니다. 기존 이름 `project`, 패키지 `com.example.project`, Spring Boot 4.1.1, Gradle Wrapper 9.7.1을 유지합니다.
Java 소스/바이트코드 기준은 17(`--release 17`)이며, Gradle은 IntelliJ에 설치된 JDK 26에서도 실행할 수 있습니다.
특정 PC의 JDK 경로는 빌드 파일에 고정하지 않았습니다. 팀 JDK/toolchain 버전은 추후 통일하세요.

| 모듈 | 역할 | 로컬 주소 |
| --- | --- | --- |
| user-service | Thymeleaf, Spring Security 세션, core 호출 | http://localhost:8080 |
| core-service | REST, JPA, QueryDSL, MyBatis, MySQL | http://localhost:8081 |
| batch-service | core API 연결 확인 후 종료 | 웹 포트 없음 |
| database-migration | Flyway 적용 후 종료 | 웹 포트 없음 |
| MySQL | Docker 개발 DB | localhost:3307 / appdb |

## 1. 준비

- IntelliJ에서 이 디렉터리의 `settings.gradle`을 Gradle 프로젝트로 열고 **Reload All Gradle Projects**를 실행합니다.
- Settings → Build Tools → Gradle → Distribution은 **Wrapper**, Gradle JVM은 설치된 JDK 26(또는 Gradle과 호환되는 JDK)로 지정합니다.
- Docker Desktop을 설치/실행하고 Linux containers 엔진을 준비합니다. `docker compose version`과 `docker info`가 성공해야 합니다.
- 현재 확인한 PC는 JDK가 `C:\Users\문창현\.jdks\openjdk-26.0.2`에 있고 터미널 PATH에는 Java/Docker가 없습니다.

PowerShell 터미널에서 직접 실행할 때만 해당 세션에 설정합니다(설치 위치가 다르면 변경).

```powershell
cd C:\practice\project
$env:JAVA_HOME = 'C:\Users\문창현\.jdks\openjdk-26.0.2'
$env:GRADLE_USER_HOME = "$env:USERPROFILE\.gradle"
.\gradlew.bat assemble
```

## 2. MySQL 시작

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose -f compose.local.yaml up -d --wait
```

`.env`는 Git에서 제외되며 **Compose만 읽습니다**. IntelliJ/Spring이 자동으로 읽지 않습니다.
Spring의 로컬 값은 `application-local.yml`에서 가져오고 환경변수로 덮어쓸 수 있습니다.
DB 포트 충돌로 `.env`의 MYSQL_PORT를 바꾸면 core/migration 실행 환경의 DB_URL도 함께 변경하세요.

## 3. 실행 순서

IntelliJ 실행 목록의 공유 Gradle 설정을 사용합니다. 보이지 않으면 Gradle 창에서 아래 태스크를 실행합니다.

1. `database-migration (local)`: 성공 종료까지 기다립니다.
2. `core-service (local)`: 계속 실행합니다.
3. `user-service (local)`: 계속 실행합니다.
4. 브라우저에서 http://localhost:8080 접속 후 아래 로컬 계정으로 로그인합니다.
5. 선택적으로 `batch-service (local)`을 실행합니다. core API 조회 성공 후 종료합니다.

동일한 PowerShell 명령(상주 서비스는 각각 별도 터미널):

```powershell
.\gradlew.bat :database-migration:bootRun --args="--spring.profiles.active=local"
.\gradlew.bat :core-service:bootRun --args="--spring.profiles.active=local"
.\gradlew.bat :user-service:bootRun --args="--spring.profiles.active=local"
.\gradlew.bat :batch-service:bootRun --args="--spring.profiles.active=local"
```

로컬 로그인: **developer / local-developer-only**.
메모리 세션이므로 user 재시작 시 로그아웃됩니다. 기본 CSRF 보호를 유지합니다.
웹 서비스는 local 프로필에서 127.0.0.1에만 바인딩합니다.

## 4. 연결 확인과 종료

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8081/api/v1/setup
Invoke-RestMethod http://localhost:8080/actuator/health
docker compose -f compose.local.yaml stop
```

화면에 `Local database initialized`가 표시되면 SSR → core → DB 연결이 성공한 것입니다.
core가 내려가 있으면 화면에 연결 확인 안내가 표시됩니다. Batch는 연결 실패 시 실패 종료합니다.
헬스 경로는 `/actuator/health`입니다. 향후 ALB 경로도 이 값으로 맞추세요.
Docker stop/down은 데이터를 보존합니다. `down -v`는 DB 데이터를 삭제하므로 초기화할 때만 사용합니다.
MySQL 초기 계정 SQL은 새 볼륨에서 한 번만 실행됩니다. 기존 볼륨에서는 SQL이나 .env를 바꿔도 계정 비밀번호가 바뀌지 않습니다.

## 5. 설정

| 변수 | 적용 대상 | local 기본값 |
| --- | --- | --- |
| DB_URL | core, migration | localhost:3307/appdb, Asia/Seoul |
| DB_USERNAME / DB_PASSWORD | core | app_core / local-core-only |
| MIGRATION_DB_USERNAME / MIGRATION_DB_PASSWORD | migration | app_migration / local-migration-only |
| CORE_BASE_URL | user, batch | http://localhost:8081 |
| LOCAL_USERNAME / LOCAL_PASSWORD | user | developer / local-developer-only |
| MYSQL_ROOT_PASSWORD / MYSQL_PORT | Compose | .env.example 참고 |

이 값은 로컬 개발 전용 공개 예시입니다. 운영에서 local 프로필을 사용하지 마세요.
local 외 프로필은 DB/core 연결 값을 반드시 외부에서 주입해야 합니다.
core 계정은 DML만, migration 계정은 appdb에 한정된 DDL 권한을 가집니다.
Hibernate는 validate만 사용하며 core에서 Flyway를 실행하지 않습니다.

## 6. 개발 규칙 및 남은 결정

- 스키마 변경은 `database-migration/src/main/resources/db/migration/V{번호}__{설명}.sql`을 추가합니다. 적용된 파일은 수정하지 않습니다.
- V1의 setup_marker는 연결 확인용 기술 테이블이며 업무 모델이 아닙니다.
- JPA Entity/Repository 및 업무 트랜잭션은 core에 둡니다. MyBatis XML은 core의 `src/main/resources/mapper` 아래에 추가합니다.
- QueryDSL은 Hibernate 7에 대응하는 OpenFeign fork 7.0과 Jakarta annotation processor를 사용합니다. Q 클래스는 Gradle 컴파일 시 build 아래에 생성됩니다.
- user에는 DB 의존성이 없습니다. batch는 기획의 마지막 메모에 맞춰 core API만 호출합니다.
- 배치 프레임워크/스케줄러/재시도/중복 방지는 아직 선택하지 않았습니다. 현재 batch는 API 연결 확인용 1회 실행 프로그램입니다.
- jQuery/DataTables는 업무 화면 구현 시 버전·라이선스를 확인해 static/vendor에 추가합니다. 현재 확인 화면에는 필요하지 않아 포함하지 않았습니다.
- 실제 회원 모델, 운영 인증/서비스 간 인증, 세션 저장소, 공통 API 오류, 날짜 JSON 계약은 추후 구현 대상입니다.
- bootRun은 JVM 업무 시간대를 Asia/Seoul로 설정합니다. PowerShell에서 JAR 직접 실행 시 `java '-Duser.timezone=Asia/Seoul' -jar ...`를 사용합니다.
- AWS/Terraform/배포 워크플로는 이번 로컬 설정 범위에 포함하지 않았습니다. 인프라는 별도 저장소로 관리합니다.
- 기획에 따라 자동화 테스트는 추가하지 않았습니다. 기존 생성기의 src는 `local/initial-project/src`에 보관했으며 빌드/Git 대상에서 제외했습니다.
- 모듈별 JAR은 `<모듈>/build/libs/<모듈>.jar`에 생성됩니다. `assemble`은 컴파일과 패키징을 검증합니다.

버전 참고: [Spring Boot 요구 사항](https://docs.spring.io/spring-boot/system-requirements.html),
[MyBatis Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/),
[QueryDSL 7 릴리스](https://github.com/OpenFeign/querydsl/releases).

## 초기 설정 검증 결과

- 네 모듈 모두 Gradle assemble 성공 및 실행 JAR 생성 확인.
- user 로컬 기동, health UP, 로그인 페이지 200, CSRF 토큰을 사용한 로그인 및 Thymeleaf 화면 200 확인.
- core 미기동 시 연결 실패 안내 렌더링 확인. 검증용 user 프로세스는 종료함.
- Docker 명령/설치 경로를 찾지 못해 MySQL 컨테이너, Flyway 적용, 실제 DB API 및 batch 성공 실행은 미검증.
- IntelliJ 공유 실행 설정은 파일을 작성했으며 IDE에서 직접 실행하는 검증은 수행하지 않음.
