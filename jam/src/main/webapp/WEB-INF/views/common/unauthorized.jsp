<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script>
	const prevUrl = document.referrer || "/";

	if (confirm("로그인이 필요합니다. 로그인 하시겠습니까?")) {
	    window.location.href = "/member/login?redirect=" + encodeURIComponent(prevUrl);
	} else {
	    window.location.href = prevUrl;
	}
</script>
</head>
<body>
</body>
</html>