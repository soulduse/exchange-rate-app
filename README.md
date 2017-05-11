# exchange-rate-app
This app is exchange rate app.


왜 시작하게 되었나? 
    - 오늘의 환율 앱을 보고 비슷하게 만들어 보자 라는 취지로 개발 시작.
    - 해외 영어로된 환율 앱의 경우 그래프, 다양한 환율정보를 제공한다.
    - 오늘의 환율 앱의 디자인과 기능 + 그래프보기 + 영어버전 제공을 추가로 하면 꾀 쓸만 한 앱이 나올 것이라 판단.
 
기능정의
1. 탭은 환율 정보, 계산기, 환율 알람으로 구분
2. 환율 정보 탭
    2-1. 환율정보를 리사이클러 뷰로 사용자에게 보여준다.
    2-2. 사용자는 자기가 보고자 하는 나라를 선택하여 볼 수 있다.
    2-3. 사용자가 선택한 데이터는 Realm DB에 저장하여 앱이 종료되더라도 저장된 값을 유지한다.
3. 계산기 탭
    3-1. 사용자는 자기가 원하는 나라의 환율로 계산을 해볼 수 있다.
    3-2. 간단한 사칙연산을 위주로 기능을 구현.
    3-3. 사용자가 계산했던 환율 (지정한 나라) 유지되어야 하므로 Realm에 나라 데이터 저장
    3-4. 나라에 맡게 환율이 적용되어 금액이 계산되어져야 한다. 
4. 환율 알람 탭    
    4-1. 사용자가 지정한 일정금액이 도달하면 사용자에게 알람으로 알려준다.
5. 설정
    5-1. 사용자는 파싱하는 시간을 설정 할 수 있다.
           (데이터를 매 초 단위로 파싱하면 비효율 적이므로, 기본 디폴트 값을 정한다.)
    5-2. 앱 시작 화면을 설정 할 수 있다. ( 탭 설정 - 환율정보, 계산기, 환율 알람)
    5-3. 앱 실행시 자동 업데이트를 체크 할 수 있다.
    5-4. 알람 소리 또는 진동을 선택 할 수 있다.
    5-5. 사용자의 지역에 따라 또는 사용자의 설정에 따라 언어변경이 가능하다.
6. 추가 기능
    6-1. 환율 정보 탭에서 상세보기를 통해 그래프를 확인 할 수 있다.







작업 환경
    - Android Studio 2.3

사용된 라이브러리

// data binding
dataBinding {
    enabled = true
}

compile 'com.android.support:appcompat-v7:24.2.1’
compile 'com.android.support:design:24.2.1'
// retrofit
compile 'com.squareup.retrofit2:retrofit:2.2.0'
// jsoup
compile group: 'org.jsoup', name: 'jsoup', version: '1.10.2'
// RecyclerView
compile 'com.android.support:recyclerview-v7:24.2.0'
// CardView
compile 'com.android.support:cardview-v7:24.2.0'
// Glide
compile 'com.github.bumptech.glide:glide:3.7.0'
// realm
apply plugin: 'realm-android'compile 'io.realm:android-adapters:2.0.0'
// Required -- JUnit 4 framework
testCompile 'junit:junit:4.12'
// Optional -- Mockito framework
testCompile 'org.mockito:mockito-core:1.10.19'
// constraint-layout
compile 'com.android.support.constraint:constraint-layout:1.0.0'compile 'com.google.android.gms:play-services-ads:10.2.1'


추가 기능
    - 개발 진행 도중 추가 되었으면 하는 아이디어나 내용들 작성 예정.

    - 네이버의 HTML 환율정보의 데이터 순서가 HTML파싱을 통해 긁어오는 정보라서 불안정한 경우가 있다.

    - rxJava, Realm, firebase
