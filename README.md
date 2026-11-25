# 세종대학교 학내 행사 통합 관리 플랫폼 - Backend

> 2026-2학기 세종창의학기제 | Spring Boot 백엔드

## 프로젝트 개요

학내 행사에서 반복적으로 발생하는 두 가지 문제를 하나의 플랫폼으로 해결합니다.

- **행사 모드** (개강총회·전체학생총회): 운영진이 QR 발급 → 학생이 QR 스캔 후 학번 입력 → 번호표 발급 → 추첨 시 번호(+학번) 기반 랜덤 추첨
- **축제 모드** (세종연회): 부스 운영진이 웨이팅 등록·관리 → 실시간 대기 인원 WebSocket 브로드캐스트

## 기술 스택

- Java 21 / Spring Boot 3.3.6 / Gradle
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA + MySQL 8
- Spring WebSocket (STOMP over SockJS)
- SpringDoc OpenAPI (Swagger)

## 아키텍처

```
com.sejong.event/
├── auth/          # JWT 인증 (로그인·로그아웃)
├── user/          # 사용자 엔티티
├── account/       # 운영진 계정 관리 (superuser 전용)
├── event/         # 행사 CRUD
├── attendance/    # 입장 처리 (QR 스캔·수동 발급·추첨)
├── booth/         # 부스 CRUD + 승인/거부
├── waiting/       # 웨이팅 등록·관리
├── festival/      # 축제 ON/OFF 설정
└── config/        # Security, WebSocket, Swagger, CORS
```

## 역할 (Role)

| 역할 | 대상 | 권한 |
|------|------|------|
| `SUPERUSER` | 개발팀·최고 관리자 | 모든 리소스 접근, 계정 생성, 부스 승인/거부, 축제 ON/OFF |
| `ORGANIZER` | 학생회·동아리 운영진 | 본인 organization의 행사·부스 생성·관리 |
| 미인증 | 일반 학생 | 행사 QR 스캔 입장, 부스 조회, 웨이팅 등록 |

> 계정은 superuser가 직접 생성하여 전달합니다. 별도 회원가입 없음.

## 주요 API

### 인증
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| POST | `/api/auth/login` | 로그인 | 공개 |
| POST | `/api/auth/logout` | 로그아웃 | 인증 필요 |

### 행사 (Events)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/api/events` | 행사 목록 조회 | 공개 (역할별 필터링) |
| GET | `/api/events/{id}` | 행사 단건 조회 | 공개 |
| POST | `/api/events` | 행사 생성 | ORGANIZER 이상 |
| PUT | `/api/events/{id}` | 행사 수정 | ORGANIZER 이상 |
| DELETE | `/api/events/{id}` | 행사 삭제 | ORGANIZER 이상 |

### 입장 관리 (Attendance)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/api/events/{id}/qr-token` | QR 토큰 발급 (60초 유효) | ORGANIZER 이상 |
| POST | `/api/events/{id}/attendance/scan` | QR 스캔 입장 처리 | 공개 |
| POST | `/api/events/{id}/attendance/manual` | 수동 번호표 발급 | ORGANIZER 이상 |
| GET | `/api/events/{id}/attendance` | 입장 로그 목록 | ORGANIZER 이상 |
| POST | `/api/events/{id}/lottery` | 추첨 실행 | ORGANIZER 이상 |

### 부스 (Booths)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/api/booths` | 승인된 부스 목록 | 공개 |
| GET | `/api/booths/{id}` | 부스 상세 조회 | 공개 |
| POST | `/api/booths` | 부스 신청 (pending) | ORGANIZER 이상 |
| PUT | `/api/booths/{id}` | 부스 수정 | ORGANIZER 이상 |
| DELETE | `/api/booths/{id}` | 부스 삭제 | ORGANIZER 이상 |
| PATCH | `/api/booths/{id}/approve` | 부스 승인 | SUPERUSER |
| PATCH | `/api/booths/{id}/reject` | 부스 거부 | SUPERUSER |
| PATCH | `/api/booths/{id}/status` | 영업 상태 변경 | ORGANIZER 이상 |

### 웨이팅 (Waiting)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/api/booths/{id}/waiting` | 웨이팅 목록 | ORGANIZER 이상 |
| POST | `/api/booths/{id}/waiting` | 웨이팅 등록 | 공개 |
| POST | `/api/booths/{id}/waiting/next` | 다음 팀 호출 | ORGANIZER 이상 |
| DELETE | `/api/booths/{id}/waiting/{wid}` | 웨이팅 취소 | ORGANIZER 이상 |
| GET | `/api/booths/{id}/waiting/{wid}` | 내 웨이팅 상태 조회 | 공개 |

### 계정 관리 (Accounts)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/api/accounts` | 운영진 계정 목록 | SUPERUSER |
| POST | `/api/accounts` | 운영진 계정 생성 | SUPERUSER |
| DELETE | `/api/accounts/{id}` | 운영진 계정 삭제 | SUPERUSER |

