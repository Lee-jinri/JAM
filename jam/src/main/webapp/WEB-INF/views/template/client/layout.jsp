<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" session="false" buffer="32kb" autoFlush="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ include file="/WEB-INF/views/common/common.jspf" %>

<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;700&display=swap" rel="stylesheet">	

<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400..900;1,400..900&display=swap" rel="stylesheet">

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="rawUri" value="${pageContext.request.requestURI}" />

<%-- forward(내부 디스패치)된 경우 원래 URI를 사용하도록 --%>
<c:set var="fwdUri" value="${requestScope['javax.servlet.forward.request_uri']}" />
<c:set var="uri" value="${empty fwdUri ? rawUri : fwdUri}" />

<c:set var="jobsRoot" value="${ctx}/jobs" />

<html lang="ko">
	<body class="wrap">
		<div id="container">
			<div id="header">
				<c:choose>
					<c:when test="${uri == jobsRoot || fn:startsWith(uri, jobsRoot.concat('/'))}">
						<tiles:insertAttribute name="jobs-header" ignore="true"/>
					</c:when>
					<c:otherwise>
						<tiles:insertAttribute name="header"/>
					</c:otherwise>
				</c:choose>
			</div>
			
			<div id="content">
				<tiles:insertAttribute name="body"/>
			</div>
			
			<div id="footer">
				<tiles:insertAttribute name="footer"/>
			</div>
		</div>
	</body>
</html>