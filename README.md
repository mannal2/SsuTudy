# SsuTudy
숭실대 전용 열품타

### 기능
1. 유세인트 연동 로그인
2. 강의 일정 관리
3. 친구 추가
4. 할 일 기록
5. 그룹 할 일 기록
6. 공부 시간 측정
7. 과목별 공부 시간 랭킹

### 맡은 작업
<img width="781" alt="image" src="https://github.com/user-attachments/assets/453cfde4-cce2-43e9-8d39-f87b68f6a08d" />

### 어려웠던 개발 부분
##### 유세인트 연동 로그인
안드로이드에서는 보통 webview를 이용한 정적 웹페이지 파싱이 대부분이다. <br/>
하지만 유세인트는 동적 웹페이지라 webview로는 힘들고 셀레니움을 사용하는게 편하다. <br/>
다만 셀레니움은 안드로이드OS에서 작동하지 않기에 서버를 하나 거쳐야 해서 <br/>
개발볼륨이 늘어나기 때문에 사용하지 않기로 결정하였다. <br/>
여러번의 시도 끝에 webview를 이용한 동적 웹페이지 파싱을 구현해냈다. <br/>
![image](https://github.com/user-attachments/assets/7f6ff937-c131-489e-8848-dd80ce465164)
onPageFinished 함수가 실행될때마다 매번 다른 코드가 실행되게 만들어 자동 로그인을 구현하였다. <br/>
![image](https://github.com/user-attachments/assets/3019db3a-b992-4756-8ecf-c1b65bbd46ec)
사용자 이름은 html파일 내용에 포함되어 있기 때문에 innerHTML을 전송받은후 이름을 찾아내었다.
![image](https://github.com/user-attachments/assets/5006af83-0580-4272-8bd3-ef2651769878)
수강 강의 목록은 ajax응답을 통해 화면에 뿌려지기 때문에 innerHTML로는 강의 목록을 알아낼 수 없었다. <br/>
따라서 ajax요청이 완료되면 해당 ajax응답을 안드로이드 기기에 전송하는 함수를 미리 웹페이지에서 작동하게 넣어놓은 후<br/>
![image](https://github.com/user-attachments/assets/57fe5e32-44b5-4149-bf67-93c56facc5fb)
ajax응답 문자열에서 위 정규표현식을 활용해 강의 정보들을 찾아내었다.

### 업데이트 하면 좋을 부분
현재 앱은 프레그먼트를 활용하지 않아서 액티비티 전환 간 약간의 오류가 발생할 수 있다. <br/>
프레그먼트를 활용하는 식으로 리팩토링을 한다면 안정성이 높아질 것으로 예상된다.
