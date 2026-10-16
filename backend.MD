<div align="center">

# Start Study — Backend

개발자를 위한 온라인 스터디 매칭 플랫폼 · REST API 서버

![CI/CD](https://github.com/StartStudy-project/Backend/actions/workflows/CICD.yml/badge.svg?branch=develop)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen)
![License](https://img.shields.io/badge/build-Gradle-blue)

</div>

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [핵심 기능](#핵심-기능)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API 문서](#api-문서)
- [테스트](#테스트)
- [커밋 컨벤션](#커밋-컨벤션)

## 프로젝트 소개

**Start Study**는 CS / 코딩테스트 / 프로젝트 / 기타 카테고리로 스터디 게시글(`board`)을 등록하고, 온라인·오프라인(카카오맵 위치 기반) 모집 방식을 선택해 스터디원을 모으는 커뮤니티형 플랫폼의 백엔드입니다. 게시글에는 계층형 댓글(`reply`)과 관심글 저장(`postlike`) 기능이 있으며, 관리자용 회원/게시글 관리와 반복 위반자에 대한 블랙리스트(`blacklist`) 자동 제재 로직을 포함합니다.

## 핵심 기능

| 영역 | 기능 |
| --- | --- |
| **인증** | 자체 회원가입/로그인(BCrypt), 이메일 인증(SMTP), 카카오·네이버 소셜 로그인, JWT 발급/재발급(Access·Refresh Token) |
| **게시글** | 카테고리(CS/코테/프로젝트/기타) · 온라인/오프라인(카카오맵) · 모집상태 CRUD, QueryDSL 동적 검색/정렬(최신순·인기순), 조회수(쿠키 기반 중복 방지) |
| **댓글** | 부모-자식 대댓글 구조, 소프트 삭제(`orphanRemoval`을 활용한 계단식 정리) |
| **관심글** | 게시글 북마크 등록/삭제, 마이페이지에서 내가 쓴 글·찜한 글 조회 |
| **모더레이션** | 관리자 게시글 삭제(내용 마스킹), 회원 검색/조회, 위반 이력 기반 자동 블랙리스트(3회 위반 시 영구 정지) |
| **인프라** | Redis 캐시(블랙리스트 조회 5분 캐시, SHA-256 이메일 해시 키), Actuator + Prometheus/Grafana 모니터링 |

## Tech Stack

**Language / Framework**
- Java 17, Spring Boot 3.2.1
- Spring Web, Spring Security, Spring Data JPA, Spring Validation, Spring Mail
- OAuth2 Client (Kakao / Naver 소셜 로그인)

**Data**
- MySQL 8 (운영), H2 — TCP server mode (로컬 개발)
- QueryDSL 5.0 — 동적 쿼리 (검색/정렬/페이징)
- Redis (Spring Data Redis) — 블랙리스트 캐시

**인증/보안**
- JJWT 0.11.5 (JWT 발급/검증)
- BCryptPasswordEncoder, SHA-256(이메일 해싱)

**인프라 / DevOps**
- Docker, Docker Compose (로컬: H2+Redis / 운영: MySQL+Redis)
- GitHub Actions (`backend/.github/workflows/CICD.yml`)
- Prometheus + Grafana (메트릭 수집/대시보드)
- springdoc-openapi (Swagger UI)

**Test**
- JUnit 5, spring-security-test
- Jacoco (라인 커버리지 80% 이상 강제, `domain`/`global`/generated Q클래스 제외)

## Getting Started

### 1. 사전 준비

- JDK 17
- Docker / Docker Compose (H2·Redis·모니터링 스택 실행용)

### 2. 환경 변수 설정

`backend/.env.example`을 참고하여 `.env` 파일을 프로젝트 루트(`backend/`)에 생성합니다.

```bash
cp .env.example .env
```

```dotenv
# Kakao OAuth2
KAKAO_CLIENT_ID=발급받은_카카오_REST_API_키
KAKAO_CLIENT_SECRET=카카오_클라이언트_시크릿
KAKAO_REDIRECT_URI=http://localhost:8000/login/oauth2/code/kakao
KAKAO_REDIRECT_URI_DEV=개발서버용_리다이렉트_URI

# Naver OAuth2
NAVER_CLIENT_ID=발급받은_네이버_클라이언트_ID
NAVER_CLIENT_SECRET=네이버_클라이언트_시크릿
NAVER_REDIRECT_URI=http://localhost:8000/login/oauth2/code/naver
NAVER_REDIRECT_URI_DEV=개발서버용_리다이렉트_URI

# JWT
JWT_SECRET=최소_256bit_이상의_임의_문자열

# 이메일 인증 (SMTP, Gmail 기준)
MAIL_USERNAME=발신용_지메일_주소
MAIL_PASSWORD=Gmail_앱_비밀번호
```

> 소셜 로그인 키는 각 [Kakao Developers](https://developers.kakao.com) / [Naver Developers](https://developers.naver.com) 콘솔에서 발급받고, 리다이렉트 URI는 콘솔에 등록된 값과 일치해야 합니다.

### 3. 로컬 인프라 실행 (H2 + Redis + 모니터링)

```bash
docker-compose up -d
```

- H2 TCP: `1521` (DB 연결) / `8081` (웹 콘솔)
- Redis: `6380` → 컨테이너 내부 `6379`
- Prometheus: `9090`, Grafana: `3000` (초기 관리자 비밀번호 `admin`)

`docker-compose.yml`은 앱 컨테이너(`Dockerfile-dev`)도 함께 띄웁니다. **애플리케이션만 로컬에서 직접 실행**하려면 `springboot` 서비스를 제외하고 `db`/`redis`만 올려도 됩니다:

```bash
docker-compose up -d db redis
```

### 4. 서버 실행

```bash
./gradlew bootRun          # macOS/Linux, Git Bash
gradlew.bat bootRun        # Windows(cmd/PowerShell)
```

기본 활성 프로필은 `local`이며, 기동 시 `data.sql`이 매번 재실행되어 초기 데이터를 시딩합니다(`ddl-auto: create-drop`). 로컬 서버 포트는 `8000`입니다 → `http://localhost:8000`.

**초기 계정**

| 구분 | 이메일 | 비밀번호 |
| --- | --- | --- |
| 관리자 | `admin@naver.com` | `Y@3r9o$7aaak` |
| 일반 사용자 | `kimSky@naver.com` | `Y@3r9o$7k` |

### 5. 빌드

```bash
./gradlew build                 # 컴파일 + 테스트 + jar 빌드
./gradlew clean bootJar -x test # CI가 배포용 jar를 만들 때 쓰는 명령 (테스트 생략)
```

## API 문서

애플리케이션 기동 후 Swagger UI에서 전체 스펙을 확인할 수 있습니다.

```
http://localhost:8000/swagger-ui/index.html
```

### 주요 엔드포인트

| 경로 | 설명 | 접근 |
| --- | --- | --- |
| `POST /api/v1/auth/sign` | 회원가입 | 공개 |
| `POST /api/v1/auth/login` | 로그인 (Access/Refresh 발급) | 공개 |
| `POST /api/v1/auth/service-logout` | 로그아웃 | 인증 필요 |
| `POST /api/renew-token` | Access Token 재발급 | 공개* |
| `POST /api/v1/auth/email/send-code` | 이메일 인증코드 발송 | 공개 |
| `POST /api/v1/auth/email/verify-code` | 이메일 인증코드 검증 | 공개 |
| `GET /api/v1/` | 메인 게시글 목록 (카테고리/정렬/온·오프라인 필터, 페이징) | 공개 |
| `GET /api/v1/board/{boardId}` | 게시글 상세 조회 | 선택적 인증 |
| `POST /api/v1/board/{boardId}` | 조회수 증가 처리(쿠키 기반 중복 방지) | 선택적 인증 |
| `POST /api/v1/board/member` | 게시글 등록 | 인증 필요 |
| `PATCH /api/v1/board/member` | 게시글 수정 | 인증 필요 |
| `PATCH /api/v1/board/member/recruit/{boardId}` | 모집 상태 변경 | 인증 필요 |
| `DELETE /api/v1/board/member/{boardId}` | 게시글 삭제 | 인증 필요 |
| `GET /api/v1/reply/view/{boardId}` | 댓글 목록 조회 | 선택적 인증 |
| `POST\|PATCH /api/v1/reply`, `DELETE /api/v1/reply/{rno}` | 댓글 등록/수정/삭제 | 인증 필요 |
| `GET /api/v1/view/post-like/{boardId}` | 관심글 여부 조회 | 인증 필요 |
| `POST /api/v1/post-like/{boardId}`, `DELETE /api/v1/post-like/{postLikeId}` | 관심글 등록/삭제 | 인증 필요 |
| `GET\|PATCH /api/v1/user/info` | 내 정보 조회/수정 | 인증 필요 |
| `GET /api/v1/user/lists`, `GET /api/v1/user/post-likes` | 내가 쓴 글 / 찜한 글 목록 | 인증 필요 |
| `GET /api/v1/admin/user-all`, `GET /api/v1/admin/dash-board` | 회원 전체 조회 / 관리자 대시보드 | 인증 필요 (ROLE_ADMIN) |
| `DELETE /api/v1/admin/board/{boardId}` | 게시글 강제 삭제 | 인증 필요 (ROLE_ADMIN) |
| `GET\|POST\|PATCH\|DELETE /api/v1/back-list/admin/**` | 블랙리스트 등록/해제/영구정지/조회 | 인증 필요 (ROLE_ADMIN) |
| `GET /api/v1/back-list-history/admin/**` | 제재 이력 전체/개별 조회 | 인증 필요 (ROLE_ADMIN) |
| `GET /api/v1/back-list-history/me` | 내 제재 이력 조회 | 인증 필요 |

\* `/api/renew-token`은 만료된 Access Token을 전제로 하는 재발급 전용 엔드포인트라 `JwtFilter`가 토큰 검증 자체를 건너뛰고 재발급 로직에 위임합니다 — 실제로는 Access/Refresh Token 쌍이 있어야 정상 동작합니다.

> 전체 요청·응답 스키마는 Swagger UI 기준이 최신입니다. `인증 필요` 엔드포인트는 `Authorization: Bearer {AccessToken}` 헤더에 로그인/재발급 API로 받은 Access Token을 사용하세요.

### 모니터링

- Actuator: `http://localhost:8000/actuator` (`health,info,metrics,env,loggers,prometheus` 노출)

## 테스트

```bash
./gradlew test
```

- 특정 클래스만 실행: `./gradlew test --tests "com.study.studyproject.board.service.BoardServiceTest"`
- 특정 메서드만 실행: `./gradlew test --tests "com.study.studyproject.board.service.BoardServiceTest.methodName"`
- 테스트는 `src/main`의 도메인 구조를 그대로 미러링합니다 (`<domain>/{controller,service,repository}`).
- 테스트 실행 후 `jacocoTestReport`가 자동으로 뒤따라 실행되며, 커버리지 리포트는 `build/reports/jacoco`에 생성됩니다.

```bash
./gradlew jacocoTestCoverageVerification
```

라인 커버리지 최소 80%를 강제합니다(`InitData*`, `*Application`, `domain.**`, `global.**`, QueryDSL Q클래스 제외).

### 현재 커버리지 현황 (2026-10-16 기준, 직접 실행 후 검증)

- 테스트 **166개** 전부 통과 (`BUILD SUCCESSFUL`) — 서비스 계층의 미검증 분기(조회/예외/영구정지/권한 등)를 보강하는 테스트 21개를 추가
- 전체 라인 커버리지 **88.0%** (`covered=1009 / total=1146`) — `build/reports/jacoco/test/jacocoTestReport.xml` 기준 (이전 76.0%에서 상승)
- `./gradlew jacocoTestCoverageVerification`(클래스 단위 80% 강제)은 아직 FAIL — 이 게이트는 전체 평균이 아니라 **클래스 1개 1개가 각각 80% 이상**이어야 통과함. 현재 미달 클래스:
  - 컨트롤러 6개: `BoardController`, `MemberController`, `AdminController`, `PostLikeController`, `BlackListController`, `BlackListHistoryController`, `EmailVerificationController` — MockMvc 테스트 부재
  - DTO 5개: `LoginRequest`, `BlacklistResponseDto`, `BlacklistHistoryMemberResponseDto`, `BlacklistHistoryAdminResponseDto`, `MemberReplyRequestDto`, `EmailSendCodeRequest`
  - `EmailSender`(10%) — 아래 WIP 테스트가 전제하는 `AlertService`가 아직 구현되지 않아 구조적으로 못 채움
  - `BlackListHistoryRepositoryImpl`(75%) — 거의 근접
- 미반영: `EmailSenderTest`, `BlacklistCacheHitRateBenchmarkTest` (커밋되지 않은 WIP 테스트 2개, 아직 구현 안 된 `AlertService`/캐시 히트율 카운터를 전제로 작성되어 현재는 컴파일되지 않아 집계에서 제외)

> 참고: 이 수치는 "테스트 90여 개 작성, 커버리지 93% 확보"라는 외부 보고와 일치하지 않습니다. 실측 기준으로는 테스트 166개(미반영 WIP 2개 제외), 전체 라인 커버리지 88.0%이며, 프로젝트에 설정된 클래스당 80% 게이트는 컨트롤러/DTO 테스트 보강이 더 필요해 아직 FAIL 상태입니다.

## 커밋 컨벤션

- `feat` 새로운 기능 추가, 요구사항 변경에 따른 기존 기능 변경
- `fix` 버그 수정
- `docs` 문서 추가/수정
- `style` 코드 포매팅 등 스타일 수정
- `refactor` 기능 변경 없는 프로덕션 코드 리팩토링
- `test` 테스트 코드 추가/리팩토링
- `chore` 빌드, 배포 등 기타 작업
- `rename` 파일명 변경
- `remove` 파일 삭제
- `revert` 작업 되돌리기

## Member

|           Backend                    |                        Frontend                    |
| :------------------------------------------: | :------------------------------------------------: |
|  [jacomyou0121](https://github.com/jacomyou0121)  |  [blkaka66](https://github.com/blkaka66)  |
