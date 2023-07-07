<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<head>
	<!-- 카카오 주소 검색 API -->
	<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
	
	<title>JAM-회원가입</title>
	<style>
	.join_check{
		width:23px;
		height:20px;
		margin-left:5px;
		display: none;
	}
	
	
	</style>

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
			let id_check = true;
			let pw_check = true;
			let name_check = true;
			let phone_check = true;
			
			
			/* 	아이디, 비밀번호 정규식 : 영문 대,소문자 + 숫자 8 ~ 20자
			닉네임 정규식 : 영문 대,소문자 + 한글 + 숫자 3 ~ 8자
			전화번호 정규식 : 0,1 3글자 + 0~9 8글자 */
			let id_legExp = /^[a-zA-Z]+[0-9a-zA-Z]{7,19}$/;
			let pw_legExp = /^[a-zA-Z]+[0-9a-zA-Z]{7,19}$/;
			let name_legExp = /^[a-zA-z가-힣0-9]{2,7}$/;
			let phone_legExp = /^01([0|1|6|7|8|9])([0-9]{3,4})([0-9]{4})$/;
			
			/********************아이디 **********************/
			$("#user_id").on("propertychange change keyup paste input", function(){
				var userId = $("#user_id").val();	
				
				// 아이디 유효성 체크
				if(!id_legExp.test(userId)){
					$("#id_check2").css("display","inline-block");
					$('#id_check1').css("display","none");
				}else{
					// 아이디 중복 확인
					$("#id_check2").css("display","none");
					
							
					
					$.ajax({
						type : "post",
						url : "/member/memberIdChk",
						data : {userId : userId}, 	// '컨트롤에 넘길 데이터 이름' : '데이터(.userId에 입력되는 값)'
						success : function(result){
							if(result != 'fail'){
								id_check = false;
								$('#id_check1').css("display","inline-block");
								$("#id_check2").css("display","none");
								$("#id_hint1").css("display","inline-block");
								$("#id_hint2").css("display","none");
							} else {
								id_check = true;
								$('#id_check1').css("display","none");
								$("#id_check2").css("display","inline-block");
								$("#id_hint1").css("display","none");
								$("#id_hint2").css("display","inline-block");
								$("#id_hint2").css("color","red");
							}		
						}
					}); 
				}
		   	});
			
			
			/******************* 비밀번호 *********************/
			$("#user_pw").on("propertychange change keyup paste input", function(){
				// 비밀번호 유효성 체크
				if(!pw_legExp.test($("#user_pw").val())){
					$("#pw_check2").css("display","inline-block");
					$('#pw_check1').css("display","none");
					pw_check = true;
				}else {
					$('#pw_check1').css("display","inline-block");
					$("#pw_check2").css("display","none");
					pw_cehck = false;
				}
			});
			
			/******************* 닉네임 *********************/
			$("#user_name").on("propertychange change keyup paste input", function(){
				// 닉네임 유효성 체크
				if(!name_legExp.test($("#user_name").val())){
					$("#name_check2").css("display","inline-block");
					$("#name_check1").css("display","none");
				}else{
					// 닉네임 중복 확인
					
					var user_name = $("#user_name").val();
					
					$.ajax({
						type : "post",
						url : "/member/memberNameChk",
						data : {user_name : user_name},
						success : function(result){
							if(result != 'fail'){
								name_check = false;
								$('#name_check1').css("display","inline-block");
								$("#name_check2").css("display","none");
								$("#name_hint1").css("display","inline-block");
								$("#name_hint2").css("display","none");
							} else {
								name_check = true;
								$('#name_check1').css("display","none");
								$("#name_check2").css("display","inline-block");
								$("#name_hint1").css("display","none");
								$("#name_hint2").css("display","inline-block");
								$("#name_hint2").css("color","red");
							}		
						}
					}); 
				}
			});
			
			
			/******************* 핸드폰 번호 *********************/
			$("#phone").on("propertychange change keyup paste input", function(){
				// 핸드폰 번호 유효성 체크
				if(!phone_legExp.test($("#phone").val())){
					$("#phone_check2").css("display","inline-block");
					$("#phone_check1").css("display","none");
				}else{
					// 핸드폰 번호 중복 확인
					var phone = $("#phone").val();
					
					$.ajax({
						type : "post",
						url : "/member/memberPhoneChk",
						data : {phone : phone},
						success : function(result){
							if(result != 'fail'){ // 사용 가능한 핸드폰 번호
								phone_check = false;
								$('#phone_check1').css("display","inline-block");
								$("#phone_check2").css("display","none");
								$("#phone_hint1").css("display","inline-block");
								$("#phone_hint2").css("display","none");
							} else { // 사용 불가능한 핸드폰 번호
								phone_check = true;
								$('#phone_check1').css("display","none");
								$("#phone_check2").css("display","inline-block");
								$("#phone_hint1").css("display","none");
								$("#phone_hint2").css("display","inline-block");
								$("#phone_hint2").css("color","red");
							}		
						}
					}); 
				}
			});
			
			
			// 회원가입 버튼 클릭
			$(".join_agree").click(function(){
				
				let id = $("#user_id").val();
				let pw = $("#user_pw").val();
				let name = $("#user_name").val();
				let phone = $("#phone").val();
				let streetAddress = $("#streetAddress").val();
				let detailAddress = $("#detailAddress").val();
				
				
				if(id.replace(/\s/g,"")==""){
					alert("아이디를 입력하세요.");
					$("#user_id").focus();
					return false;
				} 
				
				if(id_check){
					alert("아이디를 확인하세요.");
					$("#user_id").focus();
					return false;
				}
				
				if(pw.replace(/\s/g,"")==""){
					alert("비밀번호를 입력하세요.");
					$("#user_pw").focus();
					return false;
				}
				
				if(name.replace(/\s/g,"")==""){
					alert("닉네임을 입력하세요.");
					$("#user_name").focus();
					return false;
				}
				
				if(phone.replace(/\s/g,"") == ""){
					alert("전화번호를 입력하세요.");
					$("#phone").focus();
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
				$("#address").val($("#streetAddress").val() + " " + $("#detailAddress").val());
				
				$("#join_form").attr({
					"method" : "post",
					"action" : "/member/join"
				})
				$("#join_form").submit();
			});
			
		})
	</script>
