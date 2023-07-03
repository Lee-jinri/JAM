<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>JAM</title>
	
	<style>
		#join_f1{
			margin : 50px 0 25px 0;
		}
		#join_f2{
			margin : 0 0 25px 0;
		}
		#join_f3{
			font-size : 20px;
			margin : 0 0 40px 0; 
		}
		.login_btn {
			width: 280px;
    		height: 50px;
		    color: #fff;
			background-color: #00C3FF;
		    text-align: center;
		    font-size: 18px;
			border-radius: 13px;
			font-weight: bold;
			margin-bottom : 15px;
		}
		#naver_login{
			background-color:#03CF5D;
		}
		
		#naver{
			font-family: 'Black Han Sans', sans-serif;
			font-weight: normal;
		}
	</style>
	<script type="text/javascript">
		$(function(){
			$("#login_btn").click(function(){
				location.href = "/member/loginPage";
			})
		})
	</script>
</head>
<body>
	<div>
		<h2 class="text-center" id="join_f1" class="font-weight-bold">회원가입 완료!</h2>
		<h3 class="text-center" id="join_f2">JAM 회원이 되신 것을 환영해요.</h3>
		<p class="text-center" id="join_f3">로그인하면 더 많은 서비스를 이용할 수 있어요.</p>
		<div class="text-center">
			<button type="button" class="login_btn" id="jam_login" >아이디로 로그인</button><br/>
			<button type="button" class="login_btn" id="naver_login" ><span id="naver">NAVER</span> 로그인</button>
		</div>
			
		
	</div>
</body>
</html>