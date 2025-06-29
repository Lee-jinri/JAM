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
			
			let post_id = $("#post_id").val();
			let authorUserId;
			let authorUserName;
			
			getBoard();
			
			// 게시글 불러오는 함수
			function getBoard(){
				if(post_id == "" || post_id == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
				else{
					fetch("/api/fleaMarket/post/" + post_id)
					.then(response =>{
						if(!response.ok){
							throw new Error('Network response was not ok.');
						}
						return response.json();
					})
					.then(data =>{
						authorUserId = data.user_id;
						authorUserName = data.user_name;
						
						$("#title").html(data.title);
						$("#user_name").html(data.user_name);
						$("#date").html(data.created_at);
						$("#hits").html(data.view_count);
						$("#board_content").html(data.content);
						$("#price").html("가격 : " + data.price + " 원");
						
						if(data.sales_status == 1) $("#saleDone").html("거래 완료 된 글 입니다.");
						
						currentUserIsAuthor(authorUserId);
					})
					.catch(error => console.error('Error : ', error));
				}
			}
			
			// 현재 로그인한 사용자와 글쓴이가 같은지 비교하는 함수
			function currentUserIsAuthor(authorUserId){
				
				fetch('/api/member/me/session')
		        .then(response => response.json())
		        .then(data =>{
		        	let loggedInUserId = data.userId;
		        	
		        	let isAuthor = false;
		        	
		        	if(loggedInUserId == authorUserId) isAuthor = true;
		        	renderPostActionButtons(isAuthor);
		        })	
			}
			
			// 수정 버튼 클릭
			$(document).on("click", "#fleaUpdateBtn", function() {
			    if(post_id == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			    else $(location).attr('href','/fleaMarket/post/edit/'+post_id);
			});
			
			// 삭제 버튼 클릭
			$(document).on("click", "#fleaDeleteBtn", function() {
				if(confirm("정말 삭제하시겠습니까?")){
					fetch('/api/fleaMarket/post/'+post_id,{
						method: 'DELETE'
					})
					.then(response =>{
						if (!response.ok) {
							throw new Error('Network response was not ok');
						}
						alert("삭제가 완료 되었습니다.");
						$(location).attr('href', '/fleaMarket/board');
					})
					.catch(error =>{
						alert('게시글 삭제를 완료할 수 없습니다. 잠시 후 다시 시도해주세요.');
						console.error('Error : ' , error);
					});
				}
			});
			
			/* 쪽지 아이콘 클릭 
			$(document).on("click", "#send_msg", function() {
			    $("#receiver_id").val(authorUserId);
			    $("#receiver").val(authorUserName);

			    var url = "/message/send";
			    var option = "width=500, height=430, top=10, left=10";
			    var name = "팝업";

			    window.open("", name, option);
			    
			    $("#formPopup").attr("action", url);
			    $("#formPopup").attr("target","팝업");
			    $("#formPopup").attr("method","post");
			    $("#formPopup").submit();
			});
			
			*/
		})
		
		
		// 글쓴이와 로그인한 사용자가 다르면 채팅 버튼 생성
		function renderPostActionButtons(isAuthor) { 
			
			if(isAuthor){
				var btnDiv = document.getElementById("btnDiv");
				
               	// 수정 버튼 생성
               	var updateButton = document.createElement("button");
               	updateButton.type = "button";
               	updateButton.id = "fleaUpdateBtn";
               	updateButton.textContent = "수정"; 

               	// 삭제 버튼 생성
               	var deleteButton = document.createElement("button");
               	deleteButton.type = "button";
               	deleteButton.id = "fleaDeleteBtn";
               	deleteButton.textContent = "삭제"; 

               	// 버튼을 btn_div에 추가
               	btnDiv.appendChild(updateButton);
               	btnDiv.appendChild(deleteButton);
			} else {
				// FIXME: 채팅으로 변경할 것
				var msgDiv = document.getElementById("msgDiv");
				
		        var imgElement = document.createElement("img");
		        imgElement.id = "send_msg";
		        imgElement.className = "message_icon ml-05 cursor-pointer";
		        imgElement.style.width = "2rem";
		        imgElement.alt = "쪽지";
		        imgElement.src = "/resources/include/images/message_icon.svg";
		
		        msgDiv.appendChild(imgElement);
			}
		}
		

	</script>
</head>
<body class="wrap">
	<div class="my-top-15 my-bottom-15">
		<form name="f_data" id="f_data">
			<input id="post_id" type="hidden" name="post_id" value="${post_id }"/>
		</form>
		<form name="formPopup" id="formPopup">
			<input type="hidden" id="receiver_id" name="receiver_id">
			<input type="hidden" id="receiver" name="receiver">
		</form>
		<div class="py-8 text-center">
			<h1 id="title" class="font-weight-bold"></h1>
		</div>
		<div class="content flex items-center float-right">
			<span id="user_name"></span>
			<div id="msgDiv"></div>
			<span id="date" class="ml-1"></span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span id="hits" class="ml-05"></span>
		</div>
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<div>
				<span id="saleDone"></span>
			</div>
			<p id="price"></p>
			<p id="board_content"></p>
		</div>
		<div class="text-right my-top-7">
			<div id="btnDiv"></div>
		</div>
		<div class="py-8">
			<jsp:include page="comment.jsp"/>
		</div>
		<div class="text-center">
			<a id="fleaList" href="/fleaMarket/boards">목록</a>
		</div>
		
	</div>
</body>
</html>