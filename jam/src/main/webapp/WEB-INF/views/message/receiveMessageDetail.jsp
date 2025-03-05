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
	
		body {margin:0; width=500; height=370;}
		
	</style>
	<script>
		$(function(){
			
			$("#response").click(function(){
				
				let url = '/message/send/' + '${receiveDetail.sender}';
				
				$(location).attr('href',url);
			})
		
			// 닫기 버튼 클릭
			$("#close").click(function(){
				self.close();
			});
		});
		
	</script>
</head>
<body>
	<div class="contents message">
		
		<div class="border-bottom">
		</div>
		
		<div class="send_info">
			<div class="margin15px ">
				<img class="icon vertical-align-m"style="width:1.7rem;" src="/resources/include/images/msg1.svg">
				<span class="font600 vertical-align-m">보낸 사람</span>
			</div>
			<div class="font600 margin15px msg_border height35 background-color-gray flex items-center">
				<span id="sender" class="ml-05">${receiveDetail.sender }</span>
			</div>
		</div>
		
		
		<div class=''>
			<div class="margin15px"  >
				<img class="icon vertical-align-m"style="width:1.3rem;" src="/resources/include/images/msg2.svg">
				<span class="font600 vertical-align-m">제목</span>
			</div>
			<div class="margin15px msg_border height35 flex">
				<span id="message_title" class="pd-top05 ml-05">${receiveDetail.message_title }</span>
			</div>
		</div>	
			
		<div class=''>
			<div class="margin15px"  >
				<img class="icon vertical-align-m"style="width:1.3rem;" src="/resources/include/images/msg2.svg">
				<span class="font600 vertical-align-m">쪽지 내용</span>
			</div>
			<div class="margin15px msg_border min-height60 flex" >
				<span id="message_content" class='pd-top05 ml-05'>${receiveDetail.message_contents }</span>
			</div>
		</div>
		<div class="border-bottom my-top-7">
		</div>
		
		<div class="msgBtn_div my-top-4 margin15px ">
			<button type="button" class="msgBtn mr-1" id="response"  style="cursor:pointer">답장</button>
			<button type="button" class="msgBtn2" id="close"style="width: 4rem; cursor:pointer;" >닫기</button>
		</div>
		
	</div>
</body>
</html>