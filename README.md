# JAM 🎵
> 음악 커뮤니티, 중고거래, 구인구직 등을 함께 이용할 수 있는 통합 음악 커뮤니티 플랫폼

<br/>

### 메인 페이지
<p align="center">
  <img src="docs/images/main.png" width="90%" style="border: 1px solid #eaecef;">
</p>

<br/><br/>

## 목차
- [프로젝트 개요](#-프로젝트-개요)
- [배포 주소](#배포-주소)
- [주요 기능 및 서비스 화면](#주요-기능-및-서비스-화면)
- [로컬 실행 가이드](#로컬-실행-가이드)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [ERD](#erd)
- [기술적 고민](#-기술적-고민)
- [프로젝트 구조](#-프로젝트-구조)
- [회고](#-회고)



<br/><br/>

## 🎯 프로젝트 개요

기존 음악 커뮤니티들은 소통, 구인구직, 중고거래 등이 여러 플랫폼에 파편화되어 있어 사용자가 매번 이동해야 하는 불편함이 있었습니다.

JAM은 이러한 불편함을 해소하고, 음악인들이 필요한 모든 활동을 한곳에서 즐길 수 있는 환경을 목표로 개발했습니다.

프로젝트 이름은 즉흥 연주라는 의미의 Jam Session에서 착안했습니다.

<br>


### 배포 주소
- 서비스 : https://jam.it.kr/
- 기업 계정
  - ID : company1
  - PW : test1234
- 일반 회원 계정
  - ID : member123
  - PW : test1234

※ 두 계정을 이용하여 채팅 기능을 테스트할 수 있습니다.

<br/><br/>

## 주요 기능 및 서비스 화면

### 커뮤니티
> 음악 장비, 감상, 작업 팁 등을 자유롭게 공유하는 커뮤니티 게시판

<br>
<table width="100%">
  <tr>
    <td width="50%" align="center" valign="top">
      <b>[ 목록 페이지 ]</b><br><br>
      <img src="docs/images/communityList.png" width="100%">
    </td>
    <td width="50%" align="center" valign="top">
      <b>[ 상세 페이지 ]</b><br><br>
      <img src="docs/images/communityDetail.png" width="100%">
    </td>
  </tr>
</table>
<br>

**📝 게시글**
- 게시글 작성 / 조회 / 수정 / 삭제
- 작성한 게시글 다중 삭제
- 이미지 파일 업로드 지원
- 조회수 및 댓글 수 기반 인기글 노출


**💬 댓글**
- 댓글 조회 / 작성 / 수정 / 삭제


**🔍 검색**
- 제목 및 내용 통합 검색


**⭐ 북마크**
- 게시글 북마크 추가 / 삭제
- 북마크한 게시글 조회
<br>

---

<br>

### 중고 악기 거래
> 사용자가 악기를 사고팔며, 판매자와 채팅으로 직접 소통할 수 있는 중고 거래 게시판

<p align="center">
  <img src="docs/images/fleaMarket.gif" width="80%">
</p>

<br>


**🎸 상품 게시글**
- 상품 게시글 작성 / 조회 / 수정 / 삭제
- 작성한 상품 게시글 조회
- 판매 상태 변경 (판매중 / 판매완료)


**🖼️ 이미지**
- 상품 이미지 다중 업로드 지원


**🔍 검색**
- 카테고리 기반 상품 검색


**❤️ 찜 (관심 상품)**
- 상품 찜 추가 / 삭제
- 찜한 상품 조회


**💬 채팅 연동**
- 상품 게시글 기반 채팅 연결 (상품 문의 → 채팅)
<br>

---

<br>

### 구인 구직
> 음악 관련 구인 공고를 확인하거나 밴드 멤버를 모집하고, 원하는 공고에 지원할 수 있는 게시판

<p align="center">
  <img src="docs/images/jobs.gif" width="80%">
</p>


<br>


**🏢 공고 관리 (기업)**
- 구인 공고 및 밴드 멤버 모집글 등록
- 공고 작성 / 조회 / 수정 / 삭제
- 작성한 공고 목록 조회
- 공고 상태 관리 (진행중 / 마감)


**👤 지원자 관리 (기업)**
- 공고 지원자 목록 조회
- 지원자의 지원서 조회 및 이력서 다운로드
- 공고별 지원 현황 조회


**🎯 공고 탐색 (사용자)**
- 지역 기반 모집 공고 조회
- 지역 및 포지션 기반 검색
- 키워드 검색 (지역/포지션 검색과 별도)


**📄 지원 기능 (사용자)**
- 공고 지원 시 이력서 파일 업로드
- 지원 내역 조회 (공고 상태 및 지원 시점 기준 필터링)
- 지원 취소


**🧾 이력서 관리**
- 작성한 지원서 조회 및 이력서 다운로드


**🔄 회원 전환**
- 일반 회원 → 기업 회원 전환


**⭐ 스크랩**
- 공고 스크랩 추가 / 삭제
- 스크랩한 공고 조회
<br>

---

<br>

### 실시간 채팅
> 사용자 간 메시지를 주고받으며 거래 관련 대화를 나눌 수 있는 실시간 채팅 기능

![채팅 1:1 대화 화면](docs/images/chat.gif)


**💬 1:1 채팅**
- 사용자 간 실시간 1:1 채팅 (WebSocket 기반)
- 채팅방 생성 및 입장


**📩 메시지**
- 텍스트 메시지 전송 및 수신
- 채팅 메시지 실시간 동기화


**🗂️ 채팅방 관리**
- 채팅방 목록 조회
- 채팅방 별 메시지 히스토리 조회
- 마지막 메시지 미리보기 제공
- 최근 메시지 기준 채팅방 정렬
- 메시지 전송 시간 표시


**🔗 서비스 연동**
- 중고거래 게시글 기반 채팅 연결 (상품 문의 → 채팅)


<br/><br/>

## 🐳로컬 실행 가이드

### 1. 사전 요구 사항

- Docker & Docker Desktop: 설치 및 실행 상태여야 합니다.


<br>


### 2. 프로젝트 설정

**① 프로젝트 클론**

```bash
git clone https://github.com/Lee-jinri/JAM.git
cd JAM
```

**② 환경 변수 및 설정 파일 준비**
프로젝트 루트 디렉토리에서 아래 파일들의 이름을 변경하고 실제 값을 입력해주세요.

- `.env.example` → `.env` (애플리케이션 상세 설정 등)
- `src/main/resources/application-template.yml` → `application.yml`
- LOCATION_CONSUMER은 sgis 통계지리정보서비스의 서비스 ID와  보안 KEY 입니다.


<br>

### 3. 애플리케이션 실행

아래 명령어를 입력하면 Oracle DB(23c Free)와 Spring Boot 서버가 자동으로 구성됩니다.

`docker-compose up -d`

- **접속 정보**: http://localhost:8080
- **테스트 계정**:

| **권한** | **아이디** | **비밀번호** |
| --- | --- | --- |
| **기업 회원** | `company1` | `test1234` |
| **일반 회원** | `member123` | `test1234` |


<br>

### 4. 환경 정리 및 초기화

**① 서비스 중지**

`docker-compose stop`

**② 완전 삭제 및 초기화**
컨테이너뿐만 아니라 저장된 DB 데이터까지 모두 삭제하고 초기 상태로 되돌리고 싶을 때 사용합니다.

`docker-compose down -v`

> **`-v` 옵션 사용 시 로컬에 생성된 모든 DB 볼륨 데이터가 삭제됩니다.**
> 

---

### **🚨주의사항**

- 로컬 환경에서는 게시글 본문 검색 기능이 동작하지 않을 수 있습니다.
- **DB 권한 관련 에러**: 윈도우 환경에서 `oracle-data` 폴더 삭제가 거부될 경우, 터미널을 관리자 권한으로 실행하거나 Docker를 통해 해당 폴더를 삭제해야 합니다.
- **포트 충돌**: 로컬에 이미 오라클(1521)이나 톰캣(8080)이 실행 중인지 확인해주세요.


<br/><br/>


## 기술 스택

**Backend**: 
- Java 17, Spring Boot 3.x (Jakarta EE)
- Redis (캐싱, 분산 락)
- WebSocket (STOMP 없이 직접 구현)
- Spring Security + JWT (인증/인가)

**Infra & Database**: 
- AWS EC2, S3 (Presigned URL)
- GitHub Actions (CI/CD)
- Tomcat (WAR 배포)
- Oracle
- MyBatis

**Frontend**: 
- HTML, CSS, JavaScript
- jQuery
- Thymeleaf


<br>


## 시스템 아키텍처

<p align="center">
  <img src="docs/images/architecture/architecture.jpg" width="60%">
</p>

<br>

## ERD
### 회원/커뮤니티/파일

<p align="center">
  <img src="docs/images/erd/memberCommunityFileAsset.png" width="80%">
</p>

<br>

### 중고 악기

<p align="center">
  <img src="docs/images/erd/fleaMarket.drawio.svg" width="60%">
</p>


<br>

### 구인구직

<p align="center">
  <img src="docs/images/erd/job.drawio.svg" width="60%">
</p>

<br>

### 채팅

<p align="center">
  <img src="docs/images/erd/chat.drawio.svg" width="80%">
</p>


<br/><br/>


## 🚀 기술적 고민
### 1️⃣ STOMP 미사용 WebSocket 세션 직접 관리

**배경:**
라이브러리 추상화 뒤에 숨겨진 메시지 흐름을 깊이 이해하고자 Raw WebSocket 선택

**해결:**
- `WebSocketHandler`를 직접 구현하여 세션 생명주기 관리
- `ConcurrentHashMap`기반 세션 저장소 설계  (멀티스레드 환경 고려)
- Double Mapping: 방 기준(chatRoomSession) +  세션 기준(sessionToChatRoom) 양방향 관리로  연결 종료 시 O(1) 탐색
- 비인가 사용자 연결/메시지 요청 검증
- 메시지 전송 전 채팅방 참여 여부 서버 재검증
- 상대방이 채팅방에 없을 때 실시간 알림 전송으로 메시지 수신 보장

**성과:**
실시간 1:1 채팅 시스템 구축 및 WebSocket 프로토콜 동작 원리 깊이 이해

- O(1) 기반 세션 정리 구조로 연결 종료 처리 성능 개선
- 채팅방 참여 검증 로직으로 비인가 메시지 차단
- 실시간 1:1 채팅 안정적 처리 구조 구축

👉 [관련 소스코드 보기](src/main/java/com/jam/chat/webSocket/WebSocketHandler.java)


<br><br>


### 2️⃣ Redisson 분산 락을 활용한 동시성 제어

**배경:**
1:1 채팅방 생성 시 동시 요청으로 인한 데이터 중복 생성(Race Condition) 리스크 인지

**해결** :

- Redis 기반의 `Redisson` 분산 락을 도입하여 다중 서버 환경에서도 유일한 채팅방 생성을 보장하는 로직 설계
- pairKey 정규화로 동일 사용자 쌍 직렬화
- Double-Checked Locking으로 불필요한 DB 접근 최소화
- finally에서 isHeldByCurrentThread() 체크로 안전한 락 해제
- DB 유니크 제약 + DuplicateKeyException fallback으로 다층 방어

**성과:** 
- 단일 서버 환경에서 동일 사용자 중복 클릭 (150ms 간격) → 중복 생성 0건
- 서로 다른 두 사용자 초정밀 동시 요청 (6ms 간격) → 중복 생성 0건

**트레이드 오프**:

- 락 획득 대기 시간으로 인해 응답 지연 가능성 존재
- Redis 장애 시 채팅방 생성 로직 영향 (인프라 의존성 증가)


👉 관련 소스코드 보기
<br>
- [ChatRoomFacade.java: 분산 락 획득/해제 및 락 최적화 로직 (Facade Pattern)](src/main/java/com/jam/chat/service/ChatRoomFacade.java)
- [ChatService.java: 트랜잭션 처리 및 DuplicateKeyException 예외 2차 방어 로직](src/main/java/com/jam/chat/service/ChatService.java)


<br/>
<br/>

### **[검증1] 동일 사용자의 중복 클릭 요청 검증 (수동 테스트)**

사용자가 채팅 시작 버튼을 빠르게 두 번 클릭하는 상황(약 150ms 간격)을 가정하여 
중복 채팅방이 생성되지 않는지 확인했습니다.

<p align="center">
  <img src="docs/images/ChatConcurrentTest.gif" width="80%">
</p>

<br>

### **서버 로그**

```jsx
[첫 번째 요청 - 채팅방 생성]

14:56:42.081  ChatRestController.getChatRoomId(..) START
14:56:42.120  ChatService.createChatRoomWithTransaction(..) START
14:56:42.132  새로운 채팅방 생성 완료: 23
14:56:42.132  ChatService.createChatRoomWithTransaction(..) END return=23
14:56:42.154  ChatRestController.getChatRoomId(..) END return=23

[두 번째 요청 - 중복 생성 방지]

14:56:42.234  ChatRestController.getChatRoomId(..) START
14:56:42.242  ChatService.createChatRoomWithTransaction(..) START
14:56:42.244  ChatService.createChatRoomWithTransaction(..) END return=23
14:56:42.248  ChatRestController.getChatRoomId(..) END return=23
```

<br>

**[결과]**
- 두 요청 모두 동일 room_id=23 반환 → 중복 생성 0건

<br><br>

### **[검증2] 초정밀 동시 요청 검증 (JMeter)**

유저 A와 B가 **0.006초 차이**로 동시에 채팅방 생성을 시도하는 상황을 JMeter Synchronizing Timer로 테스트했습니다.

<br>

### **서버 로그**

```jsx
[동시 요청 시작]
13:54:21.230  [exec-10] ChatRestController.getChatRoomId(..) START 
13:54:21.236  [exec-4 ] ChatRestController.getChatRoomId(..) START

[요청 1이 먼저 채팅방 생성 시도]
13:54:21.252  [exec-10] ChatService.createChatRoomWithTransaction(..) START 
13:54:21.259  [exec-10] INSERT INTO chat_room ... 
13:54:21.264  [exec-10] 새로운 채팅방 생성 완료: 16
13:54:21.268  [exec-10] ChatRestController.getChatRoomId(..) END return=16

[요청 2는 이미 생성된 채팅방을 조회]
13:54:21.275  [exec-4 ] ChatService.createChatRoomWithTransaction(..) START 
13:54:21.283  [exec-4 ] ChatService.createChatRoomWithTransaction(..) END return=16
13:54:21.283  [exec-4 ] ChatRestController.getChatRoomId(..) END return=16
```

**[결과]**

- Thread 1이 채팅방 생성
- Thread 2는 Double-Checked Locking으로 기존 방 재사용
- **중복 데이터 생성 0건**, **DB 무결성 100% 유지**.

<br><br>

### **3️⃣** S3 Presigned URL 기반 클라이언트 직접 업로드

**배경:** 
서버 메모리를 거치는 기존 업로드 방식의 서버 사이드 오버헤드와 I/O 병목 및 확장성 문제 고려

**해결:**

- 서버는 Presigned URL만 발급하고 실제 파일은 클라이언트가 S3로 직접 업로드하는 구조 설계
- 업로드 전 서버에서 파일 타입/크기 사전 검증
- Promise.all로 다중 파일 병렬 업로드 처리
- 다운로드 시 인가된 사용자만 URL 발급 (URL 탈취 시에도 무단 접근 방지)

**성과:** 

- 서버 Heap 점유 없이 파일 업로드 처리
- URL 기반 접근 제어로 무단 다운로드 방지

<br><br>

### 4️⃣ 커뮤니티 검색 성능 최적화 (DB 쿼리 레벨 약 2~2.5배 개선)

**배경:** 
제목(VARCHAR)과 본문(CLOB)을 함께 검색하는 통합검색 API에서 응답 지연(1.26s)을 확인. (10만 건 데이터 기준)

**해결 및 트러블슈팅:**

- **1차 시도:** 제목에 B-Tree 인덱스 적용 <br>
→ LOWER() 함수 + 선행 와일드카드 구조로 인해 인덱스가 Predicate Type: filter로 처리됨. <br>
→ title(VARCHAR)은 풀스캔이어도 상대적으로 가벼워, 실제 병목은 content(CLOB)에 있음을 인지 <br>

- **2차 시도:** 본문(CLOB) 검색을 위해 Oracle Text(CONTEXT INDEX) 도입<br>
→ 본문 단독 검색 Cold 1.182s → 0.448s로 대폭 개선 <br>
(LIKE 대비 A-Time 기준 약 28배 개선: 1.12s→0.04s) <br>

- **3차 시도:** 제목/본문 조건을 OR로 결합 <br>
→ **CONTAINS가 access에서 filter로 강등됨.** <br>
→ title 매칭되지 않을 때 Cold 응답시간이 기존보다 약 1.5배 느려짐. (1.101s → 1.671s) <br>
→ 검색 조건(title 매칭 여부)에 따라 Buffers가 최대 276배(731 ~ 202,000)까지 변동하여 **성능을 예측할 수 없음.** <br>

- **최종 해결:** 제목 검색과 본문 검색을 별도 쿼리로 분리 후 UNION ALL로 결합 <br>
→ **검색 조건과 무관하게 안정적인 성능 확보**

<br>

**성과:** 

**1. 응답 시간 개선 (API와 Cold 기준, 10만 건 데이터)**

| 측정 레벨 | 기존 | OR 결합 (실패) | 최종 (UNION ALL) |
| --- | --- | --- | --- |
| **DB 쿼리** | 1.101s | 최대 1.671s | **0.449~0.55s** |
| **API 응답시간** | 1.26s | 6.12s | **121ms** |
- DB 쿼리 응답시간 **약 2~2.5배 개선**,  API 응답시간 **약 10배 개선** <br>
※ DB 쿼리 레벨은 순수 SQL 실행 성능 검증용,  API 레벨은 실제 서비스 체감 성능 지표

<br>

**2. 자원 사용량 개선 및 인덱스 정상화**

| 방식 | Buffers (논리적 읽기) | 비고 |
| --- | --- | --- |
| OR 결합 (최악의 경우) | 202,000 | DOMAIN INDEX 미사용, filter 강등 |
| UNION ALL (최종) | 2,436(약 83배 감소) | DOMAIN INDEX 정상 사용, access 유지 |
- 논리적 읽기(Buffers) **약 83배 감소.**
- UNION ALL 분리 후 정상적으로 Domain Index를 통한 조회(access)로 전환

<br>

**3. 성능 안정성 확보**

```
OR 결합:    Buffers 731 ~ 202,000 (약 276배 변동, 예측 불가)
UNION ALL:  Buffers 2,436 ~ 6,833 (약 2.8배 변동, 안정적)
```

- 검색 조건과 무관하게 Buffers 2,436~6,833 수준으로 일관되게 유지되어 예측 가능한 성능 확보

<br>

**트레이드오프 :**

- Oracle Text 인덱스 동기화 주기로 인한 실시간 반영 지연 가능성 존재
- title은 여전히 Full Table Scan이며 게시글 수가 크게 늘어나면 title 검색도 재검토 필요
- 현재 측정은 단일 세션 순차 실행 기준으로 다중 사용자 동시 요청 시 자원 경쟁 등 동시성 관점은 검증되지 않음.


👉 [검색 성능 최적화 분석 상세 문서(Notion)](https://www.notion.so/396e7bde342380ee9d8bd7f6db775e18?source=copy_link)

<br><br>


## 📂 프로젝트 구조

```text
src/main
 ├── java/com/jam
 │    ├── community
 │    │    └── controller
 │    │        ├── CommunityController.java      <-- View Controller (페이지 반환)
 │    │        └── CommunityRestController.java  <-- REST API Controller (데이터 처리)
 │    ├── chat
 │    │    ├── webSocket                          <-- WebSocket 직접 구현체
 │    │    └── service
 │    │        ├── ChatService.java               <-- 채팅 비즈니스 로직
 │    │        └── ChatRoomFacade.java            <-- 분산 락 처리
 │    ├── member                                  <-- 사용자 관리
 │    ├── config                                  <-- 보안 및 설정
 │    └── global                                  <-- 공통 처리 (예외 등)
 └── resources
      ├── mapper                                  <-- MyBatis Mapper (SQL 매핑) 
      └── templates                               <-- Thymeleaf 기반 View
``` 


- 도메인 중심 패키지 구조
- View Controller / REST Controller 분리
- ChatService / ChatRoomFacade 분리 (비즈니스 로직 · 분산 락 분리)
- config · global 패키지로 공통 기능 관리


<br>

## 💭 회고

- 초기 설계 미흡으로 기능 추가마다 구조를 개선해야 했고, 이 과정에서  설계의 중요성을 직접 체감했습니다.
- 단순히 기술을 적용하는 것보다 "왜 이 기술이 필요한가" 에 대한 근거를 찾는 과정의 중요성을 깨달았습니다.
- JPA와 React를 도입해 고도화하려 시도했으나 기존 구조와의 호환성 문제 및 트랜잭션 관리 등의 기술적 난관을 겪었습니다. 한정된 일정 내에 프로젝트 완성도를 높이기 위해 도입하지 못했지만 다음 프로젝트에서는 한층 더 견고한 애플리케이션을 구축하고자 합니다.
