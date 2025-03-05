<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>

<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta charset="UTF-8">
<title>JAM - 로그인</title>
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
	<!-- 카카오 로그인 SDK -->
	<script src="https://developers.kakao.com/sdk/js/kakao.js"></script>
  	<!-- 네이버 로그인 -->
  	<script type="text/javascript" src="https://static.nid.naver.com/js/naveridlogin_js_sdk_2.0.2.js" charset="utf-8"></script>
  	


<style>
#user_id, #user_pw {
	width: 480px;
}
</style>
<script type="text/javascript">
		$(function(){
			
			
			// Jwt 토큰 헤더에서 가져와서 로컬 스토리지에 저장 후 로그인 이전 페이지로 이동하는 함수
			function redirect(response){
				const prevPage = response.headers.get('prev-page');
			       
			    if (prevPage != null && prevPage != "") {
					// 회원가입에서 로그인으로 넘어온 경우 메인 페이지로 redirect
					if (prevPage.includes("/member/join")) {
						$(location).attr('href', "/");
					} else {
						$(location).attr('href', prevPage);
					}
				}else $(location).attr('href', "/");
			}
			
			
			
			
			// 비밀번호 입력하고 엔터 눌렀을 때 로그인 실행
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
					console.log(id);
					alert("아이디를 입력하세요.");
					$("#user_id").focus();
					return false;
				}
				
				if(pw.replace(/\s/g,"")==""){
					alert("비밀번호를 입력하세요.");
					$("#user_pw").focus();
					return false;
				}
				
				let checkbox = document.getElementById("autoLogin");
				let autoLogin = checkbox.checked;
				
				fetch('http://localhost:8080/api/member/login-process', {
					method: 'POST',
					headers : {
						'Content-Type' : 'application/json',
					},
					body : JSON.stringify({
						'user_id' : id,
						'user_pw' : pw,
						'autoLogin' : autoLogin
					}),
				}) 
				.then((response) => {
				    if (response.ok) {
				    	redirect(response)
				    } else if(response.status === 401){
				    	const errorDiv = document.getElementById('error_div'); 
			            errorDiv.innerHTML = '<div class="alert alert-danger">로그인에 실패하였습니다. 올바른 아이디와 비밀번호를 입력하세요.</div>';
				    }
				})
				.catch((error) => {
					const errorDiv = document.getElementById('error_div'); 
		            errorDiv.innerHTML = '<div class="alert alert-danger">시스템 오류입니다. 잠시 후 다시 시도해주세요.</div>';
					console.log(error);
				});
			})
			
			
			let clientId = "<spring:eval expression="@environment.getProperty('social.client_id')" />";
	        
			var naverLogin = new naver.LoginWithNaverId({
				clientId: clientId,
				callbackUrl: "http://localhost:8080/member/naver_login",
				isPopup: false, 
				loginButton: {color: "green", type: 3, height: 52.5}
			});
				
			naverLogin.init();
			
			// 네이버 로그인 이미지 변경
		  	$("#naverIdLogin a img").attr("class", "socialLoginBtn");
		  	$("#naverIdLogin a img").attr("src","/resources/include/images/btnG_완성형.png");
		  	
		  	
			// 카카오 로그인
			
			// Kakao API 초기화
			let kakaoKey = "<spring:eval expression="@environment.getProperty('social.kakao_key')" />";
			
		    Kakao.init(kakaoKey);
			Kakao.isInitialized();
			
			$("#kakaoLogin").click(function() {
				Kakao.Auth.login({
					success: function (authObj) {
						Kakao.Auth.setAccessToken(authObj.access_token); //access 토큰 값 저장
						getKakaoInfo();
					},
					fail: function (error) {
						const errorDiv = document.getElementById('error_div'); 
			            errorDiv.innerHTML = '<div class="alert alert-danger">시스템 오류입니다. 잠시 후 다시 시도해주세요.</div>';
						console.log(error);
					},
				});
			});

			function getKakaoInfo() {
				Kakao.API.request({
					url: "/v2/user/me",
					success: function (res) {
						var id = res.id;
						var nickname = res.kakao_account.profile.nickname;
		            	var email = res.kakao_account.email;
						
		            	kakaoLogin(id, nickname, email);
					},
					fail: function (error) {
						alert("카카오 로그인 실패" + JSON.stringify(error));
					},
				});
			}
		      
			function kakaoLogin(id, nickname, email){
				fetch('/api/member/kakao_login',{
					method: 'POST',
					headers: {
						'Content-Type':'application/json'
					},
					body: JSON.stringify({
						'user_id': id,
						'user_name': nickname,
						'email': email
					})
				}).then(response =>{
					
					redirect(response);
				})
			}
		      
			
			
			
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
			<div style="height: 30px; text-align: end;">
				<label>
					<input type="checkbox" id="autoLogin">
					로그인 상태 유지
				</label>
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
				<li class="my-bottom-4">
					<img id="kakaoLogin" class="socialLoginBtn" src="/resources/include/images/kakao_login_large_wide.png">
				</li>
				<li id="naverIdLogin">
					
				</li>
			</ul>
		</div>
	</div>
</body>
</html>