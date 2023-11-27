<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 로그인</title>
<style>
#user_id, #user_pw {
	width: 480px;
}
</style>

<script type="text/javascript">
		$(function(){
			
			var search = location.search;
			var params = new URLSearchParams(search);
			var error = params.get('error');
			
			if(error == 'error'){
				const errorDiv = document.getElementById('error_div'); 
	            errorDiv.innerHTML = '<div class="alert alert-danger">시스템 오류입니다. 잠시 후 다시 시도해주세요.</div>';
			}
			
			$('#user_pw').on('keypress', function(e){ 
			    if(e.keyCode == '13'){ 
			        $('#loginBtn').click(); 
			    }
			}); 
			
			// 일반 로그인
			$("#loginBtn").click(function(){
				let id = $("#user_id").val();
				let pw = $("#user_pw").val();
				
				if(id.replace(/\s/g,"")==""){
					alert("아이디를 입력하세요.");
					$("#user_id").focus();
					return false;
				}
				
				if(pw.replace(/\s/g,"")==""){
					alert("비밀번호를 입력하세요.");
					$("#user_pw").focus();
					return false;
				}
				
				fetch('http://localhost:8080/member/login-process', {
					method: 'POST',
					headers : {
						'Content-Type' : 'application/json',
					},
					body : JSON.stringify({ // 바디에 담긴 정보를 json 으로 변환해서 요청
						'user_id' : id,
						'user_pw' : pw,
					}),
				}) 
				.then((response) => {
				    if (response.status === 200) {
				        
				        const Authorization = response.headers.get('Authorization');
				        
				        if(Authorization != null && Authorization != ""){
				        	// jwt 토큰 로컬 스토리지에 저장
				        	localStorage.setItem('Authorization', Authorization);
				        	
				        	// 로그인 이전 페이지로 이동
				        	const prevPage = response.headers.get('prev-page');
					        
					        if (prevPage != null && prevPage != "") {
								// 회원가입에서 로그인으로 넘어온 경우 "/"로 redirect
								if (prevPage.includes("/member/join")) {
									$(location).attr('href', "/");
								} else {
									$(location).attr('href', prevPage);
								}
							}else $(location).attr('href', "/");
				        }
				        return null;
				        
				    } else if(response.status === 401){
				    	const errorDiv = document.getElementById('error_div'); 
			            errorDiv.innerHTML = '<div class="alert alert-danger">로그인에 실패하였습니다. 올바른 아이디와 비밀번호를 입력하세요.</div>';
			            return response.text();
				    } else {
				    	const errorDiv = document.getElementById('error_div'); 
			            errorDiv.innerHTML = '<div class="alert alert-danger">시스템 오류입니다. 잠시 후 다시 시도해주세요.</div>';
			            return response.text();
				    }
				     
				})
				.then((body) => {
					if(body != null) console.log(body);
				})
				.catch((error) => console.log("error" , error));
			})
			
			// 네이버 로그인
			$("#naverLogin").click(function(){
				fetch('http://localhost:8080/member/naver_oauth', {
					method: 'GET'
				}) 
				.then((response) => {
				    if (response.status === 200) {
				        const Authorization = response.headers.get('Authorization');
				        
				        if(Authorization != null && Authorization != ""){
				        	// jwt 토큰 로컬 스토리지에 저장
				        	localStorage.setItem('Authorization', Authorization);
				        	
				        	// 로그인 이전 페이지로 이동
				        	const prevPage = response.headers.get('prev-page');
					        
					        if (prevPage != null && prevPage != "") {
								// 회원가입에서 로그인으로 넘어온 경우 "/"로 redirect
								if (prevPage.includes("/member/join")) {
									$(location).attr('href', "/");
								} else {
									$(location).attr('href', prevPage);
								}
							}else $(location).attr('href', "/");
				        }
				        return null;
				        
				    } else if(response.status === 401){
				    	const errorDiv = document.getElementById('error_div'); 
			            errorDiv.innerHTML = '<div class="alert alert-danger">로그인에 실패하였습니다. 올바른 아이디와 비밀번호를 입력하세요.</div>';
			            return response.text();
				    } else {
				    	const errorDiv = document.getElementById('error_div'); 
			            errorDiv.innerHTML = '<div class="alert alert-danger">시스템 오류입니다. 잠시 후 다시 시도해주세요.</div>';
			            return response.text();
				    }
				     
				})
				.then((body) => {
					if(body != null) console.log(body);
				})
				.catch((error) => console.log("error" , error));
			})
			
			
			// 카카오 로그인
			
		})
	</script>
</head>
<body class="wrap">
	<div class="common-box">
		<div class="login_title">
			<p>로그인</p>
		</div>
		<div id="error_div"></div>
		<form id="login_form">
			<div>
				<ul>
					<li>
						<input type="text" id="user_id" name="user_id" class="login_input" placeholder="아이디" autocomplete="username">
					</li>
					<li>
						<input type="password" id="user_pw" name="user_pw" class="login_input" placeholder="비밀번호" autocomplete="current-password">
					</li>
				</ul>
			</div>

		</form>
		<div class="login_button">
			<button type="button" id="loginBtn">로그인</button>
		</div>
		<div>
			<ul class="join_find">
				<li class="deco"><a href="/member/joinFind">아이디/비밀번호 찾기</a></li>
				<li><a href="/member/join">회원가입</a></li>
			</ul>
		</div>
		<div>
			<p class="login_sns_p">다른 서비스 계정으로 로그인</p>
			<ul class="login_sns">
				<li>
					<a href="/member/kakao_oauth"> 
						<img class="" src="/resources/include/images/kakao_login_large_wide.png">
					</a>
				</li>
				<li class="pd-top1">
					<a href="/member/naver_oauth"> 
						<img class="" id="naverLogin" src="/resources/include/images/btnG_완성형.png">
					</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>