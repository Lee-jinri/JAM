<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>

	
	<script type="text/javascript">
		$(function(){
			
			$("#comUpdateBtn").click(function(){
				$("#c_data").attr({
					"action" : "/community/communityUpdateForm",
					"method" : "POST"
				})
				$("#c_data").submit();
			})
			
			$("#comDeleteBtn").click(function(){
				if(confirm("정말 삭제하시겠습니까?")){
					$("#c_data").attr({
						"action" : "/community/communityDelete",
						"method" : "POST"
					})
					$("#c_data").submit();
				}return;
			})
			
		})
		
	</script>
</head>
<body>
	<div class="rem-30 my-top-15 my-bottom-15">
		<form name="c_data" id="c_data">
			<input type="hidden" name="com_no" value="${detail.com_no}"/>
		</form>
		
		<div class="py-8 text-center">
			<h1 class="font-weight-bold">${detail.com_title }</h1>
		</div>
		<div class="content flex items-center float-right">
			<span>${detail.user_name }</span>
			<span class="ml-1">${detail.com_date }</span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span class="ml-05">${detail.com_hits }</span>
		</div>
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<p>${detail.com_content }</p>
		</div>
		<div class="text-right my-top-7">
			<c:if test="${member.user_id == detail.user_id }">
				<div id="btn_div">
					<button type="button" id="comUpdateBtn" class="">수정</button>
					<button type="button" id="comDeleteBtn" class="">삭제</button>
				</div>
			</c:if>
		</div>
		<div class="py-8">
			<jsp:include page="communityReply.jsp"/>
		</div>
		<div class="text-center">
			<a id="comList" href="/community/communityList">- 목록 -</a>
		</div>
	</div>
		
		
		
</body>
</html>