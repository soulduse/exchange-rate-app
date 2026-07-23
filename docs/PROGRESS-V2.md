# 환율알리미 v2 진행 상황 (진실 원천)

> 계획: `PLAN-V2.md` / 배경 분석: `ANALYSIS-2026-07-22.md`

## 완료

### M1+M2 — 서버 (apis-py `exchange` 도메인) ✅ 2026-07-22
- `src/apis_py/domains/exchange/`: models(4테이블)·naver(수집 클라)·service(보드/4종가/백필/52주/알림평가/브리핑/급변동)·dispatch(FCM)·router(공개 API 8종)·schemas
- 스케줄러 잡 4종 (`exchange_jobs.py`, exchange_ 그룹): collect_board(2분)·collect_details(고시시간 10분)·backfill_daily(03:40)·briefing_morning(평일 07:50)
- 알림 평가: armed 재무장 + ONCE/REPEAT + 30분 쿨다운. 급변동=주요4통화 ±1% 토픽 푸시(일1회 dedup)
- FCM: **SECOND_PROJECT(promotions-ebe8e) 재사용** (신규 크리덴셜 없음, 사용자 확정). 앱 등록 완료 `1:803103030735:android:7006469c0520a8a57666ba`
- 코드리뷰 반영: 공개 쓰기 EP 레이트리밋(rate_limiter, device당 분당 10회 fail-open)·FiredAlert에 fcm_token 동봉(N+1 회피)·collect_details/backfill 단일 세션
- 테스트: tests/exchange 16개 + 전체 3,286개 통과 (잡 수 원장 181→185 갱신)
- 데이터 소스 실검증: 벌크 `api.stock.naver.com/marketindex/exchange`(1콜 58통화), prices pageSize≤60, 52주 밴드는 daily 자체 계산

### 서버 오픈 게이트 ✅ 2026-07-22~23 (프로덕션 개통)
- 커밋 14389e6 배포(⚠️compose up 시 `IMAGE=<SHA>` env 필수 — 누락하면 stale latest로 뜨는 실사고 재확인)
- DDL 4테이블 프로덕션 적용 완료, 박스 `.env` line 117 `SCHEDULER_ENABLED_GROUPS`에 `exchange` 추가(line 12는 빈 데코이)
- 수집 가동: 58통화 2분 주기, `/api/v1/exchange/rates` 프로덕션 응답 검증
- 백필+4종가+52주 시드 완료: USD 1y 히스토리 200행+, 4종가/52주 밴드 채워짐
- 후속 lint/type 정리: f3e5ccd(ruff E501·SIM103·I001), 83f226e(mypy — trade_date `Mapped[date]`·dict 제네릭)

### M3 — 앱 스캐폴드 (Compose 전면 리라이트) ✅ 빌드 성공 2026-07-22
- 툴체인: Gradle 8.13 / AGP 8.13.2 / Kotlin 2.3.20 / Hilt 2.57(KSP) / Room 2.8.4 / compose-bom 2026.03 / kotlinx.serialization (증권모아 미러링). minSdk 24 / target 36 / vc16 / 2.0.0
- 구성: 홈(워치리스트+통화선택+당겨새로고침+스테일 안내) / 상세(시세·기간차트 Canvas·4종가·우대율 실효가·52주 밴드·알림 생성) / 계산기(KRW+멀티 통화 동시 환산, 행 탭=기준 전환, 상태 저장) / 알림센터(CRUD+토글+POST_NOTIFICATIONS) / 설정(브리핑·급변동 토픽 토글, 테마 선택, 피드백 API 연동) / FCM 수신+디바이스 등록
- 기존 Java/Realm/JSoup 코드 전량 삭제. google-services.json = promotions-ebe8e (구버전 `.v1-backup`, gitignore)

### M4 — Glance 위젯 + 광고 ✅ 2026-07-23 에뮬 검증
- Glance responsive 위젯: SMALL(1통화 1행)·LARGE(4통화 리스트, 국기+가격+등락색) 실데이터 렌더 확인, WorkManager 30분 갱신
- 광고: UMP fail-open + 하단 배너(전 탭) + 상세 진입 전면(4회당 1회·60s 간격) — 테스트 광고 노출 확인
- 다크 테마: 팔레트·카드 대비 정상. **인앱 테마 오버라이드 시 상태바 아이콘 대비 수정**(MainActivity `DisposableEffect`+`SystemBarStyle.auto`)

### M6 일부 — 에뮬 QA 1차 ✅ 2026-07-23
- 홈(라이트/다크)·상세(3M/1Y 차트, 4종가, 우대율 슬라이더, 52주 밴드)·계산기(소수점 입력, 기준 전환, 교차환산 검산 일치)·알림(권한→2단계 생성 다이얼로그→목록)·설정 전부 실데이터 검증
- 알림 서버 발화 확인: `alerts_fired: 1` (USD 이하 조건)

### FCM 실발송 가드 이슈 → 스코프 한정 해제 (진행 중 2026-07-23)
- **원인**: 박스 `.env`의 `FCM_FORCE_TEST_TOPIC=apis-py-staging-test`(이관기 fail-closed 방침)가 스케줄러발 토큰 FCM 전부 드롭 → 앱에 미도달
- **해법**: 전역 해제는 D3 배치 2(푸시 계열)의 몫이므로, `FCM_GUARD_ALLOW_TYPES`(data.type 화이트리스트) 예외를 추가해 exchange 계열만 개방 — 커밋 8b4684d
- 남은 절차: CI 이미지 → 박스 `.env`에 `FCM_GUARD_ALLOW_TYPES=EXCHANGE_RATE_ALERT,EXCHANGE_BRIEFING,EXCHANGE_SWING` 추가(백업 후) → scheduler `IMAGE=<SHA>` 재기동 → 새 알림 생성 → FCM 실수신 확인

## 다음
1. FCM E2E 마무리 (위 절차)
2. 앱 저장소 커밋 (전면 리라이트 — 코드리뷰 에이전트 결과 반영 후. `.gitignore`에 release 산출물/백업 제외 추가함)
3. M5 잔여: 앱 아이콘/스플래시 리프레시, 릴리스 서명(exchange-rate-app.jks 비번 미확인 — 무비번 지문 대조 레시피), R8 release 실기기 검증
4. M6 잔여: 스토어 자산(스크린샷·문구) → 심사 제출
5. 미결 결정(사용자): AdMob 계정 이관(현 제3 계정 `ca-app-pub-1908860913688060~...` 유지 중), 어드민 앱 등록(서버 광고설정 채택 여부 — v2.0은 클라 자체 게이팅)

## 메모
- 앱 API 베이스: `https://app-api.oror.link` (oror.link는 py EP 404 — edge-router 화이트리스트)
- 런처 위젯 피커는 에뮬에서 ANR 잦음 — 앱 아이콘 long-press → Widgets 경로가 안정적. 배치는 `input draganddrop`
- Glance 위젯 previewImage 미설정(피커에 기본 아이콘) — 스토어 제출 전 previewImage 추가 검토
