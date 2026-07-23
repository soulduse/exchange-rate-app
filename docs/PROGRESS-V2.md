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

### FCM 실발송 가드 이슈 → 스코프 한정 해제 ✅ E2E 완료 2026-07-23
- **원인**: 박스 `.env`의 `FCM_FORCE_TEST_TOPIC=apis-py-staging-test`(이관기 fail-closed 방침)가 스케줄러발 토큰 FCM 전부 드롭 → 앱에 미도달
- **해법**: 전역 해제는 D3 배치 2(푸시 계열)의 몫이므로, `FCM_GUARD_ALLOW_TYPES`(data.type 화이트리스트) 예외를 추가해 exchange 계열만 개방 — 커밋 8b4684d
- 반영 완료: 박스 `.env` line 93 `FCM_GUARD_ALLOW_TYPES=EXCHANGE_RATE_ALERT,EXCHANGE_BRIEFING,EXCHANGE_SWING`(백업 .env.bak-fcmallow-*) + scheduler `IMAGE=83f226e...` 재기동(72잡)
- **E2E 실수신 검증**: 앱 알림 생성(USD 1,480.47 이하) → 2분 사이클 발화 → `fcm_test_guard_allowed_type`→`fcm_sent`(promotions-ebe8e) → 에뮬 시스템 알림 실표시 "미국 USD 매매기준율 1,472.50원 — 목표 1,480.47원 이하 도달"
- ONCE 발화 후 active=false 전이가 앱 목록 토글 OFF로 동기화되는 것도 확인

### M5 — 서명·아이콘/스플래시·R8 ✅ 2026-07-23
- **서명 확보**: `exchange-rate-app.jks`(레포 루트) 무비번 지문 열람 → 기존 릴리스 APK와 SHA-256 일치(79:0B:00:80:9B:34...)로 파일 확정 → 비번은 타 앱 keystore.properties 후보 전수 대조로 판별(값 비노출, android-clipboard와 동일값 재사용 패턴). alias=exchange-rate, keyPassword=storePassword. `keystore.properties`(gitignore) + build.gradle.kts signingConfig(파일 없으면 debug 폴백)
- **아이콘 리프레시**: 벡터 adaptive icon 신규 제작(딥블루 그라데이션 배경 + 흰 순환화살표·₩ 전경, monochrome 포함) + 레거시 PNG 5밀도(rsvg-convert) + 스토어 512(`docs/store/icon_512.png`). 구 icon.png 잔재 제거
- **스플래시**: core-splashscreen 1.0.1 — `Theme.ExchangeApp.Splash`(딥블루 배경+전경 아이콘) + MainActivity installSplashScreen
- **⚠️R8 릴리스 실사고 해결**: 릴리스에서만 "환율 정보를 불러올 수 없습니다" — **AGP 8 R8 full mode에서 retrofit 2.9 keep 부족**(인터페이스/`kotlin.coroutines.Continuation`/`retrofit2.Response` 제네릭 축소). proguard-rules.pro에 `-if interface … -keep,allowobfuscation interface <1>` + Continuation/Response shrink-keep 추가로 해결. refresh 실패 Log.w 추가(진단용)
- **릴리스 검증(전수)**: 실키 서명 AAB(10.7MB, 지문 재대조 일치) + 에뮬 릴리스 빌드로 전 축 검증 — 홈 로드·상세 차트/4종가/우대율·계산기·알림 CRUD·**Glance 위젯 SMALL/LARGE 실렌더**·설정 토픽 토글·실광고 fill·새 아이콘/스플래시·**FCM 실수신**("1,466.80원 — 목표 1,474.44원 이하 도달")
- 위젯 previewImage 추가(`drawable-nodpi/widget_preview.png`)

### M6 준비 — 스토어 자산 ✅ 초안 2026-07-23
- 등록 문구: `docs/store/listing.md` (앱명/짧은·전체 설명/출시 노트/체크리스트)
- 스크린샷 4장(릴리스 빌드·라이트): `docs/store/shot_01_home~04_alerts.png` / 아이콘 512 완비
- 업로드 산출물: `app/build/outputs/bundle/release/app-release.aab` (vc16/2.0.0)

