<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 로그인</title>
	<style>
		#user_id, #user_pw{width:480px;}
	</style>

	<script type="text/javascript">
		$(function(){
			let result = "${result}";
			console.log(result);
			if(result == 1){
				alert("비밀번호가 일치하지 않습니다.");
			}else if(result == 2){
				alert("존재하지 않는 아이디 입니다.");
			}
			
			$('#user_pw').on('keypress', function(e){ 
			    if(e.keyCode == '13'){ 
			        $('#loginBtn').click(); 
			    }
			}); 
			
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
				
				$("#login_form").attr({
					"method" : "post",
					"action" : "/member/login"
				})
				$("#login_form").submit(); 
			})
			
			
			
		})
	</script>
</head>
<body>
	<div class="common-box">
		<div class="login_title">
			<p>로그인</p>
		</div>
		<c:if test="${param.error ne null}">
		    <div class="alert alert-danger">
		        로그인에 실패하였습니다. 올바른 아이디와 비밀번호를 입력하세요.
		        ${param.error }
		    </div>
		</c:if>
		<form id="login_form">
			<div>
				<ul>
					<li>
						<input type="text" id="user_id" name="user_id" class="login_input" placeholder="아이디">
					</li>
					<li>
						<input type="password" id="user_pw" name="user_pw" class="login_input" placeholder="비밀번호">
					</li>
				</ul>
			</div>	
			<input type="hidden" name="${_csrf.parameterName }" value="${csrf.token }">
		
		</form>
		<div class="login_button">
			<button type="button" id="loginBtn">로그인</button>
		</div>
		<div>
			<ul class="join_find">
				<li class="deco">
					<a href="/member/joinFind">아이디/비밀번호 찾기</a>
				</li>
				<li>
					<a href="/member/join">회원가입</a>
				</li>
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
						<img class="" src="/resources/include/images/btnG_완성형.png">
					</a>
				</li>
			</ul>
		</div>
	</div>
</body>
</html>