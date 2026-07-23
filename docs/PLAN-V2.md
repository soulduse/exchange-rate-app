# 환율알리미 v2.0 전면 리뉴얼 계획 (2026-07-22 확정)

> 사용자 확정 결정: **Compose 네이티브 전면 리라이트**(기존 코드 전량 폐기 승인, 패키지명 `com.dave.soul.exchange_app`+서명키 `exchange-rate-app.jks` 유지) + **apis-py `exchange` 도메인 신설**(수집·캐시·알림엔진·FCM). v1.9 긴급 수리 생략, v2.0 직행.
> 배경 분석: `docs/ANALYSIS-2026-07-22.md` 참조.

## 0. 제품 방향

**포지셔닝**: "무가입 3초 환율 관측소" — 은행/핀테크(환전 체결)와 싸우지 않고, 그들이 못 하는 ①즉시 조회 ②소형/커스텀 위젯 ③전 통화 정밀 알림(서버 푸시) ④은행 중립 4종가·우대율 실효가로 승부.

**차별화 3축**:
1. **정밀 알림**: 목표가(이상/이하·1회/반복) + 급변동(±n%) + 아침 브리핑 — 전부 서버 평가+FCM 푸시(15분 WorkManager 한계 탈피)
2. **위젯 라인업**: 소형 2x1·리스트 4x2, 다크/라이트/투명도, 등락 색상 — 시장 공급 공백 확인됨
3. **환전 의사결정 정보**: 4종가(현찰/송금) + 우대율 반영 실효가 + 하나/신한 고시 비교 + 52주 밴드 차트

## 1. 시스템 아키텍처

```
[네이버 JSON(하나·신한 고시)] ←수집기(1~2분, 고시시간)─ [apis-py exchange 도메인]
[수출입은행 oapi(폴백)]                                    ├ rates 캐시(DB)
[frankfurter(최후 폴백)]                                   ├ REST API (app-api.oror.link/api/v1/exchange/*)
                                                           ├ alert 엔진(수집 직후 평가, dedup)
                                                           └ FCM 발송
[Android 앱 v2 (Compose)] ──REST──> 위 API
  ├ Room(시세 캐시·오프라인) + DataStore(설정)
  ├ FCM 수신(알림) + WorkManager(위젯 갱신·오프라인 폴백)
  └ Glance 위젯
```

## 2. 데이터 소스 (2026-07-22 curl 실검증 완료)

| 용도 | 엔드포인트 | 비고 |
|---|---|---|
| **전체 시세 벌크** | `GET https://api.stock.naver.com/marketindex/exchange` | **단일 GET으로 58통화 전체**: closePrice(매매기준율)·fluctuations·fluctuationsRatio·**highPriceOf52Weeks/lowPriceOf52Weeks**·unit(JPY=100)·marketStatus·localTradedAt·국가정보·로고 URL. normalList[59]+majorList[6] |
| 통화 상세(고시회차) | `GET …/marketindex/exchange/FX_{CCY}KRW` | degreeCount(고시회차), 신한은행=`FX_USDKRW_SHB` |
| **4종가+일별 히스토리** | `GET …/marketindex/exchange/FX_{CCY}KRW/prices?page=&pageSize=` | cashBuyValue/cashSellValue/sendValue/receiveValue + 일별 closePrice (차트 데이터) |
| 폴백 1 | 수출입은행 `oapi.koreaexim.go.kr` (⚠️2025-06 신도메인) | 일 1회 고시·일 1,000회·현찰가 없음·키 2년 자동파기 |
| 폴백 2 | `frankfurter.dev` / fawazahmed0 CDN | mid-market·최후 방어선·sanity check |

수집 전략: 벌크 1~2분 주기(고시시간 09:00~19:00 밀도↑, 야간/주말 완화) + 4종가는 통화별 10~30분 주기 or 온디맨드 캐시. 모든 응답에 `source`+`fetchedAt` 포함. 콤마 문자열 → Decimal 정규화. unit(100엔) 정보 보존.

## 3. 서버 — apis-py `exchange` 도메인

> 컨벤션은 feedback 도메인(최근 신규 도메인 정본)과 C-5 시세 알림 축(FCM+dedup 기존 인프라)을 따른다. DDL은 `deploy/ddl/` 수동 적용 규약.

