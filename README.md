# exchange-rate-app
This app is exchange rate app.
![alt text][logo]

##### 왜 시작하게 되었나? 
  - 오늘의 환율 앱을 보고 비슷하게 만들어 보자 라는 취지로 개발 시작.
  - 해외 영어로된 환율 앱의 경우 그래프, 다양한 환율정보를 제공한다.
  - 내가 원하는 기능을 마음껏 추가 해보고 싶어서.
  - 오늘의 환율 앱의 디자인과 기능 + 그래프보기 + 영어버전 제공을 추가로 하면 꾀 쓸만 한 앱이 나올 것이라 판단.
 
##### 기능정의
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
  5-1. 사용자는 파싱하는 시간을 설정 할 수 있다. (데이터를 매 초 단위로 파싱하면 비효율 적이므로, 기본 디폴트 값을 정한다.)
  5-2. 앱 시작 화면을 설정 할 수 있다. ( 탭 설정 - 환율정보, 계산기, 환율 알람)
  5-3. 앱 실행시 자동 업데이트를 체크 할 수 있다.
  5-4. 알람 소리 또는 진동을 선택 할 수 있다.
  5-5. 사용자의 지역에 따라 또는 사용자의 설정에 따라 언어변경이 가능하다.
6. 추가 기능
  6-1. 환율 정보 탭에서 상세보기를 통해 그래프를 확인 할 수 있다.

##### 작업 환경
  - Android Studio 2.3
  - MacOS 10.12.4

##### 사용된 라이브러리
* [DataBinding] - HTML enhanced for web apps!
* [Jsoup] - awesome web-based text editor
* [RecyclerView] - Markdown parser done right. Fast and easy to extend.
* [CardView] - great UI boilerplate for modern web apps
* [Glide] - evented I/O for the backend
* [Realm] - fast node.js network app framework [@tjholowaychuk]
* [constraint-layout] - the streaming build system
----
#### 사용 예정인 라이브러리
* [Retrofit] - awesome keyboard handler lib by
* [JUnit] - duh
* [Mockito framework] - 

#### ETC
  - 개발 진행 도중 추가 되었으면 하는 아이디어나 내용들 작성 예정.
  - 네이버의 HTML 환율 정보의 데이터 순서가 HTML파싱을 통해 긁어오는 정보라서 불안정한 경우가 있다.
  - 다음 기술들도 사용해볼것 rxJava, stetho
  - 
  

[DataBinding]: <https://developer.android.com/topic/libraries/data-binding/index.html?hl=ko#method_references>
[Jsoup]: <https://jsoup.org/>
[RecyclerView]: <https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html>
[CardView]: <https://developer.android.com/training/material/lists-cards.html>
[Glide]:<https://github.com/bumptech/glide>
[Realm]:<https://realm.io/>
[constraint-layout]:<https://developer.android.com/training/constraint-layout/index.html>
[Retrofit]: <http://square.github.io/retrofit/>
[JUnit]:<http://junit.org/junit4/>
[Mockito framework]:<http://site.mockito.org/>

[logo]: http://deltaplusforex.com/wp-content/uploads/2016/05/Transformacion-y-ventajas-del-nuevo-mercado-en-Forex..jpg "Logo image"
