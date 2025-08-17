<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - JOB</title>
<style>

.job-meta-box {
    background-color: #f9f9f9;
    border: 1px solid #eee;
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 16px;
    max-width: 400px;
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



/* 이거 지우면 안됨 */
.user_toggle {
    top: 2rem;
}

</style>
	
<script type="text/javascript">
$(function(){
		
	let post_id = $("#post_id").val();
	let authorUserId;
	let authorUserName;
		
	getBoard();
		
	function getBoard(){
		if(post_id == "" || post_id == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
		else{
			fetch('/api/jobs/post/' + post_id)
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
	        	$("#userName").html(data.user_name);
	        	$(".boardUserName").attr("data-userId", data.user_id);
				
	        	$("#job_date").html('🕒' +formatRelativeTime(data.job_date));
	        	$("#job_hits").html('👀' +data.job_hits);
	        	$("#job_content").html(data.job_content);
		        	
	        	$("#city").html(data.city);
	        	$("#gu").html(data.gu);
	        	$("#dong").html(data.dong);
		        	
	        	$("#position").html(data.position);
		        
	        	if(data.job_category == 0){
	        		const payCategoryMap = {
	        			    0: "건별",
	        			    1: "주급",
	        			    2: "월급"
	        		};

	        		let pay_category = payCategoryMap[data.pay_category];

	        		$("#pay_category").html(pay_category);
	        		$("#pay").html(data.pay_category < 3 ? ${data.pay} + "원" : "협의 후 결정");

		        	
	        	}else $(".pay-div").css("display", "none");
				
		        	
	        	if(data.job_status == 1){
	        		$("#job_status").css("display","block");
	        		$("#job_status").html("구인 완료된 글 입니다.");
	        	}
	        	toggleUserMenu();
	         	currentUserIsAuthor(data.author);
	        })
	        .catch(error => console.error('Error:', error));
		}
	}
		
	// 현재 로그인한 사용자와 글쓴이가 같은지 비교하는 함수
	function currentUserIsAuthor(isAuthor){
		console.log(isAuthor);
		// 글쓴이와 현재 로그인한 사용자가 다르면 메세지 버튼 생성
        if (isAuthor) {		        
	        // 글쓴이와 사용자가 같으면 수정, 삭제 버튼 추가
	        var btnDiv = document.getElementById("btn-div");
			
	        // 수정 버튼 생성
	        var updateButton = document.createElement("button");
	        updateButton.type = "button";
	        updateButton.id = "jobUpdateBtn";
	        updateButton.classList.add("board-btn");
	        updateButton.textContent = "수정"; 
			
	        // 삭제 버튼 생성
	        var deleteButton = document.createElement("button");
	        deleteButton.type = "button";
	        deleteButton.id = "jobDeleteBtn";
	        deleteButton.classList.add("board-btn");
	        deleteButton.textContent = "삭제";

	        btnDiv.appendChild(updateButton);
	        btnDiv.appendChild(deleteButton);
	    }
        
	}
	
	
	// 수정 버튼 클릭
	$(document).on("click", "#jobUpdateBtn", function() {
	    if(post_id == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
	    else $(location).attr('href', '/jobs/post/edit/'+post_id);
	});
	
	// 삭제 버튼 클릭
	$(document).on("click", "#jobDeleteBtn", function() {
		if(confirm("정말 삭제하시겠습니까?")){
			fetch('/api/jobs/post/'+$("#post_id").val(), {
				method: 'DELETE'
			})
			.then(response => {
				if (!response.ok) {
					throw new Error('Network response was not ok');
				}
				alert("삭제가 완료 되었습니다.");
				$(location).attr('href', '/jobs/post');
			})
			.catch(error => {
				alert('게시글 삭제를 완료할 수 없습니다. 잠시 후 다시 시도해주세요.');
				console.error('Error : ' , error);
			});
		}
	});
	
	/* 쪽지 아이콘 클릭 */
	$(document).on("click", "#sendMsg", function() {
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

function formatRelativeTime(dateString) {
	
    const postDate = new Date(dateString.replace(' ', 'T').replaceAll('/', '-'));  
    const now = new Date();

    const diffMs = now - postDate;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffDay >= 7) {
    	return dateString.slice(0, -3);
    } else if (diffDay >= 1) {
        return diffDay + '일 전';
    } else if (diffHour >= 1) {
        return diffHour + '시간 전';
    } else if (diffMin >= 1) {
        return diffMin + '분 전';
    } else {
        return '방금 전';
    }
}
		
</script>
</head>
<body class="wrap">
	<div class="my-top-15 my-bottom-15">
		<input type="hidden" id="post_id" name="post_id" value="${post_id }"/>
		
		<form name="formPopup" id="formPopup">
			<input type="hidden" id="receiver_id" name="receiver_id" value="${detail.user_id }">
			<input type="hidden" id="receiver" name="receiver" value="${detail.user_name }">
		</form>
		
		<!--  -->
		<div class="board-detail-container">
		    <!-- 제목 & 작성자 정보 -->
		    <div class="board-header">
		        <p id="job_title" class="board-title"></p>
		        <div class="board-info">
				    <span id="userName" class="userName boardUserName"></span>  
				    <div class="userNameToggle"></div> 
		        	<span class="divider">|</span>
		        	<span id="job_date"></span>
		        	<span class="divider">|</span>
		            <span class="board-hits">
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
		        <div class="job-meta-row pay-div">
		            <div class="job-meta-label">급여</div>
		            <div class="job-meta-value">
		                <span id="pay_category"></span> 
		                <span id="pay"></span>
		            </div>
		        </div>
		        <div class="job-meta-row">
		        	<div class="job-meta-label">포지션</div>
		        	<div class="job-meta-value">
		        		<span id="position"></span>
		        	</div>
		        </div>
		    </div>
		
		    <!-- 모집 완료 표시 (선택적 표시) -->
		    <div class="job-status" id="job_status" style="display: none;"></div>
		
		    <!-- 본문 영역 -->
		    <div class="board-content">
		        <p id="job_content"></p>
		    </div>
		
		    <!-- 버튼 영역 -->
		    <div class="board-buttons">
		        <div id="btn-div" class="author-buttons"></div>
		        <a href="/jobs/board" class="board-btn">목록</a>
		    </div>
		
		</div>
	</div>
</body>
</html>