<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>

	<script type="text/javascript">
		$(function(){
			
			$("#fleaUpdateBtn").click(function(){
				$("#f_data").attr({
					"action" : "/fleaMarket/fleaUpdateForm",
					"method" : "POST"
				})
				$("#f_data").submit();
			})
			
			$("#fleaDeleteBtn").click(function(){
				if(confirm("정말 삭제하시겠습니까?")){
					$("#f_data").attr({
						"action" : "/fleaMarket/fleaDelete",
						"method" : "POST"
					})
					$("#f_data").submit();
				}return;
			})
			
		})
		
	</script>
</head>
<body>
	<div class="rem-30 my-top-15 my-bottom-15">
		<form name="f_data" id="f_data">
			<input type="hidden" name="flea_no" value="${detail.flea_no}"/>
		</form>
		
		<div class="py-8 text-center">
			<h1 class="font-weight-bold">${detail.flea_title }</h1>
		</div>
		<div class="content flex items-center float-right">
			<span >${detail.user_name }</span>
			<span class="ml-1">${detail.flea_date }</span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span class="ml-05">${detail.flea_hits }</span>
		</div>
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<p>가격 : ${detail.price }원</p>
			<p>${detail.flea_content }</p>
		</div>
		<div class="text-right my-top-7">
			<c:if test="${member.user_id == detail.user_id }">
				<div id="btn_div">
					<button type="button" id="fleaUpdateBtn" class="">수정</button>
					<button type="button" id="fleaDeleteBtn" class="">삭제</button>
				</div>
			</c:if>
		</div>
		<div class="py-8">
			<jsp:include page="fleaMarketReply.jsp"/>
		</div>
		<div class="text-center">
			<a id="fleaList" href="/fleaMarket/fleaMarketList">- 목록 -</a>
		</div>
		
	</div>
</body>
</html>