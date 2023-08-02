<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 합주실/연습실</title>
	
	<script type="text/javascript">
		$(function(){
			
			$("#roomRentalUpdateBtn").click(function(){
				$("#f_data").attr({
					"action" : "/roomRental/roomRentalUpdateForm",
					"method" : "POST"
				})
				$("#f_data").submit();
			})
			
			$("#roomRentalDeleteBtn").click(function(){
				if(confirm("정말 삭제하시겠습니까?")){
					$("#f_data").attr({
						"action" : "/roomRental/roomRentalDelete",
						"method" : "POST"
					})
					$("#f_data").submit();
				}return;
			})
			
			/* 쪽지 아이콘 클릭 */
			$("#send_msg").click(function(){
				var url = "/message/send";
				var option = "width=500, height=370, top=10, left=10";
				var name = "팝업";
				
				window.open("",name,option);
				
				$("#frmPopup").attr("action", url);
				$("#frmPopup").attr("target", name);
				$("#frmPopup").attr("method", "POST");
				$("#frmPopup").submit();
			})
		})
		
	</script>
</head>
<body>
	<div class="rem-30 my-top-15 my-bottom-15">
		<form name="f_data" id="f_data">
			<input type="hidden" name="roomRental_no" value="${detail.roomRental_no}"/>
		</form>
		<form name="frmPopup" id="frmPopup">
			<input type="hidden" id="receiver_id" name="receiver_id" value="${detail.user_id }">
			<input type="hidden" id="receiver" name="receiver" value="${detail.user_name }">
		</form>
		
		<div class="py-8 text-center">
			<h1 class="font-weight-bold">${detail.roomRental_title }</h1>
		</div>
		
		<div class="content flex items-center float-right">
			<span>${detail.user_name }</span>
			<c:if test="${member.user_id != detail.user_id}">
				<img id='send_msg' class='message_icon ml-05 cursor-pointer' style='width:2rem;' alt='쪽지' src='/resources/include/images/message_icon.svg'>			
			</c:if>
			
			<span class="ml-1">${detail.roomRental_date }</span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span class="ml-05">${detail.roomRental_hits }</span>
		</div>
		
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<p>${detail.roomRental_content }</p>
		</div>
		<div class="text-right my-top-7">
			<c:if test="${member.user_id == detail.user_id }">
				<div>
					<button type="button" id="roomRentalUpdateBtn" class="">수정</button>
					<button type="button" id="roomRentalDeleteBtn" class="">삭제</button>
				</div>
			</c:if>
		</div>	
		<div class="py-8">
			<jsp:include page="roomRentalReply.jsp"/>
		</div>
		<div class="text-center">
				<a id="roomList" href="/roomRental/roomRentalList">- 목록 -</a>
		</div>
	</div>
</body>
</html>