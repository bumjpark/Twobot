# Twobot
![Twobot사진](https://github.com/user-attachments/assets/469c5af8-5662-49b4-aabe-3bf669b793db)


챗봇 2개를 사용하는 어플

#프로젝트 소개
Twobot은  두 개의 AI 챗봇인 Gemini와 ChatGPT를 동시에 활용하여 사용자가 두 가지의 챗봇에서 답장을 확인할 수 있는 안드로이드 애플리케이션입니다.
이 앱은 1:1 채팅과 토론 기능을 제공하며 사용자가 기능을 더 잘 사용 할 수 있게 만들었습니다.

#개발 환경

![flask](https://github.com/user-attachments/assets/39f6f75e-db1d-4c09-8a51-cb341f001456)
![python](https://github.com/user-attachments/assets/18870185-7a88-4a3e-8c9b-bf1a0d085c28)
![mlkit](https://github.com/user-attachments/assets/d7727164-05ef-42ae-b6a6-d5b6e87333ec)
![chatgpt](https://github.com/user-attachments/assets/c2edc80e-931c-4a52-9f29-7f88aa26c50f)
![wpalskdl](https://github.com/user-attachments/assets/669b2efb-4770-4e02-af50-3a314e70c484)





#UserFlow

1.앱 실행 시, 메인 화면이 나타난다. 초기 상태에서는 입력 필드가 비어있고, Gemini/ChatGPT 카드가 숨겨져 있으며, 사이드 메뉴와 상단바(툴바) 버튼이 준비되어 있다.

2.사용자가 메시지를 입력(텍스트 입력, 음성 인식, 사진에서 텍스트 인식)한 후 전송 버튼을 누른다.

3.전송 직후 사용자 메시지가 chatHistory에 저장되고, 서버로 메시지가 전송된다.
서버로부터 Gemini와 ChatGPT의 응답이 돌아오면, 해당 응답이 chatHistory에 순서대로 추가된다.

4.화면에 Gemini/ChatGPT 카드가 표시되고 사용자는 사이드 메뉴를 열어 다양한 옵션을 선택할 수 있다.
"Two Bot 선택": 현재 메인 화면 유지.
"토론": DebateActivity로 화면 전환하여 토론 기능 이용.
"기록": RecordActivity로 이동해 현재까지의 chatHistory를 RecyclerView로 확인할 수 있다.

5.토론기능을 이용할때 주제를 입력하고 토론 시작/종료를 활용한다.

6.기록 화면(RecordActivity)에서 현재까지의 모든 대화를 열람할 수 있으며, 하단의 삭제 버튼을 누르면 chatHistory가 비워지고 화면이 갱신된다.

7.삭제 직후 대화 목록은 빈 상태가 되며, 필요하다면 SharedPreferences 등 저장소에도 빈 상태로 반영한다.

#ERD

#기능

#실행화면
![image](https://github.com/user-attachments/assets/b3aa440d-9429-4955-8299-78a0271c23ee)
![image](https://github.com/user-attachments/assets/5946c0b8-2330-4033-8560-719a52659a31)
![image](https://github.com/user-attachments/assets/58db15d5-03d8-40f5-a04f-f4513f3c678c)
![image](https://github.com/user-attachments/assets/36c18912-9039-44be-88ca-12c07f974f9c)
![image](https://github.com/user-attachments/assets/f8f2a6ea-89fb-437f-8c45-d4ab7e455714)
![image](https://github.com/user-attachments/assets/97e925d6-0747-414e-91e5-282a7cb78f1e)










#실행영상
[https://drive.google.com/file/d/1t1_depPQAgZlDBDmDrvYjaBr7oKDDVRo](https://drive.google.com/file/d/1t1_depPQAgZlDBDmDrvYjaBr7oKDDVRo)



