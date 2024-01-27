<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>

	
	<script type="text/javascript">
		$(function(){
			
			let com_no = $("#com_no").val();
			let authorUserId;
			let authorUserName;
			
			getBoard();
			
			function getBoard(){
				if(com_no == "" || com_no == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
				else{
					fetch('/api/community/board/' + com_no)
			        .then(response => {
						if (!response.ok) {
							throw new Error('Network response was not ok');
						}
						return response.json();
					})
			        .then(data => {
			        	authorUserId = data.user_id;
			        	authorUserName = data.user_name;
			        	
			        	$("#com_title").html(data.com_title);
			        	$("#user_name").html(data.user_name);
			        	$("#com_date").html(data.com_date);
			        	$("#com_hits").html(data.com_hits);
			        	$("#com_content").html(data.com_content);
			         
			        	currentUserIsAuthor();
			        })
			        .catch(error => console.error('Error:', error));
				}
			}
			
			// 현재 로그인한 사용자와 글쓴이가 같은지 비교하는 함수
			function currentUserIsAuthor(){
				// 로그인 한 사용자 정보 가져오기
				fetch('/api/member/getUserInfo', {
		            method: 'GET', 
		            headers: {
		            	'Authorization': localStorage.getItem("Authorization")
		            },
		        })
		        .then(response => {
		            if (!response.ok)
		            	throw new Error('Network response was not ok');
		            
		            return response.json();
		        })
		        .then(data =>{
					let loggedInUserId = data.user_id;
	                
	                // 글쓴이와 현재 로그인한 사용자가 다르면 메세지 버튼 생성
	                if (loggedInUserId != null) {
					    if (authorUserId != loggedInUserId) {
					    	
					        var msgDiv = document.getElementById("msgDiv");
					
					        var imgElement = document.createElement("img");
					        imgElement.id = "send_msg";
					        imgElement.className = "message_icon ml-05 cursor-pointer";
					        imgElement.style.width = "2rem";
					        imgElement.alt = "쪽지";
					        imgElement.src = "/resources/include/images/message_icon.svg";
					
					        msgDiv.appendChild(imgElement);
					
					    } else { // 글쓴이와 사용자가 같으면 수정, 삭제 버튼 추가
					        var btnDiv = document.getElementById("btn_div");
					
					        // 수정 버튼 생성
					        var updateButton = document.createElement("button");
					        updateButton.type = "button";
					        updateButton.id = "comUpdateBtn";
					        updateButton.textContent = "수정"; // 버튼 텍스트 설정
					
					        // 삭제 버튼 생성
					        var deleteButton = document.createElement("button");
					        deleteButton.type = "button";
					        deleteButton.id = "comDeleteBtn";
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
			$(document).on("click", "#comUpdateBtn", function() {
			    if(com_no == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			    else $(location).attr('href', '/community/board/edit/'+com_no);
			});
			
			// 삭제 버튼 클릭
			$(document).on("click", "#comDeleteBtn", function() {
				if(confirm("정말 삭제하시겠습니까?")){
					fetch('/api/community/board/'+$("#com_no").val(), {
						method: 'DELETE'
					})
					.then(response => {
						if (!response.ok) {
							throw new Error('Network response was not ok');
						}
						alert("삭제가 완료 되었습니다.");
						$(location).attr('href', '/community/boards');
					})
					.catch(error => {
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
			    var option = "width=500, height=370, top=10, left=10";
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
		<input type="hidden" id="com_no" name="com_no" value="${com_no }"/>
		
		<form id="formPopup" name="formPopup">
			<input type="hidden" id="receiver_id" name="receiver_id">
			<input type="hidden" id="receiver" name="receiver">
		</form>
		
		<div class="py-8 text-center">
			<h1 id="com_title" class="font-weight-bold"></h1>
		</div>
		<div class="content flex items-center float-right">
			<span id="user_name"></span>
			<span id="user_id"></span>
			<div id="msgDiv"></div>

			<span id="com_date" class="ml-1"></span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span id="com_hits" class="ml-05"></span>
		</div>
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<p id="com_content"></p>
		</div>
		<div class="text-right my-top-7">
			<div id="btn_div"></div>
		</div>
		<div class="py-8">
			<jsp:include page="reply.jsp"/>
		</div>
		<div class="text-center">
			<a id="comList" href="/community/boards">- 목록 -</a>
		</div>
	</div>
		
</body>
</html>