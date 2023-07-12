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
- 앱에서 서버로 요청을 보내면, 서버는 ```MongoClient```를 통해 mongodb에 access합니다.
- 앱에서 서버로의 요청은 ```OkHttp```를 사용했으며 JSON을 통해 Query와 Response를 주고받습니다.
- Glide 라이브러리를 사용하여 이미지를 로드하고 보여주었습니다.
    
---

### TAB 2 - Ranking

<img src="https://github.com/pbc1017/MadStyles/assets/20718582/fe904c9c-df8e-453c-b85d-22e6e9045971" width="400" height="700"/>

***Major features***

- 판매량을 기준으로 1위부터 한 페이지에 20개씩 상품을 표시해줍니다.
- 옷 종류별 랭킹을 볼 수도 있습니다.
- 기본적으로 자신의 성별에 맞게 아이템이 나오지만, 원하면 다른 성별의 옷도 확인할 수 있습니다.

---

***기술설명***

- 옷 종류를 설정하는 버튼의 경우 xml에서 디자인하지 않고 동적으로 view를 생성합니다. 
- db에서 요청한 데이터들을 '판매량'을 기준으로 내림차순 정렬하기 위해 sort를 사용하며 이를 20개씩 끊기 위해 limit을 사용합니다.
- skip을 사용하면 앞의 n개 데이터를 건너뛸 수 있는데, 이를 활용하여 페이지 넘기기를 구현합니다.

---

### TAB 3 - Search

<img src="https://github.com/pbc1017/MadStyles/assets/20718582/e5dd7c2c-f22d-433c-8067-b745be0e96bb" width="400" height="700"/>
<img src="https://github.com/pbc1017/MadStyles/assets/20718582/b83f4866-b2af-4ba4-a145-d36c80f2eec5" width="400" height="700"/>

***Major features***

- 키워드를 이용한 검색이 기본이며 검색 필터 적용이 가능합니다.
- 
- '남자 빨강 바지' 와 같이 검색하면 남자, 빨강, 바지에 해당하는 필터가 자동으로 선택됩니다.
- 검색 결과를 인기순, 낮은 가격순, 높은 가격순으로 정렬할 수 있습니다.
- 옷 종류, 가격, 옷사진, 성별, 가격대, 색깔을 필터링하여 검색할 수 있습니다.
  
---

***기술설명***

- 검색어에 필터 키워드가 포함되었는지 확인하기 위해 검색어를 tokenization하여 분석합니다.
- 또한 유사 검색어 항목을 추가하여 필터 키워드와 비슷한 단어들도 키워드로 인식되도록 합니다.(e.g 팬츠->바지)

---

### TAB 4 - Cart

<img src="https://user-images.githubusercontent.com/20718582/252948934-b5f4c7c6-e6ad-459c-98da-f938f2857038.png" width="400" height="700"/>
<img src="https://github.com/pbc1017/MadStyles/assets/20718582/c4e33a18-98af-4054-8233-115138fc8125" width="400" height="700"/>

***Major features***

- 상세 정보 페이지에서 장바구니에 담은 상품들을 확인할 수 있습니다.
- 구매 수량을 변경하거나 선택한 상품을 장바구니에서 제거할 수 있습니다. 
- 상의의 경우, 미리 등록해 놓은 body image를 통해 옷의 착용 사진을 AI generation으로 확인할 수 있습니다.
  
---

***기술설명***

- AI generation의 경우 2020년에 발표된 Pytorch 기반 VITON-HD 모델을 활용했습니다.
- 해당 모델을 구글 colab 환경에서 실행하고, Flask를 통해 서버로부터 생성 요청을 받습니다. 
- 장바구니 정보는 MongoDB에 상품 ID를 배열 형식으로 저장합니다. 

### TAB 5 - My Page
<img src="https://github.com/pbc1017/MadStyles/assets/20718582/feb2e0fb-bae7-4584-b959-4848c2c82a26" width="400" height="700"/>

***Major features***

- 로그아웃 후에 새로운 계정으로 로그인할 수 있습니다.
- AI generation에 사용되는 이미지를 선택할 수 있습니다.
- 최근 본 상품에서 최근에 상품의 상세 정보를 확인했던 상품의 목록을 최대 10개까지 확인할 수 있습니다.

---

***기술설명***

- 이미지 피커는 인텐트를 통해 내장 이미지 앱을 실행합니다.
- 로그아웃 시 카카오에서 생성한 토큰을 삭제한 후 다음 로그인 떄 재생성합니다.
- 최근 본 상품은 상품을 클릭할때마다 MongoDB의 유저 정보에 큐 형태로 상품의 아이디를 저장합니다.
