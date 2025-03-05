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
<script>
$(function(){
			
	// 전송 버튼 클릭
	$("#send").click(function(){
	    	
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
				
        let messageData = {
        	receiver: "${receiver }",
        	message_title: message_title,
        	message_contents : message_contents
        };
		        	
        fetch('/api/message/send',{
        	method: 'post',
        	headers: {
        		'Content-Type': 'application/json'
        	},
        	body: JSON.stringify(messageData)
        }).then(response => {
        	if(response.status == 401){
        		alert("로그인이 필요한 서비스입니다.");
        		$(location).attr('href','/member/login');
        	}
        	
        	if(response.status == 200){
        		alert("쪽지가 전송 되었습니다.");
	           	opener.parent.location.reload();
	           	self.close();
        	}
        }).catch(error => {
        	alert("쪽지 전송에 실패했습니다. 잠시 후 다시 시도해주세요.");
		    console.error('쪽지 전송 중 오류 발생:', error);
		});
	});
			
			
	// 닫기 버튼 클릭
	$("#close").click(function(){
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
				<span class="ml-05">${receiver }</span>
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
				<button type="button" class="msgBtn mr-1" id="send" style="cursor:pointer;">전송</button>
				<button type="button" class="msgBtn2" id="close"style="width: 4rem; cursor:pointer;" >닫기</button>
			</div>
		</div>
	</form>
</body>
</html>