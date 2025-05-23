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



.common-box {
  max-width: 480px;
  margin: 80px auto;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}


.login-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  background-color: #f0f3ff;
  border-radius: 30px;
  font-size: 16px;
  outline: none;
  transition: border 0.2s ease;
  margin-bottom: 16px;
}

.login-input::placeholder {
  color: #aaa;
}

.login-input:focus {
  border: 1px solid #a7b8f0; /* 연보라 테두리 */
  background-color: #e8edff; /* 포커스 시 살짝 더 연한 느낌 */
}

#autoLogin {
  margin-right: 6px;
}

</style>
<script type="text/javascript">
$(function(){
	let params = new URLSearchParams(window.location.search);
	let error = params.get("error");
			
	if(error) {
		if (error === "oauth") {
			alert("서버 오류입니다. 잠시 후 다시 시도해주세요.");
		}else if(error === "invalid_state"){
			console.log("error: oauth login invalid state.");
			alert("비정상적인 요청입니다. 다시 시도해주세요.");
		}
	}
			
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
			
			
			
			
	// 비밀번호 입력하고 엔터 누르면 로그인 실행
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
				
		let checkbox = document.getElementById("autoLogin");
		let autoLogin = checkbox.checked;
				
		
		fetch("/api/member/login-process", {
				method: "POST",
				headers: {
				"Content-Type": "application/x-www-form-urlencoded",
			},
			body: new URLSearchParams({
				username: id,
			    password: pw,
			    autoLogin: autoLogin
			  }),
		})
		.then(res => {
			if (res.status === 200){ 
				return res.json();
			}else if(res.status === 401){
				let errorDiv = document.getElementById('error_div'); 
		        errorDiv.innerHTML = '<div class="alert alert-danger">로그인에 실패하였습니다. <br/> 올바른 아이디와 비밀번호를 입력하세요.</div>';
		        return;
		    }else {
		    	let errorDiv = document.getElementById('error_div'); 
				errorDiv.innerHTML = '<div class="alert alert-danger">시스템 오류입니다. 잠시 후 다시 시도해주세요.</div>';
		    }
		})
		.then(data => {
			if (!data) return;
			if (data.redirect) {
				window.location.href = data.redirect;
			}
		})
		.catch((error) => {
			let errorDiv = document.getElementById("error_div");
			errorDiv.innerHTML = '<div class="alert alert-danger">통신 오류가 발생했습니다. 인터넷 연결을 확인해주세요.</div>';
			console.error("로그인 에러:", err);
		});


			/*
			fetch('http://localhost:8080/api/member/login-process', {
				method: 'POST',
				headers : {
					'Content-Type' : 'application/json',
				},
				body : JSON.stringify({
					'username' : id,
					'password' : pw,
					'autoLogin' : autoLogin
				}),
			}) 
			.then((response) => {
			    if (response.ok) {
			    	redirect(response)
			    } else if(response.status === 401){
			    	const errorDiv = document.getElementById('error_div'); 
			        errorDiv.innerHTML = '<div class="alert alert-danger">로그인에 실패하였습니다. <br/> 올바른 아이디와 비밀번호를 입력하세요.</div>';
			    }
			})
			.catch((error) => {
				const errorDiv = document.getElementById('error_div'); 
			    errorDiv.innerHTML = '<div class="alert alert-danger">시스템 오류입니다. 잠시 후 다시 시도해주세요.</div>';
				console.log(error);
			});*/
	})

	// 카카오 로그인
	$("#kakaoLogin").click(function() {
		window.location.href = "/oauth/kakao";
	});

	// 네이버 로그인
	$("#naverLogin").click(function() {
		window.location.href = "/oauth/naver";

	})

})
</script>
</head>
<body class="wrap">
	<div class="common-box">
		<div class="login_title">
			<p>로그인</p>
		</div>
		<div id="error_div" class="text-alignC"></div>
		<form id="login_form">
			<div>
				<ul>
					<li>
						<input type="text" id="user_id" name="user_id" class="login-input" placeholder="아이디" autocomplete="username">
					</li>
					<li>
						<input type="password" id="user_pw" name="user_pw" class="login-input" placeholder="비밀번호" autocomplete="current-password">
					</li>
				</ul>
			</div>
			<div style="height: 30px; text-align: end;">
				<label>
					<input type="checkbox" id="autoLogin">
					<span>로그인 상태 유지</span>
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
				<li id="my-bottom-4">
					<img id="naverLogin" class="socialLoginBtn" src="/resources/include/images/btnG_완성형.png">
				</li>
			</ul>
		</div>
	</div>
</body>
</html>