# 세종대학교 학내 행사 통합 관리 플랫폼 - Backend

> 2026-2학기 세종창의학기제 | Spring Boot 백엔드

## 프로젝트 개요

학내 행사에서 반복적으로 발생하는 두 가지 문제를 하나의 플랫폼으로 해결합니다.

- **행사 모드** (개강총회·전체학생총회): 학번 기반 QR 번호표 발급 → QR 스캔 입장 → 공정 추첨
- **축제 모드** (세종연회): 부스 운영자가 실시간 혼잡도 등록 → 이용자 전체에 WebSocket 브로드캐스트

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.6 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| DB | MySQL 8 (운영) / H2 (테스트) |
| ORM | Spring Data JPA + Hibernate |
| WebSocket | Spring WebSocket + STOMP |
| Docs | SpringDoc OpenAPI (Swagger) |
| Test | JUnit 5 + Mockito |
| CI | GitHub Actions |

## 아키텍처

```
com.sejong.event/
├── auth/          # JWT 인증 (로그인·회원가입·토큰 재발급)
├── user/          # 사용자 엔티티 (5개 역할)
├── event/         # 행사 CRUD + 상태 관리
├── ticket/        # 번호표 발급 + QR 스캔 + 추첨
├── booth/         # 부스 CRUD + 혼잡도 실시간 업데이트
├── admin/         # 관리자 전용 API
└── config/        # Security, WebSocket, Swagger 설정
```

## 역할 (Role)

| 역할 | 권한 |
|------|------|
| `PARTICIPANT` | 번호표 발급, 부스 조회 |
| `STAFF` | QR 스캔 입장 처리 |
| `ORGANIZER` | 행사 생성·관리, 추첨 실행 |
| `BOOTH_OPERATOR` | 부스 생성·혼잡도 수정 |
| `ADMIN` | 전체 관리 |

## WebSocket 토픽

| 토픽 | 이벤트 |
|------|--------|
| `/topic/event/{id}` | 행사 입장자 수 실시간 업데이트 |
| `/topic/booth/{id}` | 부스 혼잡도·영업 상태 실시간 업데이트 |

연결 엔드포인트: `ws://host/ws` (SockJS 지원)

## 주요 API

### 인증
| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/reissue` | Access Token 재발급 |
| POST | `/api/auth/logout` | 로그아웃 |

### 행사 (Event Mode)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| POST | `/api/events` | 행사 생성 | ORGANIZER |
| GET | `/api/events` | 전체 행사 조회 | 공개 |
| GET | `/api/events/active` | 진행 중 행사 | 공개 |
| PATCH | `/api/events/{id}/activate` | 행사 시작 | ORGANIZER |
| PATCH | `/api/events/{id}/close` | 행사 종료 | ORGANIZER |

### 번호표 (Ticket)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| POST | `/api/events/{id}/tickets` | 번호표 발급 (QR 토큰 포함) | 공개 |
| POST | `/api/events/{id}/tickets/scan` | QR 스캔 입장 | STAFF |
| POST | `/api/events/{id}/tickets/draw` | 추첨 실행 | ORGANIZER |

### 부스 (Festival Mode)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| POST | `/api/events/{id}/booths` | 부스 생성 | BOOTH_OPERATOR |
| GET | `/api/events/{id}/booths` | 부스 목록 | 공개 |
| PATCH | `/api/booths/{id}/congestion` | 혼잡도 업데이트 | BOOTH_OPERATOR |
| PATCH | `/api/booths/{id}/toggle` | 영업 상태 토글 | BOOTH_OPERATOR |

## 로컬 실행

### 1. MySQL DB 생성
```sql
CREATE DATABASE sejong_event CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 환경변수 설정
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key-must-be-at-least-256-bits-long
```

### 3. 실행
```bash
./gradlew bootRun
```

### Swagger UI
`http://localhost:8080/swagger-ui.html`

## 테스트 실행
```bash
./gradlew test
```

## 브랜치 전략

```
main ← develop ← feature/이슈번호-작업내용
```

- `feat`: 새 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `chore`: 설정 변경
- `docs`: 문서

## 팀

| 역할 | 이름 | 학번 | 담당 |
|------|------|------|------|
| 팀 대표 | 이규형 | 23011679 | 프론트엔드 (React + TypeScript) |
| 팀원 | 이고은 | 24010964 | 백엔드 (Spring Boot) |
