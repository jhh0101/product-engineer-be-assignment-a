# Product Engineer(BE-A)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/springboot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)

![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%232496ED.svg?style=for-the-badge&logo=docker&logoColor=white)

![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![Swagger](https://img.shields.io/badge/-Swagger-%23C1272D?style=for-the-badge&logo=swagger&logoColor=white)

## 프로젝트 개요
강의 등록 및 수강 신청, 대기열 관리 시스템입니다. 확장성과 대규모 트래픽 상황을 고려하며 동시성 문제를 해결하는 것에 초점을 맞췄습니다.

### 기술 스택
* **Framework & Language**: SpringBoot 4.0.5, JAVA 21
* **Data Access**: Spring Data JPA, Redis
* **Database**: H2, MySQL 8.0
* **Infrastructure**: Docker, Docker Compose
* **Tools**: IntelliJ IDEA, Git, DBeaver, Postman

### 실행 방법
```bash
docker compose up -d
```

## 요구사항 해석 및 가정
**사용자 권한**
- 크리에이터(강사), 클래스메이트(수강생)을 고려하여 USER(수강생), CREATOR(강사), ADMIN(현 프로젝트에선 사용하지 않음)로 나누었습니다.
- 강사는 강의를 개설하고, 개설한 강의별 수강생 목록을 조회할 수 있습니다.
- 수강생은 수강신청과 결제 확정, 수강 취소(결제 확정 전 또는 결제 확정 후 7일 이내), 자신의 수강 신청 목록을 조회할 수 있습니다.

**강의 모집 기간**
- 수강 기간과 강의 모집 기간은 별개로 판단했습니다.
- 수강 기간은 강의 시작 날짜와 강의 종료 날짜를 저장하였습니다.(개강 ~ 종강)
- 강의 모집 기간은 개강 날짜의 하루 전날로 잡았습니다.(OPEN 상태 변경 ~ 개강 - 1일)

**동시성 문제**
- DB의 비관적 락을 고려했으나 수강 신청 상황에서 대규모 트래픽의 발생으로 인한 DB의 병목현상을 고려하여 Redis의 단일 스레드로 동시성 문제를 해결했습니다.

**대기열 기능**
- 강의별 최대 정원을 초과하면 신청은 거부하지만 선택 구현의 대기열 기능을 고려하여 수강 신청 목록에는 들어가지 않으나 Redis의 대기열로 들어갑니다.
- 수강 취소 시 대기열에 있는 사용자를 자동으로 신청자 승급을 해주었습니다.

## 설계 결정과 이유
**MySQL**
- 주요 데이터를 저장하기 매우 안전하며 보편적으로 많이 사용하는 MySQL을 선택했습니다.

**H2**
- 테스트 코드를 사용할 때 MySQL과 같은 반영구적 DB를 사용하는 것 보단 서버 실행, 종료와 동시에 메모리에서 삭제되는 휘발성이 강한 H2를 사용했습니다.

**Redis**
- 대규모 트래픽과 동시성 문제, DB의 부담을 줄이기 위한 최적화(대기열)를 위하여 사용하였습니다.
- 비즈니스 로직과 Redis의 분리를 위하여 AOP, EventPublisher를 사용하였습니다.

**Docker**
- MySQL이나 Redis와 같은 로컬에서 돌리기 무거운 설정들을 도커의 이미지로 가볍고 빠르게 실행할 수 있기 때문에 사용하였습니다.

**JPA/QueryDSL**
- 크게 복잡한 쿼리문을 사용하지 않기 때문에 MyBatis보다 편하고 빠르게 처리할 수 있는 JPA를 사용했습니다
- 검색 조건과 같은 복잡한 조회 로직은 QueryDSL로 필터링 처리하여 구현하였습니다.

**Entity 연관 관계**
- 수강 신청의 대규모 트래픽 발생 시 MSA방식의 도입 가능성을 생각하여 @ManyToOne과 같은 연관 관계 설정보단 논리적 FK를 활용하여 Client 인터페이스로 DB 테이블간의 연동하였습니다.

**스케줄러**
- DB와 Redis간의 데이터 정합성이 틀어지는 위험성을 알고 있기 때문에 만약의 경우에 대비하여 서버 실행 시, 10분 주기 간격으로 DB와 Redis의 데이터 동기화를 구현했습니다.

**공통 응답/에러 처리**
- Back-end의 응답 처리에 일간성이 없다면 Front-end의 일관적인 화면 응답 처리가 힘들어지기 때문에 공통적인 응답/에러 처리를 사용하였습니다.(작업의 효율성)

**Swagger**
- 협업의 중요성을 고려하여 API 문서 자동화 기능을 사용하였습니다.
- http://localhost:8080/swagger-ui/index.html

## 미구현 / 제약사항
**대기열 기능**
- 대기열을 Redis에 저장하였기 때문에 갑작스럽게 Redis에 네트워크 문제나 서버 다운과 같은 예상치 못한 변수가 생겼을 때 대기열에 모든 데이터가 지워지는 상황이 생길 수 있습니다.
- 이를 위해서 클러스터나 센티널과 같은 고가용성을 사용하거나 Redis 메모리 전체를 디스크 파일에 백업해두는 방식을 고려하고 있습니다.

## AI 활용 범위
- 도메인 분리 및 아키텍처 설계 방향성 리뷰
- 작성 코드 리뷰 및 보완
- 오류 로그 분석을 통한 트러블슈팅 및 코드 최적화 방안 도출
- 복잡한 비즈니스 로직(대기열 승격, 낙관적 락)의 테스트 코드 케이스 작성 지원

## API 목록 및 예시
**POST** /api/enrollment/{courseId}
```
수강 신청 성공 - 수강자 목록에 저장
{
  "success": true,
  "code": "C001",
  "message": "수강 신청 성공",
  "data": {
    "id": 0,
    "name": "string",
    "title": "string",
    "status": "PENDING",
    "enrolledAt": "2026-04-26T07:57:47.684Z"
  }
}
```
```
수강 신청 성공 - 대기열에 저장
{
  "success": true,
  "code": "C000",
  "message": "정원이 초과되어 대기열에 등록되었습니다.",
  "data": {
    "id": 3,
    "name": "string",
    "title": "string",
    "waitNumber": Long,
    "totalWaitingCount": Long
  }
}
```
```
수강 신청 실패 - 수강 신청 강의를 찾을 수 없음
{
  "success": false,
  "code": "COURSE002",
  "message": "강의를 찾을 수 없습니다."
}
```
Swagger를 사용하므로 자세한 것은 링크를 통하여 확인해주세요.
- http://localhost:8080/swagger-ui/index.html

## 데이터 모델 설명
> **💡 설계 포인트:** 도메인 주도 설계(DDD) 관점에서 모듈 간의 독립성을 보장하기 위해, JPA의 물리적인 객체 연관관계(`@ManyToOne` 등)를 맺지 않고 **논리적 FK(ID 참조)**를 기반으로 설계했습니다.

### 1. users (사용자)
시스템을 이용하는 강사와 수강생의 기본 정보를 관리합니다.
- `role`: 사용자의 권한을 구분합니다. (`USER`: 수강생, `CREATOR`: 강사)

| 필드명 | 타입 | 설명 | 비고 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | 사용자 고유 식별자 | PK, AUTO_INCREMENT |
| `name` | VARCHAR | 사용자 이름 | NOT NULL |
| `role` | VARCHAR | 사용자 권한 | USER, CREATOR, ADMIN |

### 2. course (강의)
개설된 강의의 상세 정보와 수강 정원 현황을 관리합니다.
- 특징: 대규모 트래픽 발생 시 병목을 막기 위해 실시간 정원 증감은 Redis에서 담당하며, DB에는 배치 스케줄러 및 최종 결과만 동기화됩니다.

| 필드명 | 타입 | 설명 | 비고 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | 강의 고유 식별자 | PK, AUTO_INCREMENT |
| `creator_id` | BIGINT | 강의를 개설한 강사의 ID | 논리적 FK |
| `title` | VARCHAR | 강의 제목 | NOT NULL |
| `price` | INT | 강의 가격 | NOT NULL |
| `max_capacity` | INT | 수강 최대 정원 | NOT NULL |
| `current_capacity`| INT | 현재 수강 확정 인원 | Default 0 |
| `start_time` | DATETIME | 강의 시작(개강) 일시 | NOT NULL |
| `end_time` | DATETIME | 강의 종료(종강) 일시 | NOT NULL |
| `status` | VARCHAR | 강의 모집 상태 | DRAFT, OPEN, CLOSED |

### 3. enrollment (수강 신청 내역)
사용자의 수강 신청, 결제, 취소 상태를 관리합니다.
- 특징: 동시에 여러 건의 취소 요청이 들어올 때 발생할 수 있는 데이터 무결성 훼손을 방지하기 위해, `version` 필드를 활용한 **낙관적 락(Optimistic Lock)**을 적용했습니다.

| 필드명 | 타입 | 설명 | 비고 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | 수강 이력 고유 식별자 | PK, AUTO_INCREMENT |
| `user_id` | BIGINT | 수강생 ID | 논리적 FK |
| `course_id` | BIGINT | 강의 ID | 논리적 FK |
| `status` | VARCHAR | 수강 상태 | PENDING, CONFIRMED, CANCELLED |
| `enrolled_at` | DATETIME | 결제 확정 일시 | NULLABLE |
| `version` | BIGINT | 낙관적 락 제어용 관리 필드 | @Version |

## 테스트 실행 방법
본 프로젝트는 비즈니스 로직 검증을 위한 **단위 테스트**와, Redis 동시성 제어 및 대기열 로직을 검증하는 **통합 테스트**가 작성되어 있습니다.

### ⚙️ 테스트 환경
- **Database**: 테스트 실행 시 인메모리 DB인 **H2**로 자동 구동되므로 별도의 DB 세팅이 필요하지 않습니다.
- **Redis (Testcontainers)**: 통합 테스트 실행 시, 환경 파편화로 인한 Redis 명령어 호환성 문제를 방지하고 격리된 환경을 보장하기 위해 **Testcontainers**를 사용합니다.
- **주의:** Testcontainers가 테스트용 Redis 컨테이너를 자동으로 띄우고 내리기 때문에, **테스트를 실행하는 PC에 반드시 Docker (Docker Desktop 등)가 실행 중이어야 합니다.** (`docker compose up`을 실행할 필요는 없습니다.)

### 🚀 실행 명령어
프로젝트 루트 디렉토리에서 터미널을 열고 아래 명령어를 입력하여 전체 테스트를 실행할 수 있습니다.

```bash
# Mac / Linux
./gradlew test

# Windows
gradlew.bat test
```