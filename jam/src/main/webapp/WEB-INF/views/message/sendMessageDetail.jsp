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
		
	</style>
	
	<script>
		$(function(){
			
			let data ={
					message_no: "${message_no }",
					sender_id: "${sender_id }"
			};
			
			fetch("/api/message/sendMessageDetail", {
				method: 'post',
				headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
			}).then(response =>{
				if(response.ok){
					return response.json();
				}else if(response.status == 403){
					alert("권한이 없는 페이지입니다.");
					$(location).attr("href","/");
					return false;
				}
				else {
					throw new Error();
				}
			}).then((data) =>{
				$("#message_title").text(data.message_title);
				$("#message_content").text(data.message_contents);
				$("#receiver").text(data.receiver);
				
			}).catch(error => {
				console.log(error);
				alert("요청하신 작업을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
				self.close();
			})
			
			$("#close").click(function(){
				self.close();
			})
		})
	</script>
</head>
<body>
	<div class="contents message">
		<div class="margin15px">
			<span id="message_title"></span>
		</div>
		<div class="border-bottom"></div>
		
		<div class="receive_info">
			<div class="margin15px ">
				<img class="icon vertical-align-m"style="width:1.7rem;" src="/resources/include/images/msg1.svg">
				<span class="font600 vertical-align-m">받는 사람</span>
			</div>
			<div class="font600 margin15px msg_border height35 background-color-gray flex items-center">
				<span id="receiver" class="ml-05"></span>
			</div>
		</div>
		
		<div>
			<div class="margin15px"  >
				<img class="icon vertical-align-m"style="width:1.3rem;" src="/resources/include/images/msg2.svg">
				<span class="font600 vertical-align-m">쪽지 내용</span>
			</div>
			<div class="margin15px msg_border min-height60 flex" >
				<span class='pd-top05 ml-05' id="message_content"></span>
			</div>
		</div>
		
		<div class="border-bottom my-top-7"></div>
		
		<div class="msgBtn_div my-top-4 margin15px">
			<button type="button" class="msgBtn mr-1 cursor-pointer" id="close"style="width: 4rem;" >닫기</button>
		</div>
	</div>
</body>
</html>