### 3-1. API (모두 공개, 인증 없음 — 단 rate limit)
| EP | 설명 |
|---|---|
| `GET /api/v1/exchange/rates` | 전체 통화 최신 시세(매매기준율·등락·52주밴드·unit·고시회차·updatedAt·source). 앱 홈/위젯용 |
| `GET /api/v1/exchange/rates/{ccy}` | 단일 통화 상세: 4종가 + 하나/신한 비교 + 고시회차 |
| `GET /api/v1/exchange/rates/{ccy}/history?range=1w\|1m\|3m\|6m\|1y\|3y` | 일별 히스토리(차트). 서버 DB 적재분 + 네이버 prices 백필 |
| `POST /api/v1/exchange/devices` | FCM 토큰 등록/갱신 {deviceId, fcmToken, packageName} |
| `GET/POST/PATCH/DELETE /api/v1/exchange/alerts` | 알림 CRUD {deviceId, ccy, priceType(base/cashBuy/…), direction(≥/≤), targetPrice, repeat(1회/반복), active} |
| `PUT /api/v1/exchange/briefing` | 아침 브리핑 옵트인 {deviceId, enabled, hour} |

### 3-2. 잡 (기존 jobs 스케줄러에 등록)
1. `exchange_collect` — 벌크 수집→DB upsert(최신 스냅샷+일별 히스토리 적재). 실패 시 폴백 체인.
2. `exchange_alert_eval` — 수집 직후 활성 알림 평가→조건 충족 시 FCM(제목: "USD 1,480원 도달 ▼"). **dedup**: 알림당 재발송 억제(1회성=충족 시 비활성화, 반복=재교차 시에만). C-5 dedup TTL 패턴 재사용.
3. `exchange_briefing` — 옵트인 디바이스에 아침 요약 푸시(전일 대비·주간 추이·52주 위치).
4. `exchange_swing_alert` — 급변동(±n%, 기본 1%) 감지 푸시(옵트인).

### 3-3. 테이블 (DDL: `deploy/ddl/V2026xxxx__exchange_tables.sql`)
- `exchange_rate_snapshot` (ccy PK, 최신 시세 전체 필드, updated_at)
- `exchange_rate_daily` (ccy+date PK, close/cash4/52주, 네이버 prices 백필)
- `exchange_device` (device_id PK, fcm_token, package_name, briefing_enabled, briefing_hour)
- `exchange_alert` (id PK, device_id FK, ccy, price_type, direction, target_price, repeat, active, last_fired_at)

### 3-4. FCM — 기존 프로젝트 재사용 (2026-07-22 사용자 확정, 등록 완료)
- **서버 크리덴셜 추가 금지** — 기존 SECOND_PROJECT(`promotions-ebe8e`, firebase-promotions.json) 재사용. ⚠️FOURTH(`ai-apps-430ff`)는 앱 30개 한도 초과로 등록 불가(429 실측).
- v2 앱을 `promotions-ebe8e`에 **등록 완료**: App ID `1:803103030735:android:7006469c0520a8a57666ba`. 앱의 google-services.json 교체 완료(구버전은 `google-services.json.v1-backup`).
- 브리핑/급변동은 FCM 토픽(exchange_briefing/exchange_swing, 클라 직접 구독), 목표가 알림만 디바이스 토큰 타겟.

## 4. 앱 — Android v2.0 (전면 재작성)

### 4-1. 스택
- Kotlin 2.x + **Compose(Material 3) + MVVM + Hilt** (증권모아와 동일 계열), 버전 카탈로그(libs.versions.toml), Gradle KTS
- Retrofit + kotlinx.serialization / Room(시세 캐시·알림 로컬 사본) / DataStore(설정) / Coil(국기·로고) / **Glance(위젯)** / WorkManager(위젯 갱신·폴백) / FCM / Vico(차트)
- applicationId `com.dave.soul.exchange_app` 유지, versionCode 16+/versionName 2.0.0, minSdk 23→**24**, target 35, R8 on(⚠️직렬화 모델 keep — kotlinx.serialization 룰)
- 베이스 URL: `https://app-api.oror.link` (apis-py 도메인 — oror.link는 py EP 404 함정)