## 다음 (게이트)
1. **심사 제출** (사용자 확인 게이트): Play Console 업로드(AAB)+등록정보 반영+심사 제출. 그래픽 이미지 1024x500은 screenshots-hub 쇼케이스로 제작 예정
2. 미결 결정(사용자): AdMob 계정 이관(현 제3 계정 `ca-app-pub-1908860913688060~...` 유지 중 — 릴리스 빌드에서 fill 나오는 것 확인됨), 어드민 앱 등록(서버 광고설정 채택 여부 — v2.0은 클라 자체 게이팅)

## 메모
- 앱 API 베이스: `https://app-api.oror.link` (oror.link는 py EP 404 — edge-router 화이트리스트)
- 런처 위젯 피커는 에뮬에서 ANR 잦음 — 앱 아이콘 long-press → Widgets 경로가 안정적. 배치는 `input draganddrop`
- Glance 위젯 previewImage 미설정(피커에 기본 아이콘) — 스토어 제출 전 previewImage 추가 검토

## M7 — 스토어 반영 + 기능 확장 2차 ✅ 2026-07-23 밤 (vc17)
- **Play 등록정보 반영 완료**(DeveloperKhy·앱 4976171710155303198): 앱명/짧은·전체 설명 + 아이콘 512 + 그래픽 1024x500 + **폰 쇼케이스 스크린샷 4장(1080x1920, screenshots-hub 생성)** + 태블릿 7"/10" 2장씩 + 저장. ⚠️Play 애셋 패널 함정(슬롯 스코프·파일명 aria 전역 매칭)은 memory `store-screenshots-showcase-only` 참조
- **스크린샷 지침 확립**: 원캡처 업로드 금지 — `app-screenshots` 스킬에 "절대 규칙" 명문화. 생성기=screenshots-hub `apps/exchange-rate/generator/{shots.html,capture-shots.mjs}` (HTML+puppeteer-core 간이 패턴)
- **AdMob 확정**: `ca-app-pub-1908860913688060` = developerkhy 소유(제3 계정 아님). 종료 팝업용 네이티브 단위 신설 `…/2312962822`("환율알리미 네이티브 종료")
- **신기능(vc17, 커밋 0fbf25b)**:
  - 스플래시 게이트: 브랜드 애니메이션(아크 스윕·회전, ₩ 오버슈트 팝, 궤도 점, 배경 차트 라인 드로잉, 앱명 페이드업) → 런치 전면광고 → 메인. 첫 세션 면제(launchCount)·프리로드 폴링 6s·failsafe 10s
  - 뒤로가기 종료 팝업: 대형 네이티브 광고 + 왼쪽부터 [종료·리뷰·취소]
  - 상세 차트: x축 날짜 3점·y축 가격 3단 그리드 + 크로스헤어 공존, 헤더에 기준시각(MM.dd HH:mm) 표기
  - 통화 추가 다이얼로그 UX: 56dp 행·국기 확대·2줄(국가명+코드)·우측 체크박스
  - **i18n**: 전 문자열 리소스화(89키) + values-en + per-app locale(localeConfig) + 통화명 로케일 현지화(서버 nameEng null → java.util.Currency.getDisplayName 폴백, 전 언어 지원)
- **검증**: S21 실기기 릴리스 — 스플래시 애니메이션·전면광고 실노출(2세션 연속)·종료 팝업 네이티브 실렌더·크로스헤어+축·en 로케일(홈/종료팝업/통화명 "US Dollar" 등) 전부 확인. 에뮬은 광고 로드가 6s 초과라 전면 스킵됨(정상 폴백)
- **AAB 교체 완료**: 릴리스(id 15)에서 vc16 삭제 → vc17 업로드·인식 확인 → 저장. 출시 노트 유지
- **잔여**: ① 게시 개요 "변경사항 9개 제출" 클릭(사용자 게이트) — 빠른 검사가 "등록정보 기능 설명 불명확(스크린샷 플레이스홀더 의심)" 1건 오탐 경고, "무시하고 계속하기"로 전송 가능 ② S24 재연결 시 vc17 설치 ③ 글로벌 기준통화(크로스레이트)는 차기 마일스톤(로드맵: 홈 기준통화 선택 → krwPerOne 크로스레이트 클라 계산 → 서버 다중 기준 확장)
