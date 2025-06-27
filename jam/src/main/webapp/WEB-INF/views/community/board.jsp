<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>

<script src="/resources/include/dist/js/userToggle.js"></script>

<script type="text/javascript">
$(function(){
			
	let com_no = $("#com_no").val();
	let authorUserName;
			
	getBoard(com_no).then(() => {
		toggleUserMenu(); // mainBoards 실행 후 getUserInfo 실행
    })
    .catch(error => {
        console.error('Error while executing mainBoards:', error);
    });
	
			
	// 수정 버튼 클릭
	$(document).on("click", "#comUpdateBtn", function() {
		if(com_no == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		else $(location).attr('href', '/community/board/edit/'+com_no);
	});
			
	// 삭제 버튼 클릭
	$(document).on("click", "#comDeleteBtn", function() {
		if(confirm("정말 삭제하시겠습니까?")){
			fetch('/api/community/board?com_no='+$("#com_no").val(), {
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
			
})

function getBoard(com_no){
	return new Promise((resolve, reject) => {
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
				const detail = data.detail;
			    authorUserName = detail.user_name;
			        
			    $("#com_title").html(detail.com_title);
				$("#user_name").html(detail.user_name);
				$("#user_name").attr("data-userid", detail.user_id);
				$("#com_date").html(detail.com_date);
				$("#com_hits").html(detail.com_hits);
				$("#com_content").html(detail.com_content);
				        
				currentUserIsAuthor(data.isAuthor);
				
				resolve();
			})
			.catch(error => {
	            console.error('Error:', error);
	            reject(error); // 에러가 발생하면 reject 호출
	        });
		}
	})
}

// 현재 로그인한 사용자와 글쓴이가 같은지 비교하는 함수
function currentUserIsAuthor(isAuthor){
	if (isAuthor) { // 글쓴이와 사용자가 같으면 수정, 삭제 버튼 추가
		
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
		
</script>
</head>
<body class="wrap">
	<div class=" my-top-15 my-bottom-15">
		<input type="hidden" id="com_no" name="com_no" value="${com_no }" />
		
		<div class="board-detail-container">
			<div class="board-header">
			    <p id="com_title" class="com_title board-title"></p>
			    <div class="board-info">
					<span id="user_name" class="userName boardUserName"></span>  
					<div class="userNameToggle"></div> 
			      	<span class="divider">|</span>
			       	<span id="com_date"></span>
			        <span class="divider">|</span>
			        <span class="com-hits board-hits">
			            <span id="com_hits"></span>
			        </span>
			    </div>
			</div>
			
			
			<!-- 본문 영역 -->
			<div class="com-content board-content">
			    <p id="com_content"></p>
			</div>
			
			<!-- 버튼 영역 -->
			<div class="board-buttons">
			    <div id="btn-div" class="author-buttons"></div>
			    <a href="/community/boards" class="board-btn">목록</a>
			</div>
			
			<!-- 댓글 -->
			<div class="com-reply">
				<jsp:include page="reply.jsp" />
			</div>
		</div>	
	</div>
</body>
</html>