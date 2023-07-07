<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<!-- 제이쿼리 -->
<script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
<link rel="stylesheet" href="/resources/include/dist/css/layout.css" />
		
<title>JAM - MESSAGE</title>
	<style>
		body {margin:0;}
		.margin15px{
			margin:15px;
		}
		
		.border{
			border-bottom: 1px solid #BDBDBD;
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
		.msgBtn2 {
		    background-color:#fff; 
		     color:#0080FF; 
		    font-weight:600; 
		    border:1px solid #0080FF;
		    border-radius: 3px;
			height: 35px;
		}
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
			
			$("#message_list").click(function(){
				self.close();
			});
		})
	</script>
</head>
<body>
	<form id="message">
		<div class="my-top-4">
		
			<div class="receiver">
				<span class='ml-1 font600'>받는 사람</span>
				<span class="ml-05">${receiver } (${receiver_id })</span>
			</div>
			
			<div class="my-top-4 flex items-center">
				<span class="ml-1 font600">제목</span>
				<input type="text" class="mr-1 ml-05 msg_border height25 background-color-gray" name="message_title" id="message_title" style="background: none; flex: 1;">
			</div>
			
			<div class="border-bottom my-top-4"></div>
			
			<div class="margin15px"  >
				<img class="icon vertical-align-m"style="width:1.3rem;" src="/resources/include/images/msg2.svg">
				<span class="font600 vertical-align-m">쪽지 내용</span>
			</div>
			<div class="message_contents my-top-4">
				<textarea rows="10" cols="63" name="message_contents" id="message_area"  style="resize: none;" maxlength="200"></textarea>
			</div>
			<div class="msgBtn_div  margin15px ">
				<button type="button" class="msgBtn mr-1" id="send" style="width: 4rem;">보내기</button>
				<button type="button" class="msgBtn2" id="message_list"style="width: 4rem;" >목록</button>
			</div>
		</div>
		<div>
			<input type="hidden" name="receiver" value="${receiver }">
			<input type="hidden" name="receiver_id" value="${receiver_id }">
		</div>
	</form>
</body>
</html>