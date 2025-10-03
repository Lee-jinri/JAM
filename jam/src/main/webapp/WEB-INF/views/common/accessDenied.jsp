<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script>
	alert("권한이 없는 페이지 입니다.");
	
	if (document.referrer && document.referrer !== location.href) {
	    window.location.href = document.referrer;
	} else {
	    window.location.href = "/"; 
	}

</script>
</head>
<body>
</body>
</html>