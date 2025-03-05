<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - JOB</title>
<style>
.job-detail-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
    border: 1px solid #ddd;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.job-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    border-bottom: 1px solid #eee;
    padding-bottom: 8px;
}

.job-title {
    font-size: 24px;
    font-weight: bold;
    margin: 0;
}

.job-info {
    text-align: right;
    font-size: 14px;
    color: #666;
}

.job-info .job-hits img {
    width: 16px;
    vertical-align: middle;
}

.job-meta-box {
    background-color: #f9f9f9;
    border: 1px solid #eee;
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 16px;
}

.job-meta-row {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    border-bottom: 1px solid #f0f0f0;
}

.job-meta-row:last-child {
    border-bottom: none;
}

.job-meta-label {
    font-weight: bold;
    color: #444;
}

.job-status {
    margin: 16px 0;
    padding: 8px;
    background-color: #ffefef;
    color: #d9534f;
    font-weight: bold;
    text-align: center;
    border-radius: 4px;
}

.job-content {
    margin: 16px 0;
    line-height: 1.6;
}

.job-buttons {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16px;
}

.btn-back {
    padding: 6px 12px;
    background-color: #f0f0f0;
    border: 1px solid #ddd;
    border-radius: 4px;
    color: #333;
    text-decoration: none;
    font-size: 14px;
}

.btn-back:hover {
    background-color: #e0e0e0;
}

</style>
	
	<script type="text/javascript">
	$(function(){
		
		let job_no = $("#job_no").val();
		let authorUserId;
		let authorUserName;
		
		getBoard();
		
		function getBoard(){
			if(job_no == "" || job_no == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
			else{
				fetch('/api/job/board/' + job_no)
		        .then(response => {
					if (!response.ok) {
						throw new Error('Network response was not ok');
					}
					return response.json();
				})
		        .then(data => {
		        	authorUserId = data.user_id;
		        	authorUserName = data.user_name;
		        	
		        	$("#job_title").html(data.job_title);
		        	$("#user_name").html(data.user_name);
		        	$("#job_date").html(data.job_date);
		        	$("#job_hits").html(data.job_hits);
		        	$("#job_content").html(data.job_content);
		        	
		        	$("#city").html(data.city);
		        	$("#gu").html(data.gu);
		        	$("#dong").html(data.dong);
		        	
					let pay_category;
		        	if(data.pay_category == 0) pay_category = "일급";
		        	else if(data.pay_category == 1) pay_category = "주급";
		        	else if(data.pay_category == 2) pay_category = "월급";
		        	
		        	$("#pay_category").html(pay_category);
		        		
		        	$("#pay").html(data.pay + " 원");
		        	
		        	if(data.job_status == 1){
		        		$("#job_status").html(data.job_category + " 완료된 글 입니다.");
		        	}
		         
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
		$(document).on("click", "#jobUpdateBtn", function() {
		    if(job_no == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		    else $(location).attr('href', '/job/board/edit/'+job_no);
		});
		
		// 삭제 버튼 클릭
		$(document).on("click", "#jobDeleteBtn", function() {
			if(confirm("정말 삭제하시겠습니까?")){
				fetch('/api/job/board/'+$("#job_no").val(), {
					method: 'DELETE'
				})
				.then(response => {
					if (!response.ok) {
						throw new Error('Network response was not ok');
					}
					alert("삭제가 완료 되었습니다.");
					$(location).attr('href', '/job/boards');
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
		<input type="hidden" id="job_no" name="job_no" value="${job_no }"/>
		
		<form name="formPopup" id="formPopup">
			<input type="hidden" id="receiver_id" name="receiver_id" value="${detail.user_id }">
			<input type="hidden" id="receiver" name="receiver" value="${detail.user_name }">
		</form>
		
		
		<div class="py-8 text-center">
			<h1 id="job_title" class="font-weight-bold"></h1>
		</div>
		
		<div class="content flex items-center float-right">
			<span id="user_name"></span>
			<div id="msgDiv"></div>
			
			<span id="job_date" class="ml-1"></span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span id="job_hits" class="ml-05"></span>
		</div>
		
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<div>
				<span id="job_status"></span>
			</div>
			 
			<div class="border my-top-75 my-bottom-8" style="padding: 2rem; border-radius: 15px; width: 50rem;" >
				<div>
					<span class="mr-5">지역</span>
					<span id="city"></span>
					<span id="gu"></span>
					<span id="dong"></span>
				</div>
				
				<div>
					<span id="pay_category" class="mr-5"></span>
					<span id="pay"></span>
				</div>
			</div>
			
			<p id="job_content" class="my-4"></p>
		</div>
		
		
		<div class="text-right my-top-7">
			<div id="btn_div"></div>
		</div>
		<div class="py-8">
			<jsp:include page="reply.jsp"/>
		</div>
		<div class="text-center">
			<a id="jobList" href="/job/boards">목록</a>
		</div>
		
		<div class="job-detail-container">
		    <!-- 제목 & 작성자 정보 -->
		    <div class="job-header">
		        <h1 id="job_title" class="job-title"></h1>
		        <div class="job-info">
		            <span id="user_name" class="user-name"></span>
		            <div id="msgDiv" class="inline-flex"></div>
		            <span id="job_date"></span>
		            <span class="job-hits">
		                <img src="/resources/include/images/hits.svg" alt="조회수"> 
		                <span id="job_hits"></span>
		            </span>
		        </div>
		    </div>
		
		    <!-- 기본 정보 영역 -->
		    <div class="job-meta-box">
		        <div class="job-meta-row">
		            <div class="job-meta-label">지역</div>
		            <div class="job-meta-value">
		                <span id="city"></span> 
		                <span id="gu"></span> 
		                <span id="dong"></span>
		            </div>
		        </div>
		        <div class="job-meta-row">
		            <div class="job-meta-label">급여</div>
		            <div class="job-meta-value">
		                <span id="pay_category"></span> 
		                <span id="pay"></span>
		            </div>
		        </div>
		    </div>
		
		    <!-- 모집 완료 표시 (선택적 표시) -->
		    <div class="job-status" id="job_status"></div>
		
		    <!-- 본문 영역 -->
		    <div class="job-content">
		        <p id="job_content"></p>
		    </div>
		
		    <!-- 버튼 영역 -->
		    <div class="job-buttons">
		        <div id="btn_div" class="author-buttons"></div>
		        <a href="/job/boards" class="btn-back">목록</a>
		    </div>
		
		    <!-- 댓글 포함 -->
		    <div class="job-reply">
		        <jsp:include page="reply.jsp"/>
		    </div>
		</div>
	</div>
</body>
</html>