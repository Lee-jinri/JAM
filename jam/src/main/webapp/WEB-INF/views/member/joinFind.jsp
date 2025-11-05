<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 아이디/비밀번호 찾기</title>

	<script>
		$(function(){
			$("#find_id_btn").click(function(){
				
				var email = $("#id_email").val();
				var phone = $("#id_phone").val();
				
				const emailRegex = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/i;
				const phoneRegex = /^01[016-9]\d{7,8}$/;
				
				if(email.replace(/\s/g, "") == ""){
					alert("이메일을 입력하세요.");
					$("#id_email").focus();
					return false;
				}
				
				if(phone.replace(/\s/g,"") == ""){
					alert("핸드폰 번호를 입력하세요.");
					$("#id_phone").focus();
					return false;
				}
				
				if(!emailRegex.test(email)){
					alert("올바른 이메일 형식이 아닙니다.");
					return;
				}
				
				if(!phoneRegex.test(phone)){
					alert("전화번호 형식이 올바르지 않습니다.");
					return;
				}
				
				fetch('/api/member/id/find', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					credentials: 'same-origin',
					body: JSON.stringify({ email, phone })
				})
			    .then(response => {
			        if (!response.ok) {
			        	throw new Error("회원정보를 찾을 수 없습니다.");
			        }
			        return response.text();
			    })
			    .then(result => {
			        if (result) {
			            alert('회원님의 아이디는 ' + result + '입니다.');
			            const login = confirm("로그인 페이지로 이동하시겠습니까?");
			            if (login) {
			                location.href = "/member/login";
			            }
			        } else {
			            alert("회원정보를 찾을 수 없습니다.");
			        }
			    })
			    .catch(err=>{
			    	alert(err.message || '잠시 후 다시 시도해 주세요.');
				});
			})
			
			$("#find_pw_btn").click(function(){
				
				var user_id = $("#pw_id").val();
				var email = $("#pw_email").val();
				var phone = $("#pw_phone").val();
				
				if(user_id.replace(/\s/g, "") == ""){
					alert("아이디를 입력하세요.");
					$("#pw_id").focus();
					return false;
				}
				
				if(email.replace(/\s/g, "") == ""){
					alert("이메일을 입력하세요.");
					$("#pw_email").focus();
					return false;
				}
				
				if(phone.replace(/\s/g,"") == ""){
					alert("핸드폰 번호를 입력하세요.");
					$("#pw_phone").focus();
					return false;
				}
				
				fetch('/api/member/password/temp', {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({
						user_id: user_id,
						email: email,
						phone: phone
					}),
				})
				.then(response => {
					alert("메일로 임시 비밀번호를 발송했습니다. 로그인 후 비밀번호를 변경하세요.");
					
					let login = confirm("로그인 페이지로 이동하시겠습니까?");
					if(login) location.href = "/member/login";
				})
				.catch(error => {
					alert('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
					console.error('Error:', error);
				})
			})
		})
	</script>
</head>
<body class="wrap">
	<div class="my-top-15 my-bottom-15 rem-20">
		<div class="contents">
			<div class="info_title">
				<p class="">아이디 / 비밀번호 찾기</p>
			</div>
			<div class="justify-space-around flex">
				
				<div class="find_id my-7">
					<p class="find_title">아이디 찾기</p>
					<div>
						<div>
							<div class="inline-block width_80px my-4">
								<span>이메일</span>
							</div>
							<div class="inline-block">
								<input type="text" id="id_email" name="email" placeholder="chicken@jam.kr">
							</div>
						</div>
						<div>
							<div class="inline-block width_80px my-4">
								<span>핸드폰 번호</span>
							</div>
							<div class="inline-block">
								<input type="number" id="id_phone" name="phone" placeholder="'-'없이 입력">
							</div>
						</div>	
					</div>
					<div class="findID_div my-4">
						<button type="button" id="find_id_btn" class="border-none">아이디 찾기</button>
					</div>
				</div>
				
				<div class="find_pw my-7">
					<p class="find_title">비밀번호 찾기</p>
					<div>
						<div class="inline-block width_80px my-4">
							<span class="find_span">아이디</span>
						</div>
						<div class="inline-block">
							<input type="text" id="pw_id" name="user_id">
						</div>
					</div>	
					<div>
						<div class="inline-block width_80px my-4">
							<span class="find_span">이메일</span>
						</div>	
						<div class="inline-block">
							<input type="text" id="pw_email" name="email" placeholder="chicken@jam.kr">
						</div>
					</div>	
					<div>
						<div class="inline-block width_80px my-4">
							<span class="find_span">핸드폰 번호</span>
						</div>
						<div class="inline-block">
							<input type="text" id="pw_phone" name="phone" placeholder="'-'없이 입력">
						</div>
					</div>	
					<div class="findPw_div my-4">
						<button type="button" id="find_pw_btn" class="border-none">비밀번호 찾기</button>
					</div>
						
				</div>
			</div>
		</div>
	</div>
</body>
</html>