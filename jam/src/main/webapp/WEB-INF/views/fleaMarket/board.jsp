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
			
			let flea_no = $("#flea_no").val();
			let authorUserId;
			let authorUserName;
			
			getBoard();
			
			// 게시글 불러오는 함수
			function getBoard(){
				if(flea_no == "" || flea_no == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
				else{
					fetch("/api/fleaMarket/board/" + flea_no)
					.then(response =>{
						if(!response.ok){
							throw new Error('Network response was not ok.');
						}
						return response.json();
					})
					.then(data =>{
						authorUserId = data.user_id;
						authorUserName = data.user_name;
						
						$("#title").html(data.flea_title);
						$("#user_name").html(data.user_name);
						$("#date").html(data.flea_date);
						$("#hits").html(data.flea_hits);
						$("#board_content").html(data.flea_content);
						$("#price").html("가격 : " + data.price + " 원");
						
						if(data.sales_status == 1) $("#saleDone").html("거래 완료 된 글 입니다.");
						
						currentUserIsAuthor();
					})
					.catch(error => console.error('Error : ', error));
				}
			}
			
			// 현재 로그인한 사용자와 글쓴이가 같은지 비교하는 함수
			function currentUserIsAuthor(){
					// 로그인 한 사용자 정보 가져오기
					fetch('/api/member/me/token',{
						method: 'GET'
					})
					.then(response =>{
						if(!response.ok)
							throw new Error('Network response was not ok.');
						return response.json();
					})
					.then(data =>{
						let loggedInUserId = data.user_id;
						
						// 글쓴이와 로그인한 사용자가 다르면 메세지 버튼 생성
						if(loggedInUserId != null){
							if(authorUserId != loggedInUserId){
								var msgDiv = document.getElementById("msgDiv");
								
						        var imgElement = document.createElement("img");
						        imgElement.id = "send_msg";
						        imgElement.className = "message_icon ml-05 cursor-pointer";
						        imgElement.style.width = "2rem";
						        imgElement.alt = "쪽지";
						        imgElement.src = "/resources/include/images/message_icon.svg";
						
						        msgDiv.appendChild(imgElement);
							} else {
						        
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
							}
						}
					})
					.catch(error => {
						console.error('사용자 정보를 가져오는 중 오류 발생 : ' + error);
					})

			}
			
			
			
			
			// 수정 버튼 클릭
			$(document).on("click", "#fleaUpdateBtn", function() {
			    if(flea_no == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			    else $(location).attr('href','/fleaMarket/board/edit/'+flea_no);
			});
			
			// 삭제 버튼 클릭
			$(document).on("click", "#fleaDeleteBtn", function() {
				if(confirm("정말 삭제하시겠습니까?")){
					fetch('/api/fleaMarket/board/'+com_no,{
						method: 'DELETE'
					})
					.then(response =>{
						if (!response.ok) {
							throw new Error('Network response was not ok');
						}
						alert("삭제가 완료 되었습니다.");
						$(location).attr('href', '/fleaMarket/boards');
					})
					.catch(error =>{
						alert('게시글 삭제를 완료할 수 없습니다. 잠시 후 다시 시도해주세요.');
						console.error('Error : ' , error);
					});
				}
			});
			
			/* 쪽지 아이콘 클릭 */
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
			
			
		})
		
	</script>
</head>
<body class="wrap">
	<div class="rem-30 my-top-15 my-bottom-15">
		<form name="f_data" id="f_data">
			<input id="flea_no" type="hidden" name="flea_no" value="${flea_no }"/>
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
			<jsp:include page="reply.jsp"/>
		</div>
		<div class="text-center">
			<a id="fleaList" href="/fleaMarket/boards">목록</a>
		</div>
		
	</div>
</body>
</html>