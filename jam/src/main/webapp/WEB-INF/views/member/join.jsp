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
			
			
			
		/* 아이디, 닉네임, 핸드폰 번호 비밀번호 유효성 체크 변수 / false = 확인, true = 중복 or 사용불가 */
		let id_check = false;
		let pw_check = false;
		let name_check = false;
		let phone_check = false;
		
		
		/* 	아이디, 비밀번호 정규식 : 영문 대,소문자 + 숫자 8 ~ 20자
		닉네임 정규식 : 영문 대,소문자 + 한글 + 숫자 2 ~ 10자
		전화번호 정규식 : 01 + 0,1,6,7,8,9 + 0~9 3~4글자 + 0~9 4글자 */
		let id_legExp = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{8,20}$/;
		let pw_legExp = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{8,20}$/;
		let name_legExp = /^.{3,10}$/;
		let phone_legExp = /^01([0|1|6|7|8|9])([0-9]{3,4})([0-9]{4})$/;
		let email_letExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
		
		
		/********************아이디 **********************/
		$("#user_id").on("blur", function(){
			var userId = $("#user_id").val();	
			
			// 아이디 유효성 체크
			if(!id_legExp.test(userId)){
				$("#id_check2").css("display","inline-block");
				$('#id_check1').css("display","none");
			}else{
				// 아이디 중복 확인
				$("#id_check2").css("display","none");
				
				fetch('/api/member/userId/check?userId=' + userId, {
				    method: 'GET',
				})
				.then(response => {
				    if (response.ok) {
				        // 성공 처리
				        id_check = true;
				        document.getElementById('id_check1').style.display = "inline-block";
				        document.getElementById('id_check2').style.display = "none";
				        document.getElementById('id_hint1').style.display = "inline-block";
				        document.getElementById('id_hint2').style.display = "none";
				    } else if (response.status === 409) {
				        // 중복 ID 에러 처리
				        id_check = false;
				        document.getElementById('id_check1').style.display = "none";
				        document.getElementById('id_check2').style.display = "inline-block";
				        document.getElementById('id_hint1').style.display = "none";
				        document.getElementById('id_hint2').style.display = "inline-block";
				        document.getElementById('id_hint2').style.color = "red";
				    } else {
				    	return response.text().then(errMessage => {
			            	throw new Error('Error: ${response.status}, Message: ${errMessage}');
			        	});
				    }
				})
				.catch(error => {
				    console.error("Error:", error);
				    alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				});
			}
	   	});
		
		
		/******************* 비밀번호 ******************/
		$("#user_pw").on("blur", function(){
			// 비밀번호 유효성 체크
			if(!pw_legExp.test($("#user_pw").val())){
				$("#pw_check2").css("display","inline-block");
				$('#pw_check1').css("display","none");
				pw_check = false;
			}else {
				$('#pw_check1').css("display","inline-block");
				$("#pw_check2").css("display","none");
				pw_check = true;
			}
		});
		
		
		/******************* 닉네임 *********************/
		$("#user_name").on("blur", function(){
			// 닉네임 유효성 체크
			if(!name_legExp.test($("#user_name").val())){
				$("#name_check2").css("display","inline-block");
				$("#name_check1").css("display","none");
			}else{
				// 닉네임 중복 확인
				var user_name = $("#user_name").val();
				
				fetch('/api/member/userName/check?userName='+user_name, {
			        method: 'GET',
			    })
			    .then(response => {
			    	if(response.ok){
			    		name_check = true;
						$('#name_check1').css("display","inline-block");
						$("#name_check2").css("display","none");
						$("#name_hint1").css("display","inline-block");
						$("#name_hint2").css("display","none");	
			    	}else if (response.status === 409) {
			        	name_check = false;
						$('#name_check1').css("display","none");
						$("#name_check2").css("display","inline-block");
						$("#name_hint1").css("display","none");
						$("#name_hint2").css("display","inline-block");
						$("#name_hint2").css("color","red");
			        } else {
				    	return response.text().then(errMessage => {
			            	throw new Error('Error: ${response.status}, Message: ${errMessage}');
			        	});
				    }
				})
				.catch(error => {
				    console.error("Error:", error);
				    alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				});
			    
			}
		});
		
		
		/******************* 핸드폰 번호 *********************/
		$("#phone").on("blur", function(){
			// 핸드폰 번호 유효성 체크
			if(!phone_legExp.test($("#phone").val())){
				$("#phone_check2").css("display","inline-block");
				$("#phone_check1").css("display","none");
			}else{
				// 핸드폰 번호 중복 확인
				var phone = $("#phone").val();
				
				fetch('/api/member/phone/check?phone='+phone,{
					method: 'GET',
				})
				.then(response => {
					if(response.ok){
						phone_check = true;
						$('#phone_check1').css("display","inline-block");
						$("#phone_check2").css("display","none");
						$("#phone_hint1").css("display","inline-block");
						$("#phone_hint2").css("display","none");	
					}else if(response.status === 409){
						phone_check = false;
						$('#phone_check1').css("display","none");
						$("#phone_check2").css("display","inline-block");
						$("#phone_hint1").css("display","none");
						$("#phone_hint2").css("display","inline-block");
						$("#phone_hint2").css("color","red");
					} else {
				    	return response.text().then(errMessage => {
			            	throw new Error('Error: ${response.status}, Message: ${errMessage}');
			        	});
				    }
				})
				.catch(error => {
				    console.error("Error:", error);
				    alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				});
				 
			}
		});
		
		/******************* 이메일 *********************/
		$("#email").on("blur", function(){
			// 이메일 유효성 체크
			if(!email_letExp.test($("#email").val())){
				$("#email_check2").css("display","inline-block");
				$("#email_check1").css("display","none");
			}else{
				// 이메일 중복 확인
				var email = $("#email").val();
				
				fetch('/api/member/email/check?email='+email,{
					method: 'GET',
				})
				.then(response =>{
					if(response.ok){
						email_check = true;
						$('#email_check1').css("display","inline-block");
						$("#email_check2").css("display","none");
						$("#email_hint1").css("display","inline-block");
						$("#email_hint2").css("display","none");
					}else if(response.status === 409){
						email_check = false;
						$('#email_check1').css("display","none");
						$("#email_check2").css("display","inline-block");
						$("#email_hint1").css("display","none");
						$("#email_hint2").css("display","inline-block");
						$("#email_hint2").css("color","red");
			        }else {
				    	return response.text().then(errMessage => {
			            	throw new Error('Error: ${response.status}, Message: ${errMessage}');
			        	});
				    }
				})
				.catch(error => {
				    console.error("Error:", error);
				    alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				});
				 
			}
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
				alert("전화번호를 입력하세요.");
				$("#phone").focus();
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
						if (prevPage.includes("/member/login") || pravPage.includes("/member/join")) {
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
				//$(location).attr('href','/');
				
				console.error('회원 가입 중 오류 발생 : ', error);
			})
		});
		
	})
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
							<input type="text" id="user_id" name="user_id" class="join_input" placeholder="아이디"> 
							<img class="join_check" id="id_check1" alt="check" src="/resources/include/images/checked.svg"> 
							<img class="join_check" id="id_check2" alt="error" src="/resources/include/images/checked2.svg">
							<p class="join_hint" id="id_hint1">아이디는 8~20자 이내로 영문, 숫자를 혼용하여 입력해 주세요.</p>
							<p class="join_hint" id="id_hint2" style="display: none">이미 사용중인 아이디 입니다.</p>
						</li>
						<li>
							<input type="password" id="user_pw" name="user_pw" class="join_input" placeholder="비밀번호"> 
							<img class="join_check" id="pw_check1" alt="check" src="/resources/include/images/checked.svg"> 
							<img class="join_check" id="pw_check2" alt="error" src="/resources/include/images/checked2.svg">
							<p class="join_hint">비밀번호는 8~20자 이내로 영문 대소문자, 숫자를 혼용하여 입력해주세요.</p>
						</li>
						<li>
							<input type="text" id="user_name" name="user_name" class="join_input" placeholder="닉네임"> 
							<img class="join_check" id="name_check1" alt="check" src="/resources/include/images/checked.svg"> 
							<img class="join_check" id="name_check2" alt="error" src="/resources/include/images/checked2.svg">
							<p class="join_hint" id="name_hint1">닉네임은 2~10자 이내로 한글, 영문 또는 숫자를 사용하여 입력해 주세요.</p>
							<p class="join_hint" id="name_hint2" style="display: none">이미 사용중인 닉네임 입니다.</p></li>
						<li>
							<input type="text" id="phone" name="phone" class="join_input" placeholder="핸드폰 번호" maxlength="11" 
								oninput="this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');" />
							<img class="join_check" id="phone_check1" alt="check" src="/resources/include/images/checked.svg"> 
							<img class="join_check" id="phone_check2" alt="error" src="/resources/include/images/checked2.svg">
							<p class="join_hint" id="phone_hint1">핸드폰 번호는 '-'를 제외한 숫자만 입력해 주세요.</p>
							<p class="join_hint" id="phone_hint2" style="display: none">이미 사용중인 핸드폰 번호 입니다.</p>
						</li>
						<li>
							<input type="text" id="email" name="email" class="join_input" placeholder="chicken@jam.kr"> 
							<img class="join_check" id="email_check1" alt="check" src="/resources/include/images/checked.svg"> 
							<img class="join_check" id="email_check2" alt="error" src="/resources/include/images/checked2.svg">
							<p class="join_hint" id="email_hint1">유효한 이메일 주소를 입력해 주세요.</p>
							<p class="join_hint" id="email_hint2" style="display: none">이미 사용중인 이메일 입니다.</p>
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