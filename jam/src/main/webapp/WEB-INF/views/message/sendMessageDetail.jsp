<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - message</title>
</head>
<body>
	<div class="contents">
		<div class="title">
			<span>${detail.message_title }</span>
			<span>받는 사람</span>
			<span>${detail.receiver }</span>
			<span>(${detail.receiver_id })</span>
			<span>${detail.sendTime }</span>
		</div>
		<div>
			${detail.message_contents }
		</div>
		<div class="btn">
			<button type="button">목록</button>
		</div>
	</div>
<p>
</p>

</body>
</html>