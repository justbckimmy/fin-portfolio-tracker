# Fin Portfolio Tracker

개인 투자 종목과 매수/매도 거래 내역을 관리하고, 거래 데이터를 기반으로 현재
보유 종목·보유 수량·평균 매수가·총 매수금액·포트폴리오 요약을 계산해주는
백엔드 API 서버입니다.

## 1. 프로젝트 소개

Fin Portfolio Tracker는 매수/매도
거래를 하나씩 기록하기만 하면, 그 기록을 바탕으로 종목별 보유 현황과 전체
포트폴리오 요약을 자동으로 계산해주는 개인용 주식 가계부 API 서버입니다.

## 2. 개발 목적

- Spring Boot 기반 REST API 설계 및 구현 경험을 쌓기 위해
- 여러 도메인(종목, 거래, 보유, 포트폴리오)이 서로 의존하는 구조를
  계층별(Controller-Service-Repository)로 분리해보기 위해
- 단순 CRUD를 넘어, 저장된 데이터가 아니라 **저장된 데이터로부터 계산된 값**을
  API로 제공하는 로직을 직접 설계해보기 위해
- 예외 상황(존재하지 않는 종목 조회 등)을 전역 예외 처리기로 일관되게
  다뤄보기 위해

## 3. 사용 기술

| 구분 | 기술 |
|---|---|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot |
| 웹 | Spring Web |
| 데이터 접근 | Spring Data JPA |
| 데이터베이스 | H2 Database (인메모리) |
| 빌드 도구 | Gradle |

## 4. 주요 기능

- **종목 등록/조회**: 관리하고 싶은 주식 종목(티커, 종목명, 시장)을 등록하고
  목록을 조회합니다.
- **거래 등록/조회**: 특정 종목에 대한 매수(BUY)/매도(SELL) 거래를 수량, 가격,
  거래일과 함께 기록하고 전체 거래 내역을 조회합니다.
- **보유 종목 계산**: 지금까지의 모든 거래 내역을 바탕으로 종목별 보유 수량,
  평균 매수가, 총 매수금액을 계산해서 보여줍니다.
- **포트폴리오 요약**: 전체 보유 종목 수와 총 매수금액을 한눈에 요약해서
  보여줍니다.
- **일관된 에러 응답**: 잘못된 요청(존재하지 않는 종목, 유효하지 않은 거래
  유형 등)에 대해 통일된 형식의 에러 메시지를 반환합니다.

## 5. 패키지 구조

```
com.example.portfolio
├── stock       # 종목 등록 및 조회
├── trade       # 매수/매도 거래 등록 및 조회
├── holding     # 거래 내역을 기반으로 한 보유 종목 계산
├── portfolio   # 전체 포트폴리오 요약 계산
└── common      # 공통 에러 응답 처리 (GlobalExceptionHandler)
```

각 패키지는 도메인 단위로 분리되어 있고, 내부는 `Controller → Service →
Repository`의 3계층 구조를 따릅니다. `holding`, `portfolio` 패키지는 자체
Repository 없이 다른 도메인의 데이터를 조합해서 응답을 계산하는 역할만
담당합니다.

## 6. API 명세

| Method | Endpoint | 설명 | Request Body | 비고 |
|---|---|---|---|---|
| POST | `/stocks` | 종목 등록 | `{ ticker, name, market }` | 등록된 `Stock` 객체 반환 |
| GET | `/stocks` | 종목 목록 조회 | - | 등록된 전체 종목 목록 반환 |
| POST | `/trades` | 거래(매수/매도) 등록 | `{ stockId, type, quantity, price, tradeDate }` | `type`은 `BUY` 또는 `SELL`, `tradeDate` 생략 시 오늘 날짜 |
| GET | `/trades` | 거래 목록 조회 | - | 등록된 전체 거래 내역 반환 |
| GET | `/holdings` | 보유 종목 조회 | - | 종목별 보유 수량 / 평균 매수가 / 총 매수금액 반환 |
| GET | `/portfolio/summary` | 포트폴리오 요약 조회 | - | 총 보유 종목 수 / 총 매수금액 반환 |

## 7. 핵심 로직 설명

### 보유 종목 계산 (평단가 방식)

`HoldingService`는 DB에 저장된 `Trade` 목록을 순서대로 순회하면서 종목별
보유 수량과 총 매수금액을 다음 규칙으로 누적 계산합니다.

- **BUY 거래**: 보유 수량과 총 매수금액을 거래 수량/금액만큼 그대로
  증가시킵니다.
- **SELL 거래**: 매도 시점까지의 **평균 매수가**를 기준으로 "판 만큼의
  매수원가"를 계산해서, 보유 수량과 총 매수금액을 함께 감소시킵니다. 즉
  매도는 실현 손익을 반영하는 게 아니라, 남은 보유 물량의 평균 매수가를
  그대로 유지하는 방식으로 계산됩니다.

### Holding은 저장되지 않는 계산 결과

`Holding`은 별도의 `@Entity`나 DB 테이블이 아닙니다. `HoldingService`가 매
요청마다 `TradeRepository`에서 전체 거래 내역을 읽어와 그 자리에서 계산한
**응답 전용 DTO(`HoldingResponse`)**입니다. 거래 내역만 있으면 언제든 같은
결과를 재현할 수 있기 때문에, 보유 현황을 별도로 저장하고 매 거래마다
동기화할 필요가 없습니다.

