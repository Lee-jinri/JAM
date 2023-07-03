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
		.message_title{
			font-weight:bold;
			margin : 10px 0;
		}
		.span_receiver{
			font-weight:bold;
		}
		.receiver{
			margin-bottom:10px;
		}
		.message_contents{
			display: flex;
    		justify-content: center; margin:10px 0;
		}
		.btn{
			display : flex;
			justify-content: center;
			margin:10px 0;
		}
		#send, #cancel {    width: 70px;
    			height: 30px; border:none; border-radius:15px;}
    	#send {background-color:#ffb689; margin-right:1rem;}
    	#cancel{background-color:#ffdd77;}
	</style>
	<script>
		$(function(){
			$("#send").click(function(){
				
				$.ajax({
			    	url: '/message/messageWrite',
			        type: 'post',
			        data: $('#message').serialize(),
			        error:function(xhr,textStatus, errorThrown){
						console.log(textStatus + "(HTTP-" +xhr.status+" / "+errorThrown+")");
					},
					beforeSend:function(){
						
						let message_title = $("#message_title").val();
						let message_contents = $("#message_area").val();
						
						if(message_title.replace(/\s/g,"") == ""){
							alert("쪽지 제목을 입력하세요.");
							$("#message_title").focus();
							return false;
						}
						
						if(message_contents.replace(/\s/g,"") == ""){
							alert("쪽지 내용을 입력하세요.");
							$("#message_area").focus();
							return false;
						}
					},
			        success: function(result){
			        	
						console.log(result);
			        	if(result=="SUCCESS"){  
			            	alert("쪽지가 전송 되었습니다.");
			            	
			            	opener.parent.location.reload();
							self.close();
			           	}else{
			            	alert("쪽지 전송에 실패했습니다. 잠시 후 다시 시도해주세요."); 
			            	
			            	opener.parent.location.reload();
							self.close();
			         	}
			   		}
				})
			});
			
			$("#cancel").click(function(){
				self.close();
			});
		})
	</script>
</head>
<body>
	<form id="message">
		<div class="contents">
			<div class="message_title">
				<span>제목</span>
				<input type="text" name="message_title" id="message_title">
			</div>
			<div class="receiver">
				<span class='span_receiver'>받는 사람</span>
				<span>${receiver } (${receiver_id })</span>
			</div>
			<div class="message_contents">
				<textarea rows="32" cols="55" name="message_contents" id="message_area"></textarea>
			</div>
			<div class="btn">
				<button type="button" id="send">보내기</button>
				<button type="button" id="cancel">취소</button>
			</div>
		</div>
		<div>
			<input type="hidden" name="receiver" value="${receiver }">
			<input type="hidden" name="receiver_id" value="${receiver_id }">
		</div>
	</form>
</body>
</html>