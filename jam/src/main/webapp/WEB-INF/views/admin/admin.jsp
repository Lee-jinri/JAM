<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/common.jspf"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM</title>

<script>
	$(function() { 
		const authorization = localStorage.getItem('Authorization');

		if (authorization) {
			fetch('http://localhost:8080/api/member/getUserInfo', {
				method: 'GET',
				headers: {
					'Authorization': authorization
				},
			})
			.then(response => {
				if(!response.ok) throw new Error('Network response was not ok.');
				return response.json();
			})
			.then(data => {				
				if (!data.role.includes("ROLE_ADMIN")) {
					alert("접근 권한이 없는 페이지 입니다.");
					window.location.href = "/";
				}
			})
			.catch(error => {
				console.error('사용자 정보를 가져오는 중 오류 발생:', error);
				alert("시스템 오류입니다. 잠시 후 다시 시도해주세요.");
				window.location.href = "/";
			});
		} else {
			alert("접근 권한이 없는 페이지 입니다.");
			window.location.href = "/";
		}

	});

	
	</script>
</head>
<body class="wrap">
	<header>
		<h1>관리자 페이지</h1>
	</header>
	<nav>
		<ul>
			<li><a href="#">대시보드</a></li>
			<li><a href="#">사용자 관리</a></li>
			<li><a href="#">설정</a></li>
		</ul>
	</nav>
	<main>
		<section>
			<h2>대시보드</h2>
		</section>
		<section>
			<h2>사용자 관리</h2>
		</section>
		<section>
			<h2>설정</h2>
		</section>
	</main>
</body>
</html>