</head>
<body>
	<div class="common-box" id="join-box">
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
						<p class="join_hint" id="id_hint1">아이디는 8~20자 이내로 영문, 숫자를 혼용하여 입력해 주세요. </p>
						<p class="join_hint" id="id_hint2" style="display : none">이미 사용중인 아이디 입니다.</p>
					</li>
					<li>
						<input type="text" id="user_pw" name="user_pw" class="join_input" placeholder="비밀번호">
						<img class="join_check" id="pw_check1" alt="check" src="/resources/include/images/checked.svg">
						<img class="join_check" id="pw_check2" alt="error" src="/resources/include/images/checked2.svg">
						<p class="join_hint">비밀번호는 8~20자 이내로 영문 대소문자, 숫자를 혼용하여 입력해 주세요.</p>
					</li>
					<li>
						<input type="text" id="user_name" name="user_name" class="join_input" placeholder="닉네임">
						<img class="join_check" id="name_check1" alt="check" src="/resources/include/images/checked.svg">
						<img class="join_check" id="name_check2" alt="error" src="/resources/include/images/checked2.svg">
						<p class="join_hint" id="name_hint1">닉네임은 2~6자 이내로 한글, 영문 또는 숫자를 사용하여 입력해 주세요.</p>
						<p class="join_hint" id="name_hint2" style="display : none">이미 사용중인 닉네임 입니다.</p>
					</li>
					<li>
						<input type="text" id="phone" name="phone" class="join_input" placeholder="핸드폰 번호" maxlength="11" 	
						oninput="this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');" />
						<img class="join_check" id="phone_check1" alt="check" src="/resources/include/images/checked.svg">
						<img class="join_check" id="phone_check2" alt="error" src="/resources/include/images/checked2.svg">
						<p class="join_hint" id="phone_hint1">핸드폰 번호는 '-'를 제외한 숫자만 입력해 주세요.</p>
						<p class="join_hint" id="phone_hint2" style="display : none">이미 사용중인 핸드폰 번호 입니다.</p>
					</li>
					<li>
						<input type="text" id="streetAddress" class="join_input" placeholder="주소" readonly="readonly">
						<input type="text" id="detailAddress" class="join_input" placeholder="상세주소 입력" >
						<button type="button" id="address_search">검색</button>
						
						<input type="hidden" id="address" name="address">
					</li>
					
					
				</ul>
			</div>
		</form>
		<div class="join_button">
			<button type="button" class="join_agree" >JAM 회원가입</button>
		</div>
	</div>		
</body>