### 4-2. 화면
1. **홈(워치리스트)**: 선택 통화 리스트(매매기준율·등락·미니 스파크라인), 갱신시각+고시회차 헤더, 당겨서 새로고침, 통화 추가/정렬/삭제, 검색. 마지막 캐시 오프라인 표시("n분 전 기준").
2. **통화 상세**: 대형 시세+등락, **4종가 카드**(현찰 살/팔·송금 보낼/받을), **우대율 슬라이더 → 실효가**, 기간별 차트(1주~3년)+**52주 밴드**("현재 1년 중 상위 n%"), 하나/신한 고시 비교, [알림 만들기] 버튼.
3. **계산기**: XE 스타일 멀티 통화 동시 변환(금액 1개 입력→선택 통화 전체 변환), 오프라인 동작, 마지막 상태 저장, JPY 100엔 단위 명시.
4. **알림 센터**: 목표가 알림 목록(활성/이력), 생성/수정(통화·기준가 종류·이상/이하·1회/반복), 급변동·아침 브리핑 토글. 소수점 입력 정상 지원(기존 버그 해소).
5. **설정**: 알림음/진동, 테마(라이트/다크/시스템), 시작 화면, 언어(v2는 ko 우선, 리소스는 전부 strings로), 피드백/버그 제보(**공통 피드백 API** `POST /api/v1/feedback`, X-Package-Name), 오픈소스 라이선스, 광고 제거(후속).

### 4-3. 위젯 (Glance) — 핵심 차별화
- **2x1 소형**: 통화 1개(선택 가능), 시세+등락(빨강/파랑), 갱신시각. 탭→앱 상세.
- **4x2 리스트**: 통화 3~5개, 등락 색상.
- 옵션: 다크/라이트/투명도, 통화 선택(교차환율 후속). 갱신: WorkManager 15분 + 앱 포그라운드 갱신 시 동기화 + 수동 갱신 버튼.

### 4-4. 알림 파이프라인
- 주 경로: 서버 평가 → FCM(data 메시지) → 로컬 알림 표시.
- 보조: FCM 불가/오프라인 대비 WorkManager 주기 체크(서버 rates 폴링) — 단순 폴백.
- POST_NOTIFICATIONS 런타임 권한 온보딩.

### 4-5. 수익화/정책
- AdMob: 하단 배너(홈) + 전면(상세→복귀 N회 게이팅, 첫 세션 면제) — **광고 밀도는 리뷰 불만 해소 수준으로 보수적으로**.
- **UMP(GDPR) 동의 흐름 필수 추가** (현재 전무 — 정책 리스크).
- 서버 광고설정(`GET /api/v4/app?packageName=`, adsStatus 6불리언) 채택 — 어드민 앱 등록 필요. 실패 시 캐시→기본값 fail-open(⚠️UMP 미구성=광고 전멸 사고 방지).
- ⚠️ AdMob 계정: 현 `ca-app-pub-1908860913688060`은 khy/khw 아닌 제3(구) 계정 — **이관/유지 결정 필요(미결)**.
- 광고 제거 IAP/구독은 v2.1+ (appsub 재사용 후보).

### 4-6. 데이터 이관
- Realm 알람 이관 **생략** 결정 근거: v1.8.4에서 schemaVersion bump+deleteRealmIfMigrationNeeded로 이미 전체 wipe 전력, realm-java 동반 비용 큼. 대신 v2 첫 실행 온보딩에서 알림 재설정 유도 + 구버전 알림 소멸 안내.

## 5. 마일스톤

| M | 내용 | 산출물 |
|---|---|---|
| **M1** | 서버: exchange 도메인(수집기+rates/history API+DDL+테스트) | app-api.oror.link/api/v1/exchange/rates 응답 |
| **M2** | 서버: devices/alerts API + alert 엔진 + FCM 발송 + 브리핑/급변동 잡 | 실디바이스 푸시 수신 |
| **M3** | 앱: 신규 코드베이스 골격(Gradle KTS·Hilt·네트워크·Room) + 홈 + 상세(4종가·차트) + 계산기 | 에뮬 실행 |
| **M4** | 앱: 알림 센터(FCM 배선) + 위젯(Glance 2종) + 설정 + 피드백 | E2E: 알림 생성→푸시 수신 |
| **M5** | 광고(UMP+배너+전면) + 서버 광고설정 + 아이콘/스플래시 리프레시 + R8 릴리스 검증 | release APK 실기기 스모크 |
| **M6** | QA 루프 + 스토어 자산(스크린샷·문구) + 심사 제출 | Play 제출 |

## 6. 리스크/미결
- 네이버 비공식 API 변경/차단 → 서버 흡수 구조로 완화, 폴백 체인. 수집기 실패 알람(Slack).
- AdMob 계정 이관(제3 계정) — 사용자 결정 필요.
- FCM용 Firebase 프로젝트: 기존 exchange 앱 Firebase 유지 여부(서비스 계정 키를 apis-py에 추가).
- 고시회차 수집 주기 vs 네이버 부하 — 1~2분 벌크 1콜은 안전권으로 판단, 문제 시 완화.
- minSdk 23→24 상향(구형 기기 업데이트 중단) — 영향 미미로 판단.
