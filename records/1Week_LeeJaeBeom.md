### 미션 요구사항 분석 & 체크리스트

---
#### 호감상대 삭제 처리
- [x] 삭제 테스트 코드 작성
- [x] 호감 목록의 소유권 확인(본인이 아니면 삭제X)
- [x] 삭제 처리 후 호감 목록 리스트로 다시 이동
- [x] 목록에 데이터 X

#### 구글 로그인 처리
- [x] provider_type_code 가 GOOGLE 로 해서 DB 저장
- [x] 로그인 후 회원의 username 을 상단에서 확인 가능


### 1주차 미션 요약

---
#### 호감상대 삭제(필수 미션)
- 삭제를 처리하기 전에 소유권 체크
- rq.redirectWithMsg 함수 사용하여 삭제 후 다시 호감목록 페이지로 이동

#### 구글 로그인(선택 미션)
- 스프링 OAuth2 클라이언트로 구현
- 구글 로그인으로 가입한 회원의 타입코드 : GOOGLE


**[접근 방법]**
- 호감 목록의 소유권을 확인하기 위해 instaMember 테이블의 아이디 값과 likeablePerson 테이블의 FromInstaMemberId 값을 비교
- oauth2Login으로 구분되고 CustomOAuth2UserService 클래스에 loadUser 메서드에서 providerTypeCode를 구분해서 없으면 가입을 시키고 있으면 
바로 리턴을 해주기 때문에 해당 메서드로 접근할 수 있게 application.yml 파일만 수정을 해주면 되겠다.
- 여러 티스토리 블로그 참고



**[특이사항]**
- 호감 상대를 삭제할 때 delete 쿼리가 무시되어 삭제X
 <br/>&rarr; LikeablePersonService 클래스에 `@Transactional(readOnly = true)`
때문
 <br/>&rarr; LikeablePersonService 클래스 안 delete 메서드에 `@Transactional` 추가
- 구글 계정 정보가 전달(권한)X
 <br/>&rarr; scope에 얻고자하는 정보의 문자열을 입력을 해줘야 발급된 
액세스 토큰에 부여된 스코프에 해당하는 권한을 제한적으로 얻을 수 있다.