# MadStyles
## 당신을 위한 단 하나의 쇼핑앱
---
Week2 4분반 병찬,창우팀

- Vision ai를 이용하여 사용자의 사진과 제품 사진을 합성해 실착 모습을 보여주는 기능이 가능합니다
- 약 2만건의 Fashion DB를 바탕으로 사용자의 선호에 따른 상품 추천, 판매량에 따른 랭킹 표시, 필터 검색이 가능합니다
- KAKAO SDK를 이용하여 자체 로그인 뿐만 아니라 카카오 로그인도 가능합니다.

---

### a. 개발 팀원

- 이창우 - POSTECH 컴퓨터공학과 20학번
- 박병찬 - KAIST 전산학부 21학번

---

### b. 개발환경

- Language: Kotlin, Node.js
- Database: MongoDB
- OS: Android

```
minSdkVersion 26
targetSdkVersion 33
```

- IDE: Android Studio
- Target Device: Galaxy Note 10+

---

### c. 어플리케이션 소개

### 0.Login

***Major features***

- 쇼핑 앱에 어울리는 SplashScreen이 적용되어 있습니다.
- 일반 로그인 외 Kakao 로그인이 가능합니다. 이 경우 별도 닉네임을 설정할 수 있습니다.
- 새 계정을 만들때 사용자의 성별과 선호 항목(스타일, 색상, 가격대)을 설정할 수 있습니다.

---

***기술설명***

- Mainactivity에서 ```startActivityForResuilt```를 통해 바로 LoginActivity로 넘어옵니다. 
- splash 구현은 MainActivity에서 imageView의 Visibility를 조절하는 방식을 사용했습니다.
- Handler를 이용해 일정시간 지연 후 로그인 화면이 실행되게 하였습니다.
- Kakao 로그인의 경우 Token에서 회원 고유 ID를 얻어 처리합니다.
- 앱에서 사용하는 모든 이미지 소스는 xml과 피그마, PPT, 포토샵을 이용해 직접 제작했으며, 이를 통해 일관된 디자인을 구현하였습니다.

---

### TAB 1 - HOME

<img src="https://github.com/pbc1017/MadStyles/assets/20718582/40608ff9-e950-4067-8eab-c8f0bce69a0f" width="400" height="700"/>

***Major features***

- 사용자 정보를 바탕으로 총 3가지 Concept의 추천을 표시합니다.
- 각 아이템을 눌러 상세 정보를 볼 수 있습니다.

---

***기술설명***

- Mainactivity에 진입할때 사용자 id를 body에 담아 서버에 post 요청을 합니다.
- 서버에서는 특정 조건에 맞는 아이템을 뽑은 후, 랜덤으로 10개씩을 반환합니다.
- 앱에서 서버로의 요청은 
---

### TAB 2 - Ranking

<img src="https://github.com/pbc1017/MadStyles/assets/20718582/fe904c9c-df8e-453c-b85d-22e6e9045971" width="400" height="700"/>

***Major features***

- 판매량을 기준으로 1위부터 한 페이지에 20개씩 상품을 표시해줍니다.
- 오른쪽위의 Add Image를 누르면 카메라와 갤러리 팝업창이 뜹니다
- 갤러리를 누르고 사진을 선택하면 해당 사진이 추가됩니다
- 카메라를 누르고 사진을 촬영후 확인을 누르면 해당 사진이 추가됩니다
- 사진을 짧게 누르면 사진이 화면크기에 맞게 확대되고 화면을 누르거나 뒤로가기를 한번 누르면 갤러리로 돌아옵니다
- 사진을 길게 눌러서 삭제할 수 있습니다

---

***기술설명***

- RecyclerView를 사용하여 목록 형태의 인터페이스를 구현했습니다.
- recyclerview의 layoutmanager를 grid layoutmanager로 하고 squareFramelayout이라는 custom layout을 만들어서 이미지를 정사각형으로 보여지게 하였습니다.
- local 이미지는 Int로 갤러리 이미지는 uri로 카메라 이미지는 bitmap으로 datas에 저장하여 처리하였습니다
- Glide 라이브러리를 사용하여 이미지를 로드하고 보여주었습니다.
- Android Camera API를 사용하여 카메라로부터 사진을 캡처했습니다.
- Android Storage API를 사용하여 갤러리에서 이미지를 선택하고 로드했습니다

---

### TAB 3 - Search

<img src="https://github.com/pbc1017/MadStyles/assets/20718582/e5dd7c2c-f22d-433c-8067-b745be0e96bb" width="400" height="700"/>
<img src="https://github.com/pbc1017/MadStyles/assets/20718582/b83f4866-b2af-4ba4-a145-d36c80f2eec5" width="400" height="700"/>

