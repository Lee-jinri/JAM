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
    margin: 0 10px 16px 10px;
    border-bottom: 1px solid #eee;
    padding-bottom: 8px;
}

.job-content{
	min-height: 100px;
}

.job-title {
    font-size: 24px;
    font-weight: bold;
    margin: 0;
}

.job-info {
	display: flex;
	gap: 10px;
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

.job-content {
    margin: 16px 0;
    line-height: 1.6;
}

.job-buttons {
    display: flex;
    gap: 10px;
    align-items: center;
    margin-top: 16px;
    margin-bottom: 16px;
    justify-content: flex-end;
}



.job-btn {
    padding: 8px 16px;  /* 버튼 크기 키우기 */
    background-color: #f8f9fa; /* 연한 그레이 톤 */
    border: 2px solid #ddd; /* 좀 더 도톰한 테두리 */
    border-radius: 12px; /* 둥글게 */
    font-size: 14px; /* 글자 크기 적당히 */
    font-weight: 600; /* 글자 좀 더 귀엽게 */
    color: #333;
    cursor: pointer;
    transition: all 0.2s ease-in-out;
    box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1); /* 살짝 입체감 */
}

.job-btn:hover{
    background-color: #e9ecef; /* hover 시 더 밝게 */
    border-color: #bbb; /* 테두리도 변경 */
    transform: scale(1.05); /* 살짝 커지는 효과 */
}
#btn-div{
	display: flex;
    gap: 10px;
}

.userName-wrapper {
    position: relative; /* 기준점 설정 */
    display: inline-flex;
    align-items: center;
}

.userNameToggle {
    position: absolute;
    top: 100%; /* 닉네임 아래로 배치 */
    left: -25px;
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
	        	$("#userName").html(data.user_name);
	        	$(".userName").attr("data-userId", data.user_id);
				
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
	        updateButton.classList.add("job-btn");
	        updateButton.textContent = "수정"; 
			
	        // 삭제 버튼 생성
	        var deleteButton = document.createElement("button");
	        deleteButton.type = "button";
	        deleteButton.id = "jobDeleteBtn";
	        deleteButton.classList.add("job-btn");
	        deleteButton.textContent = "삭제";

	        btnDiv.appendChild(updateButton);
	        btnDiv.appendChild(deleteButton);
	    }
        
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
	<div class="rem-30 my-top-15 my-bottom-15">
		<input type="hidden" id="job_no" name="job_no" value="${job_no }"/>
		
		<form name="formPopup" id="formPopup">
			<input type="hidden" id="receiver_id" name="receiver_id" value="${detail.user_id }">
			<input type="hidden" id="receiver" name="receiver" value="${detail.user_name }">
		</form>
		
		<!--  -->
		<div class="job-detail-container">
		    <!-- 제목 & 작성자 정보 -->
		    <div class="job-header">
		        <h1 id="job_title" class="job-title"></h1>
		        <div class="job-info"><!-- 
		        	<span id="userName" class="userName"></span>          
		        	<div class="userNameToggle"></div> --> 
		        	<div class="userName-wrapper">
				        <span id="userName" class="userName"></span>  
				        <div class="userNameToggle"></div> 
				    </div>
		        	<span class="divider">|</span>
		        	<span id="job_date"></span>
		        	<span class="divider">|</span>
		            <span class="job-hits">
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
		    <div class="job-content">
		        <p id="job_content"></p>
		    </div>
		
		    <!-- 버튼 영역 -->
		    <div class="job-buttons">
		        <div id="btn-div" class="author-buttons"></div>
		        <a href="/job/boards" class="job-btn">목록</a>
		    </div>
		
		    <!-- 댓글 -->
		    <div class="job-reply">
		        <jsp:include page="reply.jsp"/>
		    </div>
		</div>
	</div>
</body>
</html>