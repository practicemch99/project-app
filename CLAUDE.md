# 프로젝트 공통 코딩 지침

## 적용 범위와 작업 원칙

- 이 파일은 project-app 전체에 적용한다. 모듈을 수정하기 전에 해당 디렉터리의 CLAUDE.md도 읽는다.
- 사용자의 현재 요청을 우선하며, 문서와 코드가 다르면 구현 상태와 의도한 방향을 구분해서 설명한다.
- 필요한 파일과 기존 구현을 먼저 확인하고 요청 범위에 맞게 변경한다. 사용자의 작업을 되돌리지 않는다.
- 기술 선택이 미정이라고 해서 모든 구현을 멈추지 않는다. 기존 패턴으로 해결 가능한 세부 사항은 판단해서 진행하고, 서비스 경계나 새로운 운영 의존성이 필요한 선택은 먼저 논의한다.
- 문서와 사용자 설명은 한국어, Java 식별자와 SQL 파일명은 의미 있는 영어를 사용한다.
- 변경 후 변경 내용, 검증 결과, 미검증 사항을 구분해 보고한다.

## 목적과 저장소 경계

업무 도메인은 아직 정하지 않았다. 화면, 핵심 업무, 배치 책임을 분리한 Spring Boot 애플리케이션을 개발한다.

- 앱 저장소: https://github.com/practicemch99/project-app
- 인프라 저장소: https://github.com/practicemch99/project-infra
- 루트 Gradle 프로젝트에는 실행 코드를 두지 않는다.
- user/core/batch는 별도의 실행 단위다. database-migration은 스키마 갱신 도구다.
- Terraform과 AWS 리소스 생성 코드는 project-infra에서 관리한다.

```text
브라우저 → user-service → core-service → RDS MySQL
                         ↑
                   batch-service

database-migration → RDS MySQL (스키마 변경)
```

서비스 간에는 REST/JSON으로 통신한다. 다른 서비스의 Java 모듈을 직접 의존하거나 Entity/Repository/업무 Service를 공유하지 않는다. 필요성이 확인되지 않은 common 모듈은 만들지 않는다.

## 현재 코드의 기술 스택

| 항목 | 현재 설정 |
| --- | --- |
| Java | 컴파일 release 17, toolchain 미지정 |
| Spring Boot | 4.1.1 |
| Gradle Wrapper | 9.7.1, Groovy DSL 멀티 프로젝트 |
| Dependency Management Plugin | 1.1.7 |
| 기본 패키지 | com.example.project |
| 화면 | Spring MVC, Thymeleaf, HTML/CSS |
| 인증 | Spring Security, 서버 세션; local용 로그인 구현 |
| DB 접근 | Spring Data JPA, OpenFeign QueryDSL 7.0, MyBatis Starter 4.0.0 |
| 스키마 관리 | Flyway, MySQL 드라이버 |
| 배치 | Spring Boot CommandLineRunner, Spring Web RestClient |
| 관측 | user/core Actuator health |

표에 없는 의존성 버전은 Spring Boot 의존성 관리와 실제 Gradle 파일을 확인한다. 특정 PC의 JDK 경로를 빌드에 넣지 않는다. Java 17을 넘는 문법/API를 사용하거나 버전을 일괄 변경하지 않는다.

## 최신 DB·배포 방향과 남은 설정

