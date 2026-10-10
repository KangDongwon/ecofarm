# 🥦 ECOFARM — 아파트 공동체 텃밭 직거래 플랫폼

아파트 단지 안의 텃밭에서 주민(판매자)이 기른 채소를 이웃(소비자)에게 직접 파는 웹 서비스입니다.
관리자는 아파트 계약, 회원 승인, 판매 물품 승인, 주문 처리를 한 곳에서 관리하고,
**수요 분석·고객만족도 분석 대시보드**와 **NAVER Cloud AiTEMS 기반 AI 추천**으로 어떤 작물을 기를지 결정합니다.

- **개발 기간**: 2024.09 ~ 2024.10
- **개발 인원**: 1명

<br />

## 🛠 기술 스택

| 구분 | 기술 |
| --- | --- |
| **Backend** | Java 17, Spring Boot 3.3, Spring Data JPA, Spring Security, JWT (jjwt 0.11), Lombok, OpenCSV |
| **Frontend** | Vue 3, Vite 5, Vue Router 4, Vuex 4, Chart.js 4 + vue-chartjs, chartjs-plugin-datalabels |
| **Database** | MySQL 8 |
| **AI / 외부 API** | NAVER Cloud Platform AiTEMS (개인화·연관·인기 아이템 추천) |
| **Tools** | IntelliJ IDEA, VS Code, Gradle, Yarn, Git |

<br />

## 🏗 시스템 구조

```
┌──────────────────────┐   REST (JSON) + JWT    ┌────────────────────────┐        ┌──────────┐
│  Vue 3 SPA (Vite)    │ ─────────────────────▶ │  Spring Boot API       │ ─JPA─▶ │  MySQL   │
│  localhost:5173      │ ◀───────────────────── │  localhost:8080        │        └──────────┘
│  Router 가드 / Vuex  │                         │  Security + JWT 필터   │
└──────────────────────┘                         │                        │  HMAC 서명  ┌──────────────┐
                                                 │  NaverAitemsController │ ──────────▶ │ NCP AiTEMS   │
                                                 └────────────────────────┘             └──────────────┘
```

<br />

## ✨ 주요 기능

### 사용자 역할 (3종)

| 역할 | 주요 기능 |
| --- | --- |
| **소비자** (`ROLE_CONSUMER`) | 판매 물품 조회·상세 보기(재배 영상 포함), 구매, 주문 내역 관리, 리뷰·별점 작성, 회원 정보 수정·탈퇴 |
| **판매자** (`ROLE_SELLER`) | 판매 물품 등록·수정, 받은 주문 관리, 판매 통계 차트, 판매자 정보 관리 (가입 후 관리자 승인 필요) |
| **관리자** (`ROLE_ADMIN`) | 사업·회원·물품·주문 관리, 데이터 분석 대시보드, AI 추천 |

### 관리자 대시보드

- **사업 관리**: 아파트 계약(계약 기간) 관리, 재배 물품 마스터 관리
- **회원 관리**: 소비자·판매자 현황 차트, 판매자 가입 승인
- **판매 물품 관리**: 판매 물품 현황(물품별·아파트별 차트), 판매 물품 승인/반려, 재고 현황
- **주문 관리**: 물품 전달 확인, 구매 현황, 진행 중인 주문
- **데이터 분석**
  - *수요 분석*: 월별 물품별·아파트별 생산/구매량, 주문 완료·취소 비율
  - *고객만족도 분석*: 판매자별·아파트별 평균 별점, 평균 별점이 유난히 낮은 판매자/구매자 파악

### 🤖 AI 추천 (NAVER AiTEMS)

관리자가 **물품군 · 연령대 · 관심 질환**을 고르면 백엔드가 AiTEMS의 여러 추천 모델을 불러 결과를 합칩니다.

| 조건 | AiTEMS 추천 유형 | 의미 |
| --- | --- | --- |
| 물품군 | `relatedItem` | 함께 구매되는 연관 작물 |
| 연령대 | `pop` | 연령대별 인기 작물 |
| 관심 질환 (복수 선택) | `pop` | 질환별(당뇨·고혈압 등) 인기 작물 |

여러 추천 목록은 **순위 기반 가중치**(목록 길이 − 순위)를 더해 하나로 합칩니다. 여러 조건에서 동시에 상위에 오른 작물이 최종 추천의 위쪽에 놓입니다.
학습 데이터(`user_data.csv`, `item_data.csv`, `interaction_data.csv`)는 백엔드에서 만든 테스트 주문 데이터를 CSV로 내보낸 것입니다.

### 🔐 인증 / 인가

- Spring Security + **JWT(HS512) Stateless 인증**. 로그인하면 역할(role)이 담긴 토큰을 발급합니다.
- `JwtAuthenticationFilter`로 요청마다 토큰을 검증하고, `@PreAuthorize`로 API별 역할 권한을 확인합니다.
- 프론트엔드는 Vue Router `beforeEach` 가드로 로그인 여부와 역할에 따라 페이지 접근을 막습니다.
- 비밀번호는 BCrypt로 해시해 저장합니다.