***Major features***

- text를 입력후 사진 버튼을 누르면 해당 text에 해당하는 이미지를 generation 해서 보여줍니다
- 마이크를 누르면 녹음이 시작되고 정지버튼을 누르면 해당 음성이 text로 변환되어 자동으로 chat을 할 수 있습니다
- text 입력후 채팅 버튼을 눌러 chat을 할 수 있습니다

---

***기술설명***

- openAI사에서 제공하는 api를 연동하여 gpt 서버와의 상호작용을 구현하였습니다.
- 채팅 형태의 상호작용을 위해 openAI사의 api에서 제공하는 모델 중 gpt3.5-turbo 모델을 사용하였으며, 연속적인 대화의 흐름을 파악하여 답변할 수 있도록 하였습니다.
- 음성 형태의 상호작용을 위해 openAI사의 api에서 제공하는 모델 중 whispher-1 모델을 사용하였으며, 이를 통해 음성을 텍스트로 변환한 후 gpt 모델에 넣어 결과를 얻어냅니다.
- 이미지 생성을 위해 openAI사의 api에서 제공하는 모델 중 DALLE-1 모델을 사용하였으며, 이를 통해 텍스트를 바탕으로 이미지를 생성하고 서버의 이미지 url을 전달 받았습니다.
- 위에서 전달받은 url을 화면에 띄우기 위해 ~~
- 음성 녹음을 위해 특정한 코덱과 형식으로 내부 저장소에 파일을 쓸 수 있도록 구현했습니다.

---

### TAB 4 - Cart


***Major features***

- text를 입력후 사진 버튼을 누르면 해당 text에 해당하는 이미지를 generation 해서 보여줍니다
- 마이크를 누르면 녹음이 시작되고 정지버튼을 누르면 해당 음성이 text로 변환되어 자동으로 chat을 할 수 있습니다
- text 입력후 채팅 버튼을 눌러 chat을 할 수 있습니다

---

***기술설명***

- openAI사에서 제공하는 api를 연동하여 gpt 서버와의 상호작용을 구현하였습니다.
- 채팅 형태의 상호작용을 위해 openAI사의 api에서 제공하는 모델 중 gpt3.5-turbo 모델을 사용하였으며, 연속적인 대화의 흐름을 파악하여 답변할 수 있도록 하였습니다.
- 음성 형태의 상호작용을 위해 openAI사의 api에서 제공하는 모델 중 whispher-1 모델을 사용하였으며, 이를 통해 음성을 텍스트로 변환한 후 gpt 모델에 넣어 결과를 얻어냅니다.
- 이미지 생성을 위해 openAI사의 api에서 제공하는 모델 중 DALLE-1 모델을 사용하였으며, 이를 통해 텍스트를 바탕으로 이미지를 생성하고 서버의 이미지 url을 전달 받았습니다.
- 위에서 전달받은 url을 화면에 띄우기 위해 ~~
- 음성 녹음을 위해 특정한 코덱과 형식으로 내부 저장소에 파일을 쓸 수 있도록 구현했습니다.

---

### TAB 5 - My Page


***Major features***

- text를 입력후 사진 버튼을 누르면 해당 text에 해당하는 이미지를 generation 해서 보여줍니다
- 마이크를 누르면 녹음이 시작되고 정지버튼을 누르면 해당 음성이 text로 변환되어 자동으로 chat을 할 수 있습니다
- text 입력후 채팅 버튼을 눌러 chat을 할 수 있습니다

---

***기술설명***

- openAI사에서 제공하는 api를 연동하여 gpt 서버와의 상호작용을 구현하였습니다.
- 채팅 형태의 상호작용을 위해 openAI사의 api에서 제공하는 모델 중 gpt3.5-turbo 모델을 사용하였으며, 연속적인 대화의 흐름을 파악하여 답변할 수 있도록 하였습니다.
- 음성 형태의 상호작용을 위해 openAI사의 api에서 제공하는 모델 중 whispher-1 모델을 사용하였으며, 이를 통해 음성을 텍스트로 변환한 후 gpt 모델에 넣어 결과를 얻어냅니다.
- 이미지 생성을 위해 openAI사의 api에서 제공하는 모델 중 DALLE-1 모델을 사용하였으며, 이를 통해 텍스트를 바탕으로 이미지를 생성하고 서버의 이미지 url을 전달 받았습니다.
- 위에서 전달받은 url을 화면에 띄우기 위해 ~~
- 음성 녹음을 위해 특정한 코덱과 형식으로 내부 저장소에 파일을 쓸 수 있도록 구현했습니다.