### 포트폴리오 요약은 Holding 결과를 재사용

`PortfolioService`는 자체적으로 거래를 다시 계산하지 않고, `HoldingService`가
만든 보유 종목 목록을 그대로 받아서 종목 수를 세고 총 매수금액을 합산합니다.
같은 계산 로직이 여러 곳에 중복되지 않도록 상위 도메인이 하위 도메인의
결과를 재사용하는 구조입니다.

### 예외 처리

`TradeService`는 거래를 저장하기 전에 종목 존재 여부, 거래 유형, 수량, 가격을
검증합니다. 특히 **존재하지 않는 `stockId`로 거래를 등록하려고 하면**
`IllegalArgumentException`을 던지고, `GlobalExceptionHandler`가 이를
`400 Bad Request`와 함께 통일된 형식의 `ErrorResponse`로 변환해서 응답합니다.

## 8. 실행 방법

### 사전 준비물

- JDK 17 이상

### 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd "Fin Portfolio Tracker"

# macOS / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

서버가 정상적으로 실행되면 `http://localhost:8080` 에서 API를 호출할 수
있습니다. 데이터베이스는 인메모리 H2를 사용하므로, 별도의 DB 설치 없이 바로
실행 가능하며 애플리케이션을 재시작하면 데이터는 초기화됩니다.

### 테스트 실행

```bash
./gradlew test
```

## 9. curl 테스트 예시

아래 순서대로 요청을 보내면 "종목 등록 → 매수 → 매도 → 보유 현황 확인 →
포트폴리오 요약 확인"까지 전체 흐름을 확인할 수 있습니다.

### 1) 종목 등록

```bash
curl -X POST http://localhost:8080/stocks \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "005930",
    "name": "삼성전자",
    "market": "KOSPI"
  }'
```

```json
{
  "id": 1,
  "ticker": "005930",
  "name": "삼성전자",
  "market": "KOSPI"
}
```

### 2) 거래 등록 — 매수(BUY)

```bash
curl -X POST http://localhost:8080/trades \
  -H "Content-Type: application/json" \
  -d '{
    "stockId": 1,
    "type": "BUY",
    "quantity": 10,
    "price": 71500,
    "tradeDate": "2026-06-10"
  }'
```

```json
{
  "id": 1,
  "stock": { "id": 1, "ticker": "005930", "name": "삼성전자", "market": "KOSPI" },
  "type": "BUY",
  "quantity": 10,
  "price": 71500,
  "tradeDate": "2026-06-10"
}
```

### 3) 거래 등록 — 매도(SELL)

```bash
curl -X POST http://localhost:8080/trades \
  -H "Content-Type: application/json" \
  -d '{
    "stockId": 1,
    "type": "SELL",
    "quantity": 4,
    "price": 73000,
    "tradeDate": "2026-06-20"
  }'
```

```json
{
  "id": 2,
  "stock": { "id": 1, "ticker": "005930", "name": "삼성전자", "market": "KOSPI" },
  "type": "SELL",
  "quantity": 4,
  "price": 73000,
  "tradeDate": "2026-06-20"
}
```

### 4) 보유 종목 조회

```bash
curl http://localhost:8080/holdings
```

```json
[
  {
    "ticker": "005930",
    "stockName": "삼성전자",
    "quantity": 6,
    "averageBuyPrice": 71500.00,
    "totalBuyAmount": 429000.00
  }
]
```

### 5) 포트폴리오 요약 조회

```bash
curl http://localhost:8080/portfolio/summary
```

```json
{
  "totalHoldingCount": 1,
  "totalBuyAmount": 429000.00
}
```

### 참고: 잘못된 요청 예시

```bash
curl -X POST http://localhost:8080/trades \
  -H "Content-Type: application/json" \
  -d '{
    "stockId": 999,
    "type": "BUY",
    "quantity": 1,
    "price": 1000
  }'
```

```json
{
  "message": "존재하지 않는 종목입니다."
}
```

위 요청은 `HTTP 400 Bad Request`와 함께 반환됩니다.

## 10. 에러 처리 방식

모든 잘못된 요청은 `common` 패키지의 `GlobalExceptionHandler`가 한 곳에서
처리합니다.

- `IllegalArgumentException`이 발생하면 `@RestControllerAdvice`가 이를 가로채
  `HTTP 400 Bad Request`로 변환합니다.
- 응답 본문은 항상 `{ "message": "..." }` 형태의 `ErrorResponse`로 통일되어,
  API를 호출하는 쪽에서 에러 형식을 예측하기 쉽습니다.
- 현재 검증되는 대표적인 케이스는 다음과 같습니다.
  - 존재하지 않는 `stockId`로 거래를 등록하는 경우
  - 거래 유형이 `BUY` / `SELL`이 아닌 경우
  - 거래 수량이 1 미만이거나 가격이 0 이하인 경우

## 11. 향후 개선 계획

- `@Valid` + 검증 어노테이션을 활용한 요청 값 검증으로 전환
- 거래 수정/삭제 기능 추가
- 실시간(또는 준실시간) 시세를 연동해 평가금액·수익률 계산 기능 추가
- 인증/인가를 도입해 여러 사용자를 지원하는 구조로 확장
- H2 인메모리 대신 파일 기반 또는 별도 DB로 전환해 데이터 영속성 확보
