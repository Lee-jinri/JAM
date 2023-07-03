<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - MESSAGE</title>

<style>
	#receive_btn{
		background-color : #ffdd77;
		border : 2px #848484;
		border-top-left-radius: 1em;
		border-top-right-radius: 1em;
	}
	#send_btn {
		background-color : #f5ecce;
		border : 2px #848484;
		border-top-left-radius: 1em;
		border-top-right-radius: 1em;
	}
	.message_menu{font-weight:bold;}
	ul {
		padding : 0 30px; 
		padding-top : 10px; 
		padding-bottom : 10px; 
		line-height: 1.42857143; 
		margin-bottom : 0; 
		}
	li {
		display: inline-block;
		text-align : left;
	}
	.message {color : #777;}
	.msg_title {
		border : none;
		background-color : white;
		}
	#receive_btn{background-color : #f5ecce;}
	#send_btn {background-color : #ffdd77;}
	
</style>
<script>
	function senderPopup(message_no){
		$("#message_no").val(message_no);
		
		var url = "/message/sMessage_detail";
		var option = 'width=450, height=610, top=10, left=10';
		var name='팝업';
		
		window.open("",name,option);
		
		$("#frmPopup").attr("action", url);
		$("#frmPopup").attr("target", "팝업");
		$("#frmPopup").attr("method", "POST");
		$("#frmPopup").submit();
	}
</script>
</head>
<body>
	<div class="rem-20 my-top-15 my-bottom-15">
		<div class="title">
			<p class="text-center my-7">쪽지</p>
		</div>
		<div class="content">
			<button id="receive_btn" class="width_100px height_35px ml-1" onclick="location.href='/message/receiveMessage'" >받은 쪽지</button>
			<button id="send_btn" class="width_100px height_35px ml-1" onclick="location.href='/message/sendMessage'" >보낸 쪽지</button>
			
			<div class="border border-radius-15px" >
				<form id="frmPopup" name="frmPopup">
					<input type="hidden" id="message_no" name="message_no">
				</form>
				<div id="message_menu">
					<ul class="message_menu">
						<li class='message_menu width_20'>받는 사람</li>
						<li class='message_menu width_45'>제목</li>
						<li class='message_menu width_20'>보낸 시간</li>
						<li class='message_menu'>확인 여부</li>
					</ul>
				</div>
				<div id="message_list">
					<c:choose>
						<c:when test="${not empty sMessage }">
							<c:forEach items="${sMessage }" var="message" varStatus="status">
								<ul class="message border-top">
									<li class='receiver message width_20'>${message.receiver }</li>
									<li class="width_45">
										<button type='button' class='msg_title' onclick='senderPopup(${message.message_no});'>${message.message_title } </button>
									</li>
									<li class="width_20">${message.sendTime }</li>
									<c:choose>
										<c:when test="${message.read_chk == 0}">
											<li class='read_chk message '>읽지 않음 </li>
										</c:when> 
										<c:when test="${message.read_chk == 1}">
											<li class='read_chk message'>읽음</li>
										</c:when>
									</c:choose>
								</ul>
							</c:forEach>
						</c:when>
					</c:choose>
				</div>
			</div>
		</div>
	</div>
</body>
</html>