### 축제 설정 (Festival)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/api/festival/status` | 축제 활성 여부 조회 | 공개 |
| PATCH | `/api/festival/status` | 축제 ON/OFF | SUPERUSER |

## WebSocket

연결 엔드포인트: `http://localhost:8080/ws` (STOMP over SockJS)

| 토픽 | 트리거 | payload |
|------|--------|---------|
| `/topic/events/{eventId}` | QR 스캔 입장 성공 시 | `{ eventId, attendanceCount }` |
| `/topic/booths/{boothId}` | 웨이팅 등록·취소·호출, 영업상태 변경 시 | `{ boothId, waitingCount, isOpen }` |

## 로컬 실행

### 1. MySQL DB 생성

```sql
CREATE DATABASE sejong_event CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. `application-local.yml` 비밀번호 확인

`src/main/resources/application-local.yml`에서 MySQL 비밀번호를 본인 환경에 맞게 수정:

```yaml
spring:
  datasource:
    password: your_mysql_password
```

### 3. 앱 실행 (테이블 자동 생성)

인텔리제이에서 `SejongEventApplication` Run 또는:

```bash
./gradlew bootRun
```

`ddl-auto: update` 설정으로 테이블이 자동 생성됩니다.

### 4. 초기 데이터

`local` 프로필로 빈 DB 에서 처음 실행하면 `DataSeeder` 가 관리자·운영진 계정, 축제 설정, 예시 행사·부스를 자동으로 넣는다.
(데이터가 이미 있으면 건너뜀)

계정 정보는 README 에 적지 않고 팀 내부 채널로만 공유한다.

### 5. Swagger UI

`http://localhost:8080/swagger-ui.html`

OpenAPI 명세 스냅샷: [`docs/openapi.yaml`](docs/openapi.yaml)
(재생성: `./gradlew test -Dopenapi.update=true` — `OpenApiDocsTest` 가 실시간 스펙과 경로 일치를 검증)

## 팀원 로컬 연동 (같은 와이파이 환경)

백엔드 담당자 컴퓨터 IP 확인:

```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

프론트엔드 `.env`에 추가:

```
VITE_API_URL=http://192.168.x.x:8080/api
```

## 배포

### 프로필

| 프로필 | 용도 | 특징 |
|--------|------|------|
| `local` (기본) | 로컬 개발 | `ddl-auto: update`, `show-sql: true`, 시크릿 기본값 있음 |
| `prod` | 운영 배포 | `ddl-auto: validate`, `show-sql: false`, 시크릿·DB 는 환경변수 필수(기본값 없음), Swagger 기본 비활성 |

`SPRING_PROFILES_ACTIVE` 로 선택한다. (Docker 이미지는 `prod` 가 기본)

### 환경변수 (prod)

| 변수 | 필수 | 설명 |
|------|------|------|
| `DB_URL` | ✅ | `jdbc:mysql://호스트:3306/sejong_event?...` |
| `DB_USERNAME` | ✅ | DB 계정 |
| `DB_PASSWORD` | ✅ | DB 비밀번호 |
| `JWT_SECRET` | ✅ | HMAC 서명 키 (256bit 이상) |
| `CORS_ALLOWED_ORIGINS` | ✅ | 허용 오리진 (쉼표 구분) |
| `SWAGGER_ENABLED` | | `true` 면 `/swagger-ui.html` 노출 (기본 false) |
| `SERVER_PORT` | | 기본 8080 |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | | 신규 DB 초기 구축 시 일시적으로 `update` |

### Docker

```bash
# 단독 실행 (외부 MySQL 필요)
docker build -t sejong-event-backend .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://<host>:3306/sejong_event \
  -e DB_USERNAME=... -e DB_PASSWORD=... -e JWT_SECRET=... \
  -e CORS_ALLOWED_ORIGINS=https://<프론트도메인> \
  sejong-event-backend

# 앱 + MySQL 한 번에 (로컬/스테이징)
docker compose up --build
```

### CD 파이프라인

`.github/workflows/deploy.yml` — `workflow_dispatch` (수동 실행) 시 bootJar 빌드 후
Docker 이미지를 `ghcr.io/ke-62/sejong_event_backend` 로 푸시한다.
실제 호스팅(EC2 / Railway 등) 배포 시점은 프론트엔드 배포와 맞추어 진행한다.

### 초기 데이터

신규 DB 는 최초 1회 `SPRING_JPA_HIBERNATE_DDL_AUTO=update` 로 테이블을 만들고,
`SPRING_PROFILES_ACTIVE=prod,local` 로 띄워 `DataSeeder` 가 초기 계정·데이터를 넣게 한다.
이후에는 `validate` 로 되돌린다. 계정 정보는 팀 내부 채널로만 공유한다.

## 브랜치 전략

```
main <- develop <- feat/이슈번호-작업내용
```

| 타입 | 설명 |
|------|------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 |
| `chore` | 설정 변경 |
| `docs` | 문서 |