- 로컬 MySQL은 사용하지 않고 배포된 앱이 AWS RDS MySQL에 접속하는 방향이다.
- RDS는 비공개로 두고 앱의 접속과 사람의 관리 접속을 분리한다.
- core는 업무 데이터용 DML 계정, migration은 스키마 변경용 계정을 사용한다.
- 사람의 직접 DB 접속은 허용된 관리자에게만 부여하고, 관리자별 계정과 SSM 터널을 사용하는 방향이다.
- 관리자 SSM 권한, DB 계정, 네트워크/TLS, 실제 RDS 연결은 아직 구현·검증된 것으로 간주하지 않는다.
- user와 batch는 DB에 직접 접근하지 않는다.
- EC2 + Docker Compose + RDS + Terraform, 첫 배포부터 CI/CD를 적용하는 것이 배포 방향이다. 구체적인 워크플로와 AWS 설정은 아직 없다.
- 현재 compose.local.yaml, local/mysql/init.sql, localhost:3307 기본값 및 README의 로컬 DB 안내는 이전 가정의 잔여 설정이다. 이를 최신 요구로 해석하거나 로컬 DB를 다시 필수 조건으로 만들지 않는다. 제거·RDS 설정 전환은 별도 구현 작업이다.
- 기존 localhost 및 SSL 비활성화 옵션을 RDS 접속 설정에 그대로 복사하지 않는다. 실제 비밀번호, AWS 키, 관리자 자격 증명을 코드·문서·로그에 남기지 않는다.
- 개발/운영 DB 분리 등 환경별 세부 구성을 확정된 인프라처럼 만들지 않는다.

## 공통 코드 작성 규칙

- 생성자 주입을 사용한다. Controller는 입출력과 호출 조정에 집중하고 업무 규칙은 application/domain에 둔다.
- API DTO와 Entity를 분리하고 HTTP 입력은 경계에서 검증한다.
- 실패를 성공으로 바꾸거나 빈 결과로 숨기지 않는다. 외부 응답에 SQL, 자격 증명, 스택 트레이스를 노출하지 않는다.
- 업무 시간대는 Asia/Seoul이다. 날짜와 일시를 구분하고 API 일시는 오프셋을 가진 타입을 사용한다. JSON 오프셋 계약과 직렬화 검증은 기능 구현 시 함께 적용한다.
- bootRun에는 JVM 시간대가 설정되어 있다. JAR/배포 실행에도 동일한 설정이 필요하다.
- 공통 오류 형식(code, message, timestamp, path, traceId)은 초안이다. 실제 추적 구현 없이 traceId를 임의 생성해 추적이 된다고 표현하지 않는다.
- 필요한 계층만 만들고 빈 패키지, 추상화, 프레임워크를 선제적으로 늘리지 않는다.
- 새 의존성은 해당 모듈에만 추가한다. 기존 의존성으로 해결 가능한지 먼저 확인한다.

## 빌드와 검증

프로젝트 루트에서 PowerShell 기준:

```powershell
.\gradlew.bat assemble
.\gradlew.bat :user-service:assemble
.\gradlew.bat :core-service:assemble
.\gradlew.bat :batch-service:assemble
.\gradlew.bat :database-migration:assemble
```

Unix 계열에서는 ./gradlew를 사용한다. JAVA_HOME에는 실행 가능한 JDK가 필요하다. 실행 JAR은 각 모듈의 build/libs/<모듈명>.jar에 생성된다.

- 코드 변경 시 영향받은 모듈의 컴파일·패키징을 확인한다. 공통 빌드 변경은 전체 assemble로 검증한다.
- 초기 기획상 자동화 테스트 도입은 보류 상태다. 테스트 프레임워크를 임의 도입하지 않으며, 이후 테스트가 도입되면 관련 테스트도 실행한다.
- assemble 성공은 RDS 연결·migration·로그인 등 통합 동작 성공을 의미하지 않는다.
- 원격 DB 접속이나 migration은 대상 환경과 작업 범위가 확인된 실행에서만 한다. 문서 편집이나 빌드 검증 때문에 실행하지 않는다.

## 미결정 또는 미구현

실제 업무 도메인, 팀 JDK/toolchain 통일, RDS 엔진 세부 버전, 운영 회원·권한 모델, 서비스 간 인증, 세션 저장소, API 오류 상세 계약, 배치 프레임워크·스케줄러·재시도·중복 방지, CI/CD 세부 구성이 남아 있다.

jQuery와 DataTables는 도입 예정이며 현재 의존성/정적 파일은 없다. 현재 연결 확인용 화면과 setup_marker를 실제 업무 요구로 확장하지 않는다.