<br />

## 🗂 ERD (주요 엔티티)

```
Apartment 1 ─── N Seller 1 ─── N SaleProduct N ─── 1 Product
                                   │ 1
                                   │
Consumer 1 ─── N CustomerOrder N ──┘
    │ 1              │ 1
    └──── N Review N ┘        (Review는 SaleProduct와도 N:1)

Admin (독립)
```

| 엔티티 | 설명 |
| --- | --- |
| `Apartment` | 계약된 아파트 단지 (계약 시작일/종료일) |
| `Seller` | 아파트에 소속된 판매자 (승인일) |
| `Consumer` | 소비자 (나이, 관심 질환 → AI 추천에 활용) |
| `Product` | 재배 물품 마스터 (브로콜리, 케일 등) |
| `SaleProduct` | 판매자가 등록한 판매 물품 (가격, 수량, 판매 기한, 승인 상태, YouTube 링크) |
| `CustomerOrder` | 주문 (수량, 금액, 상태) |
| `Review` | 주문에 대한 별점·리뷰 |

<br />

## 📡 API 개요

| Prefix | 설명 |
| --- | --- |
| `POST /api/auth/login` | 로그인 및 JWT 발급 |
| `/api/consumers` | 소비자 가입·조회·수정·탈퇴 |
| `/api/sellers` | 판매자 가입·조회·수정, 승인 대기 목록·승인 |
| `/api/admins` | 관리자 계정 관리 |
| `/api/apartments` | 아파트 계약 CRUD |
| `/api/products` | 재배 물품 CRUD |
| `/api/saleproducts` | 판매 물품 등록·수정, 승인/반려, 판매자별 조회 |
| `/api/customerorders` | 구매, 소비자별/판매자별 주문 조회, 상태 변경 |
| `/api/reviews` | 리뷰 작성·조회 (소비자별, 판매자별) |
| `/api/aitems` | NAVER AiTEMS 추천 조회 (관리자) |

<br />

## 📁 프로젝트 구조

```
.
├── ecofarm_backend/                 # Spring Boot API 서버
│   └── src/main/java/com/ecofarm/demo
│       ├── auth/                   # 로그인, JWT 발급/검증 필터
│       ├── config/                 # Spring Security, CORS 설정
│       ├── controller/             # REST 컨트롤러 (AiTEMS 연동 포함)
│       ├── service/                # 비즈니스 로직, 테스트 데이터 생성(DataInitializer)
│       ├── repository/             # Spring Data JPA
│       ├── entity/                 # JPA 엔티티
│       └── dto/
├── ecofarm_frontend/                # Vue 3 + Vite SPA
│   └── src
│       ├── components/             # 페이지 및 차트 컴포넌트
│       ├── router/                 # 라우팅 + 역할 기반 가드
│       └── store/                  # Vuex (토큰, 사용자 역할)
├── user_data.csv                   # AiTEMS 학습용 사용자 데이터 (나이, 질환)
├── item_data.csv                   # AiTEMS 학습용 아이템 데이터
└── interaction_data.csv            # AiTEMS 학습용 구매 이력
```

<br />

## 🚀 실행 방법

### 1. 데이터베이스

```sql
CREATE DATABASE ecofarmDB DEFAULT CHARACTER SET utf8mb4;
```

### 2. 환경변수

민감한 값은 [`application.yml`](ecofarm_backend/src/main/resources/application.yml)에서 환경변수로 읽습니다. 값이 없으면 괄호 안의 로컬 기본값을 씁니다.

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL 계정 | `root` / `1234` |
| `JWT_SECRET` | JWT 서명 키 (64바이트 이상) | 로컬 개발용 값 |
| `NCP_ACCESS_KEY` / `NCP_SECRET_KEY` | NAVER Cloud AiTEMS 인증키 | 없음 (AI 추천 기능에만 필요) |

### 3. 백엔드

```bash
cd ecofarm_backend
export NCP_ACCESS_KEY=... NCP_SECRET_KEY=...   # AI 추천을 쓸 때만
./gradlew bootRun        # http://localhost:8080
```

> 서버가 시작될 때 `DataInitializer`가 테스트 데이터를 자동으로 만듭니다.
> (아파트 10곳, 판매자 150명, 소비자 200명, 판매 물품 1,000건, 주문 10,000건, 리뷰)

### 4. 프론트엔드

```bash
cd ecofarm_frontend
yarn install
yarn dev                 # http://localhost:5173
```

### 테스트 계정

| 역할 | 이메일 | 비밀번호 |
| --- | --- | --- |
| 관리자 | `admin@test.com` | `1111` |
| 판매자 | `seller1@test.com` ~ `seller145@test.com` (승인됨) | `1111` |
| 소비자 | `consumer1@test.com` ~ `consumer200@test.com` | `1111` |

> `seller146` ~ `seller150`은 승인 대기 상태라, 관리자의 판매자 승인 기능을 시험해 볼 때 씁니다.
