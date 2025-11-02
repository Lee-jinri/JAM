<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<!-- 카카오 주소 검색 API -->
<script
	src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<title>JAM-회원가입</title>

<script type="text/javascript">
	$(function(){
		if(window.MY_ID) location.href = "/";
		
		$('#detailAddress').on('keypress', function(e){ 
			if(e.keyCode == '13'){ 
				$('.join_agree').click(); 
			}
		}); 
			
		// 주소 검색 (카카오 주소 API)
		$("#address_search").click(function(){
			new daum.Postcode({
				oncomplete: function(data) {
			            	
					var addr = ''; // 주소 변수
		
					//사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
					if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
						addr = data.roadAddress;
					} else { // 사용자가 지번 주소를 선택했을 경우
		                addr = data.jibunAddress;
		            }
			
		            $("#streetAddress").val(addr);
			            
		            // 커서를 상세주소 필드로 이동한다.
					$("#detailAddress").focus();
				}
			}).open();
		})
			
		/* 아이디/비밀번호/닉네임/전화/이메일 검증 플래그: 유효하면 true, 아니면 false */
		let id_check = false;
		let pw_check = false;
		let name_check = false;
		let phone_check = false;
		let email_check = false;
		
		/* 	아이디, 비밀번호 정규식 : 영문 대,소문자 + 숫자 8 ~ 20자
		닉네임 정규식 : 영문 대,소문자 + 한글 + 숫자 3~10자
		전화번호 정규식 : 01 + 0,1,6,7,8,9 + 0~9 3~4글자 + 0~9 4글자 */
		const idRegex = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{8,20}$/;
		const pwRegex = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{8,20}$/;
		const nameRegex = /^[가-힣a-zA-Z0-9]{3,10}$/;
		const phoneRegex = /^01[016-9]\d{7,8}$/;
		const emailRegex = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/i;
		
		// 아이디 검증
		$("#user_id").on("input", function(){
			const val = $(this).val().trim();
			const $ok = $("#userIdIconOk");
			const $err = $("#userIdIconErr");
			const $hint = $("#userIdHint");
			const $hintErr = $("#userIdHintErr");

			// 값 없음
			if(!val){
				id_check = false;
				showEmpty($ok, $err, $hint, $hintErr);
				return;
			}

			// 형식 틀림
			if(!idRegex.test(val)){
				id_check = false;
				showInvalid($ok, $err, $hint, $hintErr, "아이디는 8~20자 이내로 영문, 숫자를 혼용하여 입력해 주세요.");
				return;
			}
			
			fetch('/api/member/userId/check?userId=' + encodeURIComponent(val))
			.then(res => {
				if(!res.ok) throw new Error("사용할 수 없는 아이디입니다.");
				
				// 사용 가능
				id_check = true;
				showValid($ok, $err, $hint, $hintErr);
			})
			.catch(err=>{
				id_check = false;
				showInvalid($ok, $err, $hint, $hintErr, err.message);
			});
		});
		
		// 비밀번호 검증
		$("#user_pw").on("blur", function(){
			const $ok = $("#passwordIconOk");
			const $err = $("#passwordIconErr");
			
			// 비밀번호 유효성 체크
			if(!pwRegex.test($("#user_pw").val())){
				pw_check = false;
				$ok.hide();
				$err.show();
			} else {
				pw_check = true;
				$ok.show();
				$err.hide();
			}
		});
		
		// 닉네임 검증
		$("#user_name").on("input", function(){
			//FIXME: 이모지 막을 것
			/* 
			let emojiRegex = /[\p{Emoji}]/u;
			if (emojiRegex.test(nickname)) {
				alert("닉네임에 이모지는 사용할 수 없습니다.");
			}
			*/
			const val = $(this).val().trim();
			const $ok = $("#nicknameIconOk");
			const $err = $("#nicknameIconErr");
			const $hint = $("#nicknameHint");
			const $hintErr = $("#nicknameHintErr");

			// 값 없음
			if(!val){
				name_check = false;
				showEmpty($ok, $err, $hint, $hintErr);
				return;
			}

			// 형식 틀림
			if(!nameRegex.test(val)){
				name_check = false;
				showInvalid($ok, $err, $hint, $hintErr, "닉네임은 3~10자 이내로 한글, 영문 또는 숫자를 사용하여 입력해 주세요.");
				return;
			}
			
			fetch('/api/member/userName/check?userName=' + encodeURIComponent(val))
			.then(res => {
				if (!res.ok) throw new Error("사용할 수 없는 닉네임 입니다.");
				
				// 사용 가능
				name_check = true;
				showValid($ok, $err, $hint, $hintErr);
			})
			.catch(err=>{
				name_check = false;
				showInvalid($ok, $err, $hint, $hintErr, err.message);
			});
		});
		
		// 전화번호 검증
		$("#phone").on("input", function () {
			const val = $(this).val().trim();
			const $ok = $("#phoneIconOk");
			const $err = $("#phoneIconErr");
			const $hint = $("#phoneHint");
			const $hintErr = $("#phoneHintErr");
		
			// 값 없음
			if (!val) {
				phone_check = false;
				showEmpty($ok, $err, $hint, $hintErr);
				return;
			}

			// 전화번호 유효성 체크
			if (!phoneRegex.test(val)) {
				phone_check = false;
				showInvalid($ok, $err, $hint, $hintErr, "전화번호 형식이 올바르지 않습니다.");
				return;
			}
		
			// 전화번호 중복 확인
			fetch('/api/member/phone/check?phone=' + encodeURIComponent(val))
			.then(res => {
				if (!res.ok) throw new Error("사용할 수 없는 전화번호입니다.");
				// 사용 가능
				phone_check = true;
				showValid($ok, $err, $hint, $hintErr);
			})
			.catch(err => {
				phone_check = false;
				showInvalid($ok, $err, $hint, $hintErr, err.message);
			});
		});

		// 이메일 검증
		$("#email").on("input", function () {
			const val = $(this).val().trim();
			const $ok = $("#emailIconOk");
			const $err = $("#emailIconErr");
			const $hint = $("#emailHint");
			const $hintErr = $("#emailHintErr");
		
			// 값 없음
			if (!val) {
				email_check = false;
				showEmpty($ok, $err, $hint, $hintErr);
				return;
			}

			// 이메일 형식 체크
			if (!emailRegex.test(val)) {
				email_check = false;
				showInvalid($ok, $err, $hint, $hintErr, "이메일 형식이 올바르지 않습니다.");
				return;
			}
		
			// 중복 확인
			fetch('/api/member/email/check?email=' + encodeURIComponent(val))
			.then(res => {
				if (!res.ok) throw new Error("사용할 수 없는 이메일입니다.");
				// 사용 가능
				email_check = true;
				showValid($ok, $err, $hint, $hintErr);
			})
			.catch(err => {
				console.warn("이메일 확인 오류:", err.message);
				email_check = false;
				showInvalid($ok, $err, $hint, $hintErr, err.message);
			});
		});
		
		// 회원가입 버튼 클릭
		$(".join_agree").click(function(){
			
			let id = $("#user_id").val();
			let pw = $("#user_pw").val();
			let name = $("#user_name").val();
			let phone = $("#phone").val();
			let email = $("#email").val();
			let streetAddress = $("#streetAddress").val();
			let detailAddress = $("#detailAddress").val();
			let address;
			
			if(id.replace(/\s/g,"")==""){
				alert("아이디를 입력하세요.");
				$("#user_id").focus();
				return false;
			} 
			
			if(!id_check){
				alert("아이디를 확인하세요.");
				$("#user_id").focus();
				return false;
			}
			
			if(pw.replace(/\s/g,"")==""){
				alert("비밀번호를 입력하세요.");
				$("#user_pw").focus();
				return false;
			}
			
			if(!pw_check){
				alert("비밀번호를 확인하세요.");
				$("#user_pw").focus();
				return false;
			}
			
			if(name.replace(/\s/g,"")==""){
				alert("닉네임을 입력하세요.");
				$("#user_name").focus();
				return false;
			}
			
			if(!name_check){
				alert("닉네임을 확인하세요.");
				$("#user_name").focus();
				return false;
			}
			
			if(phone.replace(/\s/g,"") == ""){
				alert("전화번호를 입력하세요.");
				$("#phone").focus();
				return false;
			}
			
			if(!phone_check){
				alert("전화번호를 확인하세요.");
				$("#phone").focus();
				return false;
			}
			
			if(email.replace(/\s/g,"") == ""){
				alert("이메일을 입력하세요.");
				$("#email").focus();
				return false;
			}
			
			if(!email_check){
				alert("이메일을 확인하세요.");
				$("#email").focus();
				return false;
			}
			
			if(streetAddress.replace(/\s/g,"") == ""){
				alert("주소를 입력하세요.");
				$("#streetAddress").focus();
				return false;
			}
			else if(detailAddress.replace(/\s/g,"") == ""){
				alert("상세 주소를 입력하세요.");
				$("#detailAddress").focus();
				return false;
			}	
			
			address = streetAddress + " " + detailAddress;
			
			let data = {
					'user_id':id,
					'user_pw':pw,
					'user_name':name,
					'phone':phone,
					'email':email,
					'address':address
			}
			
			
			fetch('/api/member/join',{
				method: 'POST',
				headers: {
				    'Content-Type': 'application/json'
				},
				body: JSON.stringify(data),
			})
			.then(response => {
				if(response.status == 400)  return response.text();
				if(!response.ok) throw new Error('Network response was not ok');
				
				if(confirm("회원 가입이 완료되었습니다. 로그인 페이지로 이동하시겠습니까?")){
					$(location).attr('href','/member/login');
				}else{
					// 회원 가입 이전 페이지로 이동
		        	const prevPage = response.headers.get('prev-page');
			        
			        if (prevPage != null && prevPage != "") {
						if (prevPage.includes("/member/login") || prevPage.includes("/member/join")) {
							$(location).attr('href', "/");
						} else {
							$(location).attr('href', prevPage);
						}
					}else $(location).attr('href', "/");
				}
			})
			.then(data =>{
				console.log(data);
				if(data == 'user_id') {
					alert("올바르지 않은 아이디 형식 입니다. 다시 입력해주세요.");
					$("#user_id").focus();
				}else if (data == 'user_name'){
					alert("올바르지 않은 닉네임 형식 입니다. 다시 입력해주세요.");
					$("#user_name").focus();
				}
			})
			.catch(error =>{
				alert("회원 가입을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
				console.error('회원 가입 중 오류 발생 : ', error);
			})
		});
	})
	
