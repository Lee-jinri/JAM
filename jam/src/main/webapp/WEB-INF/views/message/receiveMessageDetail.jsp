<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<!-- 제이쿼리 -->
<script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
		
<title>JAM - MESSAGE</title>
	<style>
		.contents{
			margin:0 10px;
		}
		#msg_title{
			font-weight:bold; 
			font-size:20px;
		}
		.title{
			margin-bottom:10px;
			height:30px;
			border-bottom: solid #ffdd77;
		}
		#sender{
			font-weight:600;
		}
		#senderTime{
			display: block; 
			color:#A4A4A4;
		}
		.msg_contents{
			border:3px solid #ffdd77; 
			border-radius:20px;
			min-height:450px; 
			margin-top:10px;
		}
		#msg_contents{
			display: block;
    		margin: 10px;
    	}
    	.btn{
    		display: flex;
    		justify-content: center; margin:10px 0;
		}
		#response, #list {    width: 70px;
    height: 30px; border:none; border-radius:15px;}
    	#response {background-color:#ffb689; margin-right:1rem;}
    	#list{background-color:#ffdd77;}

	</style>
	<script>
		$(function(){
			$("#list").click(function(){
				self.close();
			});
			
			$("#response").click(function(){
				
				$("#responseForm").attr({
					"action" : "/message/response",
					"method" : "POST"
				})
				
				$("#responseForm").submit();
				
			})
		
		});
		
	</script>
</head>
<body>
	<div class="contents">
		<div class="title">
			<span id="msg_title">${detail.message_title }</span>
		</div>
		<div class="send_info">
			<span id="sender">보낸 사람</span>
			<span id="sender_name">${detail.sender }</span>
			<span id='sender_id'>(${detail.sender_id })</span>
			<span id='senderTime'>${detail.sendTime }</span>
		</div>
		<div class='msg_contents'>
			<span id='msg_contents'>${detail.message_contents }</span>
		</div>
		<div class="btn">
			<button type="button" class="mr-1" id="response">답장</button>
			<button type="button" id="list">목록</button>
		</div>
		
		<div>
			<form id="responseForm" name="responseForm">
				<input type="hidden" name="sender_id" value="${detail.sender_id }">
				<input type="hidden" name="sender" value="${detail.sender }">
			</form>
		</div>
	</div>
<p>
</p>

</body>
</html>