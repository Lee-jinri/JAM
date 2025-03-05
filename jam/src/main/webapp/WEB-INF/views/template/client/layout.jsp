<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ include file="/WEB-INF/views/common/common.jspf" %>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;700&display=swap" rel="stylesheet">	

<html lang="ko">
	<body class="wrap">
		<div id="container">
			<div id="header">
				<tiles:insertAttribute name="header"/>
			</div>
			
			<div id="content">
				<tiles:insertAttribute name="body"/>
			</div>
			
			<div>
				<jsp:include page="/WEB-INF/views/chat/chatRooms.jsp"/>
			</div>
			
			
			<div id="footer">
				<tiles:insertAttribute name="footer"/>
			</div>
		</div>
	</body>
	
	
</html>