// 검증 성공
function showValid($ok, $err, $hint, $hintErr) {
	$ok.show();
	$err.hide();
	$hint.show();
	$hintErr.hide();
}

// 검증 실패
function showInvalid($ok, $err, $hint, $hintErr, msg) {
	$ok.hide();
	$err.show();
	$hint.hide();
	$hintErr.text(msg).css("color", "red").show();
}

// 입력값 없음
function showEmpty($ok, $err, $hint, $hintErr) {
	$ok.hide();
	$err.hide();
	$hint.show();
	$hintErr.hide();
}

</script>
</head>
<body class="wrap">
	<div class="">
		<div class="common-box my-top-15 my-bottom-15" id="join-box">
			<div class="join_title">
				<p>회원가입</p>
			</div>
			<form id="join_form">
				<div>
					<ul>
						<li>
							<input type="text" id="user_id" name="user_id" class="join_input" placeholder="아이디" autocomplete="username">
							<i id="userIdIconOk" class="fa-solid fa-check join_check" style="color:#03b300;"></i>
							<i id="userIdIconErr" class="fa-solid fa-xmark join_check" style="color:#cc0000;"></i>
							<p class="join_hint" id="userIdHint">아이디는 8~20자 이내로 영문, 숫자를 혼용하여 입력해 주세요.</p>
							<p class="join_hint" id="userIdHintErr" style="display:none"></p>
						</li>
						<li>
							<input type="password" id="user_pw" name="user_pw" class="join_input" placeholder="비밀번호"> 
							<i id="passwordIconOk" class="fa-solid fa-check join_check" style="color:#03b300;"></i>
							<i id="passwordIconErr" class="fa-solid fa-xmark join_check" style="color:#cc0000;"></i>
							<p class="join_hint">비밀번호는 8~20자 이내로 영문 대소문자, 숫자를 혼용하여 입력해주세요.</p>
						</li>
						<li>
							<input type="text" id="user_name" name="user_name" class="join_input" placeholder="닉네임"> 	
							<i id="nicknameIconOk" class="fa-solid fa-check join_check" style="color:#03b300;"></i>
							<i id="nicknameIconErr" class="fa-solid fa-xmark join_check" style="color:#cc0000;"></i>
							<p class="join_hint" id="nicknameHint">닉네임은 3~10자 이내로 한글, 영문 또는 숫자를 사용하여 입력해 주세요.</p>
							<p class="join_hint" id="nicknameHintErr" style="display: none"></p>
						</li>
						<li>
							<input type="text" id="phone" name="phone" class="join_input" placeholder="전화번호" maxlength="11" 
								oninput="this.value = this.value.replace(/[^0-9]/g, '');" />
							<i id="phoneIconOk" class="fa-solid fa-check join_check" style="color:#03b300;"></i>
							<i id="phoneIconErr" class="fa-solid fa-xmark join_check" style="color:#cc0000;"></i>
							<p class="join_hint" id="phoneHint">전화번호는 '-'를 제외한 숫자만 입력해 주세요.</p>
							<p class="join_hint" id="phoneHintErr" style="display: none"></p>
						</li>
						<li>
							<input type="text" id="email" name="email" class="join_input" placeholder="chicken@jam.kr"> 
							<i id="emailIconOk" class="fa-solid fa-check join_check" style="color:#03b300;"></i>
							<i id="emailIconErr" class="fa-solid fa-xmark join_check" style="color:#cc0000;"></i>
							<p class="join_hint" id="emailHint">유효한 이메일 주소를 입력해 주세요.</p>
							<p class="join_hint" id="emailHintErr" style="display: none"></p>
						</li>
						<li>
							<input type="text" id="streetAddress" class="join_input" placeholder="주소" readonly="readonly"> 
							<input type="text" id="detailAddress" class="join_input" placeholder="상세주소 입력">
							<button type="button" id="address_search">검색</button> 
							<input type="hidden" id="address" name="address">
						</li>
					</ul>
				</div>
				<div class="join_button">
					<button type="button" class="join_agree">JAM 회원가입</button>
				</div>
			</form>
		</div>
	</div>
</body>
</html>