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

			let room_no = $("#roomRental_no").val();
			let authorUserId;
			
			getBoard();
			
			function getBoard(){
				if(room_no == "" || room_no == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
				else {
					fetch('/api/roomRental/board/'+room_no)
					.then(response =>{
						if(!response.ok){
							throw new Error('Network response was not ok.');
						}
						return response.json();
					})
					.then(data =>{
						authorUserId = data.user_id;
					
						$("#room_title").html(data.roomRental_title);
						$("#user_name").html(data.user_name);
						$("#room_date").html(data.roomRental_date);
						$("#room_hits").html(data.roomRental_hits);
						$("#room_content").html(data.roomRental_content);
						
						if(data.roomRental_status == 1){
							$("#room_status").html("거래 완료된 글 입니다.");
						}
						
						currentUserIsAuthor();
					})
					.catch(error => console.error('Error : ' , error));
					
				}
			}
			
			// 현재 로그인한 사용자와 글쓴이가 같은지 비교하는 함수
			function currentUserIsAuthor(){
				fetch('/api/member/me/token',{
					method: 'GET'
				})
				.then(response =>{
					if(!response.ok){
						throw new Error('Network response was not ok.');
					}
					return response.json();
				})
				.then(data => {
					let loggedInUserId = data.user_id;
				
					if(loggedInUserId != null){
						if(authorUserId != loggedInUserId){
							var msgDiv = document.getElementById('msgDiv');
							
							var imgElement = document.createElement('img');
							imgElement.id = 'send_msg';
							imgElement.className = 'message_icon ml-05 cursor-pointer';
							imgElement.style.width = "2rem";
					        imgElement.alt = "쪽지";
					        imgElement.src = "/resources/include/images/message_icon.svg";
					
					        msgDiv.appendChild(imgElement);
					
						
						}else{
							var btnDiv = document.getElementById("btn_div");
							
					        // 수정 버튼 생성
					        var updateButton = document.createElement("button");
					        updateButton.type = "button";
					        updateButton.id = "jobUpdateBtn";
					        updateButton.textContent = "수정"; // 버튼 텍스트 설정
					
					        // 삭제 버튼 생성
					        var deleteButton = document.createElement("button");
					        deleteButton.type = "button";
					        deleteButton.id = "jobDeleteBtn";
					        deleteButton.textContent = "삭제"; // 버튼 텍스트 설정

					        btnDiv.appendChild(updateButton);
					        btnDiv.appendChild(deleteButton);
						}
					}
				})
				.catch(error => {
		            console.error('사용자 정보를 가져오는 중 오류 발생:', error);
		        });
				
			}
			
			// 수정 버튼 클릭
			$(document).on("click", "#roomRentalUpdateBtn", function() {
			    if(room_no == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			    else $(location).attr('href','/roomRental/board/edit/'+room_no);
			
			});
			
			// 삭제 버튼 클릭
			$(document).on("click", "#roomRentalDeleteBtn", function() {
				if(confirm("정말 삭제하시겠습니까?")){
					fetch('/api/roomRental/board/'+room_no,{
						method: 'DELETE'
					})
					.then(response =>{
						if(!response.ok){
							throw new Error('Network response was not ok.');
						}
						alert('삭제가 완료되었습니다.');
						$(location).attr('href','/roomRental/boards');
					})
					.catch(error =>{
						alert('게시글 삭제를 완료할 수 없습니다. 잠시 후 다시 시도해주세요.');
						console.error('Error : ' , error);
					})
				}
			});
			
			/* 쪽지 아이콘 클릭 */
			$("#send_msg").click(function(){
				var url = "/message/send";
				var option = "width=500, height=430, top=10, left=10";
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
<body class="wrap">
	<div class="rem-30 my-top-15 my-bottom-15">
		<form name="f_data" id="f_data">
			<input id="roomRental_no" type="hidden" name="roomRental_no" value="${roomRental_no}"/>
		</form>
		<form name="frmPopup" id="frmPopup">
			<input type="hidden" id="receiver_id" name="receiver_id" value="${detail.user_id }">
			<input type="hidden" id="receiver" name="receiver" value="${detail.user_name }">
		</form>
		
		<div class="py-8 text-center">
			<h1 id="room_title" class="font-weight-bold"></h1>
		</div>
		
		<div class="content flex items-center float-right">
			<span id="user_name"></span>
			<div id="msgDiv"></div>
			
			<span id="room_date" class="ml-1"></span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span id="room_hits" class="ml-05"></span>
		</div>
		
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<div> 
				<p id="room_status" class="font-color-red"></p>
			</div>
			<p id="room_content"></p>
		</div>
		<div class="text-right my-top-7">
			<div id="btn_div"></div>
		</div>	
		<div class="text-center">
				<a id="roomList" href="/roomRental/boards">목록</a>
		</div>
	</div>
</body>
</html>