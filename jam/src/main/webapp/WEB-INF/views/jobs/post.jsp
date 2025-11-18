<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - JOB</title>

<script src="/resources/include/dist/js/favorite.js"></script>
<style>
.post-title{
    font-size: 20px;
    font-weight: 700;
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
    color: #444;
    font-size: 16px;
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

.post-actions{
	justify-content: center;
    display: flex;
    align-items: center;
    gap: 12px; 
    margin-top: 16px;
}

#apply {
	background-color: #ff5722; 
	color: #fff;
	padding: 10px 20px;
	border: none;
	border-radius: 6px;
	font-size: 14px;
	font-weight: 600;
	cursor: pointer;
	transition: background-color 0.2s, transform 0.1s, box-shadow 0.2s;
}
#apply:hover {
	background-color: #e64a19; 
	box-shadow: 0 4px 10px rgba(255, 87, 34, 0.3);
	transform: translateY(-2px);
}
#apply:active {
	transform: translateY(1px);
}

.favoriteSpan {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 40px;
	height: 40px;
	border: 1px solid #ccc;
	border-radius: 6px;
	background: #fff;
	cursor: pointer;
	transition: background 0.2s, border-color 0.2s, box-shadow 0.2s;
}
.favoriteSpan:hover {
	background: #f8f9fa;
	border-color: #999;
	box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}
.favoriteSpan:hover {
	color: #ff9800; 
}
.post-buttons {
	position: relative;
	display: flex;
	justify-content: center;  
	align-items: center;
	margin-top: 16px;
	margin-bottom: 16px;
}

.post-buttons .board-btn {
	position: absolute;
	right: 0; 
}
.favorite{
	width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #FFD43B;
    cursor: pointer;
    transition: color 0.2s;
}
.post-view-count{
	margin-left: 15px;
}
.company-name{
	font-weight:800;
	font-size:1.125rem; /* 18px */
	letter-spacing:.2px;
	color:#111827;
}
</style>
	
<script type="text/javascript">

$(function(){
		
	let postId = $("#postId").val();
	getBoard();
		
	function getBoard(){
		if(postId == "" || postId == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
		else{
			fetch('/api/jobs/post/' + postId)
	        .then(res => {
				if (!res.ok) {
					return res.json().then(err => {
			      		throw new Error(err.detail || "요청 처리 중 오류가 발생했습니다.");
			      	})
				}
				return res.json();
			})
	        .then(result => {
				const data = result.post;
				
	        	$("#title").text(data.title);
				
	        	$("#created_at").text(timeAgo(data.created_at));
	        	$("#view_count").text(data.view_count);
	        	
	        	$("#post-content").text(data.content);
		        	
	        	$("#post-city").text(data.city);
	        	$("#post-gu").text(data.gu);
	        	$("#post-dong").text(data.dong);
		        	
	        	$("#position").text(data.position);
	        	
	        	$(".favoriteSpan").attr("data-board-no", data.post_id);
	        	$(".favoriteSpan").attr("data-board-type", "job");
	    		
	    		let $icon = $(".favoriteSpan").find("i"); 
	    		data.favorite ? $icon.addClass("fa-solid")
	    					   : $icon.addClass("fa-regular");
		        
	        	if(data.category == 0){
	        		const payCategoryMap = {
	        			    0: "건별",
	        			    1: "주급",
	        			    2: "월급"
	        		};

	        		let pay_category = payCategoryMap[data.pay_category];

	        		$("#pay_category").text(pay_category);
	        		$("#pay").text(data.pay_category < 3 ? formatNumberKo(data.pay) + "원" : "협의 후 결정");

		        	$("#company-name").text(data.company_name);
	        	}else {
					$(".pay-div").css("display", "none");
					$(".userName").text(data.user_name + "님");
	        	}
	        	
	        	$("#apply").attr("data-category", data.category);
	        	
	        	if(data.status == 1){
	        		$("#status").css("display","block");
	        		$("#status").text("구인 완료된 글 입니다.");
	        		$(".post-actions").remove();
	        	}
	        	
	        	if (result.isAuthor === true) {
	        		$(".post-actions").remove();
	        	}

	        })
	    	.catch(err => {
	    	 	alert(err.message);
	    	});
		}
	}
	
	$("#apply").click(function(){
		var category = $(this).attr("data-category");
				
		var url = "/jobs/applyForm/" + postId + "?category="+category;
	    var option = "width=500, height=610, top=10, left=10";
	    var name = "applyPopup";
	    window.open(url, name, option);
	})
	
	
})

function timeAgo(dateString) {
	let now = new Date();
	let past = new Date(dateString);
	let diff = Math.floor((now - past) / 1000); 

	if (diff < 10) return '방금 전';
	if (diff < 60) return diff + '초 전';
	if (diff < 3600) return Math.floor(diff / 60) + '분 전';
	if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';
	if (diff < 172800) return '어제';
	if (diff < 2592000) return Math.floor(diff / 86400) + '일 전';
	
    let months = Math.floor(diff / 2592000); 
    if (months < 12) return months + '개월 전';
    
	let dateStr = past.toLocaleDateString();
	return dateStr.replace(/\.$/, '');
}

function formatNumberKo(pay) {
	const num = Number(String(pay).replace(/[^\d]/g, ''));
	return num ? num.toLocaleString('ko-KR') : '';
}
</script>
</head>
<body class="wrap">
	<div class="my-top-15 my-bottom-15">
		<input type="hidden" id="postId" name="postId" value="${postId }"/>
		
		<div class="board-detail-container">
		    <!-- 제목 & 작성자 정보 -->
		    <div class="post-header">
		        <p id="title" class="post-title"></p>
		        <div class="post-info">
		        	<i class="fa-solid fa-clock" style="font-size: 16px;"></i>
		        	<span id="created_at"></span>
		        	
		            <span class="post-view-count">
		            	<i class="fa-solid fa-eye"></i>
		                <span id="view_count"></span>
		            </span>
		        </div>
		    </div>
		
		    <!-- 기본 정보 영역 -->
		    <div class="job-meta-box">
		    	<div class="job-meta-row">
		    		<div class="job-meta-label"></div>
		    		<div class="job-meta-value">
		    			<span id="company-name" class="company-name"></span>
		    			<span id="user-name" class="userName"></span>
		    		</div>
		    	</div>
		        <div class="job-meta-row">
		            <div class="job-meta-label">지역</div>
		            <div class="job-meta-value">
		                <span id="post-city"></span> 
		                <span id="post-gu"></span> 
		                <span id="post-dong"></span>
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
		
		    <!-- 모집 완료 표시 -->
		    <div class="job-status" id="status" style="display: none;"></div>
		
		    <!-- 본문 영역 -->
		    <div class="content-div">
		        <p id="post-content"></p>
		    </div>
		
		    <!-- 버튼 영역 -->
		    <div class="post-buttons">
		    	<sec:authorize access="isAuthenticated() and !hasRole('COMPANY')">
		    	<div class="post-actions">
		    		<button id="apply">지원하기</button>
	    			<span class="favoriteSpan">
	    				<i class="favorite fa-star"></i>
					</span>
		    	</div>
		    	</sec:authorize>
		    	<a href="/jobs/board" class="board-btn">목록</a>
		    </div>
		</div>
	</div>
</body>
</html>