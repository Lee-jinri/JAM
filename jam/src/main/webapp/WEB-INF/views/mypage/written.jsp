<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM- 글 모아보기</title>
<style>

.search-container {
	display: flex;
	align-items: center;
	background-color: white;
	border: 1px solid #ddd;
	border-radius: 9999px; /* pill shape */
	padding: 8px 8px;
	width: 100%;
	max-width: 600px;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
	height: 53px;
}

.search-icon {
	color: #333;
	font-size: 20px;
}

.search-input {
	border: none;
	flex: 1;
	outline: none;
	font-size: 14px;
}

.search-select {
	border: none;
	appearance: none;
	-webkit-appearance: none;
	-moz-appearance: none;
	background-color: #f2eadd;
	border-radius: 9999px;
	padding: 8px 25px;
	font-size: 14px;
	cursor: pointer;
	text-align-last: center;
	padding-right: 2em;
	background-image:
		url("data:image/svg+xml;utf8,<svg fill='black' height='12' viewBox='0 0 24 24' width='12' xmlns='http://www.w3.org/2000/svg'><path d='M7 10l5 5 5-5z'/></svg>");
	background-repeat: no-repeat;
	background-position: right 0.75em center;
	background-size: 0.8em;
}



.search-select option {
	direction: rtl;
	text-align: center;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #f9f9f9;
  border-radius: 12px;
  padding: 14px 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  max-width: 200px;
}
.profile-info .nickname {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}


.profile-icon {
  width: 48px;
  height: 48px;
  background-color: #90c4f3;
  border-radius: 50%;
  color: white;
  font-weight: bold;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-btn-div{
	margin-left: 7px;
}
/*
.profile-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  padding: 6px 10px;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}*/

.profile-btn{
	border: none;
    background-color: unset;
    padding: 0;
}
.profile-btn:hover {
  background-color: #f0f0f0;
  border-color: #999;
}

.profile-btn:active {
  background-color: #e0e0e0;
}

.written-container {
    max-width: 800px;
    margin: 40px auto;
    padding: 20px;
    background-color: #ffffff;
    border-radius: 10px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.written-title {
    text-align: center;
    color: #333;
}

.written-list {
    list-style: none;
    padding: 0;
}

.written-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 15px;
    margin-bottom: 10px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.written-item:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
    background-color: #f1f1f1;
}

.written-item a {
    text-decoration: none;
    font-weight: bold;
    color: #666;
}

.written-item a:hover {
    text-decoration: underline;
}

.written-icon {
    color: gold;
    font-size: 20px;
}

.written-info {
    flex-grow: 1;
    margin-left: 10px;
}

.written-meta {
    font-size: 14px;
    color: #666;
}


.button-group {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 50px;
}

.button-group button {
    padding: 10px 15px;
    border: 2px solid #BFC8EA; 
    border-radius: 8px; 
    cursor: pointer;
    background-color: #BFC8EA;
    color: white;
    font-size: 16px;
    font-weight: bold;
    transition: all 0.3s ease-in-out;
}

.button-group button:hover {
    background-color: #A3B1D1;
    border-color: #A3B1D1;
}

.button-group button.active {
    background-color: white;   
    color: #4A4F6A;  
    border: 2px solid #4A4F6A;
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15); 
}

.paginate-a {
	background-color: #BFC8EA;
}

/* 이거 지우면 안됨*/
ul.pagination > li > a.selected_btn {
  background-color: #BFC8EA;
  color: white;
  border: 1px solid transparent;
}

</style>
<script>
$(function(){
    getWrittenList();
    
    let params = new URLSearchParams(window.location.search);
	let user_id = params.get("user_id");
	let boardType = params.get("boardType") || "community";
    
	$(".button-group button").each(function() {
        if ($(this).attr("data-boardType") === boardType) {
            $(this).addClass("active");
        } else {
            $(this).removeClass("active"); 
        }
    });
	
	$(".button-group button").click(function() {
        // 모든 버튼에서 'active' 클래스 제거
        $(".button-group button").removeClass("active");
        // 클릭한 버튼에 'active' 클래스 추가
        $(this).addClass("active");
        
        newBoardType = $(this).attr('data-boardType');
        //let user_id = params.get("user_id");
        
        let newUrl = window.location.pathname + "?boardType=" + newBoardType + "&user_id=" + user_id;
        window.history.pushState(null, "", newUrl);
        
        getWrittenList();
    });
	
	$(document).on("click", ".boardLink", function (e) {
	    e.preventDefault();
	    var location = $(this).attr("data-location");
	    
	    if (location) {
	        window.location.href = location;
	    }
	});
	
	// 검색 
	$("#searchBtn").click(function(){
		handleSearch(params.get("user_id"), boardType);
	})
	
	document.getElementById("keyword").addEventListener("keydown", function(event) {
	    if (event.key === "Enter") {
	        event.preventDefault();
	        handleSearch(params.get("user_id"), boardType);
	    }
	});

	$("#message").click(function(){
		
	})
	
	$("#chat").click(function(){
		
	})
})



function getWrittenList() {
	let params = new URLSearchParams(window.location.search);
	let user_id = params.get("user_id");
	let boardType = params.get("boardType") || "community";
	let pageNum = parseInt(params.get("pageNum")) || 1;
	let search = params.get("search");
	let keyword = params.get("keyword");
	
	if(user_id === null || user_id === ''){
		alert("시스템 오류 입니다. 잠시 후 다시 시도해주세요.");
		location.href = '/';
	}
	
    let url = '/api/mypage/written/boards?boardType=' + boardType + '&user_id=' + user_id + '&pageNum=' + pageNum;
    
    fetch(url)
    .then(response => {
    	if(response.status == 401){
    		
    		return response.text().then(text => { 
    			if (confirm("로그인이 필요한 서비스 입니다. 로그인 하시겠습니까?")) {
    				location.href = "/member/login"; 
    			} else {
    				location.href = '/';
    			}
    		});
    	}else if (!response.ok) {
    		throw new Error("서버 응답 오류: " + response.status + ": " +  response.statusText);
        }
    	
        return response.json();
    })
    .then(data => {
    	
		renderUserHeader(data.userProfile, data.isMine); 
        renderList(data.writtenList, boardType);
        loadPagination(data.pageMaker, boardType);
    })
    .catch(error => {
    	alert("시스템 오류 입니다. 잠시 후 다시 시도해주세요.");
        console.error("데이터 불러오기 실패:", error);
    });
}

function renderUserHeader(userProfile, isMine){
	if(isMine){
		$(".written-title").text("📖 내 글 모아보기");
	}else{
		$("#userNickname").text(userProfile.user_name);
	}
}


function renderList(writtenList, boardType){
	
	let $writtenUl = $("#written-list");
	$writtenUl.empty(); // 기존 목록 비우기
	
	if(writtenList.length === 0){
		const $li = $("<li>").addClass("empty-message").css({
	        padding: "25px",
	        textAlign: "center",
	        color: "#666"
	    });

	    const $span = $("<span>").text("아직 작성한 글이 없어요.🫠");
	    $li.append($span);

	    $writtenUl.append($li);
	    return false;
	}
	
	writtenList.forEach(written => {
        const $li = $("<li>").addClass("boardLink written-item cursor-pointer")
            .attr("data-location", "/" + boardType + "/board/" + written.board_no);

        const $infoDiv = $("<div>").addClass("written-info");
        const $titleSpan = $("<span>").addClass("written-title").text(written.board_title);
        const $metaDiv = $("<div>").addClass("written-meta").text(written.created_at);
        $infoDiv.append($titleSpan, $metaDiv);

        $li.append($infoDiv);
        $writtenUl.append($li);
    });
	
}

function handleSearch(user_id, boardType){
	let search = $("#search").val();
	let keyword = $("#keyword").val();
	
	if(search == "all"){
		search = undefined; 
        keyword = undefined;
	}
	else{
		if(keyword.replace(/\s/g, "") == ""){
			alert("검색어를 입력하세요.");
			$("#keyword").focus();
			return;
		}
	}
	
    let url = "/mypage/written?&boardType=" + boardType + "&user_id="+user_id + "&search=" + search + "&keyword=" + keyword;
    
	window.location.href = url;
}

function loadPagination(pageMaker, boardType) {
    const $pagination = $("#pagination");
    
    $pagination.empty(); // 기존 페이지 버튼 초기화

    // 페이지 번호 버튼
    for (let num = pageMaker.startPage; num <= pageMaker.endPage; num++) {
        $pagination.append(
            '<li class="paginate_button">' +
                '<a href="#" data-page="' + num + '" class="paginate-a font-weight-bold ' + (pageMaker.cvo.pageNum === num ? 'selected_btn' : 'default_btn') + '">' + num + '</a>' +
            '</li>'
        );
    }

    // 클릭 이벤트 추가 (페이지 이동)
    $("#pagination a").click(function (e) {
        e.preventDefault();
        let pageNum = $(this).data("page") || 1; 
        
        let params = new URLSearchParams(window.location.search);
    	let user_id = params.get("user_id");
	    
	    let url = "/mypage/written?pageNum="+pageNum + "&boardType=" + boardType + "&user_id="+user_id;
        
		window.location.href = url;
    });
}
</script>
</head>
<body class="wrap">
	<div class="content">
		<h2 class="written-title"></h2>
		<div class="user-profile">
			<div class="profile-icon">👤</div>
			<div class="profile-info">
		    <div class="nickname">
		    	<span id="userNickname" style="margin-left: 7px">홍길동</span>
		    	<div class="profile-btn-div">
			    	<button id="message" class="profile-btn">✉️</button>
			    	<button id="chat" class="profile-btn">💬</button>
		    	</div>
		    </div>
		    <div>
		    	
		    </div>
		    <!-- <div class="bio">한 줄 소개</div> -->
			</div>
		</div>

        <div>
        
	        <div class="button-group">
			    <button data-boardType="community">커뮤니티</button>
			    <button data-boardType="fleaMarket">중고악기</button>
			    <button data-boardType="roomRental">연습실</button>
			    <button data-boardType="job">Jobs</button>
			</div>
			<div class="search-div flex justify-center items-center ">
				<div class="search-container">
					<i id="searchBtn" class="fa-solid fa-magnifying-glass search-icon"></i>
					<input type="text"  id="keyword" class="search-input" name="keyword" placeholder="검색어를 입력하세요" />
					<select id="search" name="search" class="search-select">
						<option value="all" ${searchParam == 'all' ? 'selected' : ''}>전체</option>
					    <option value="title" ${param.search == 'title' ? 'selected' : ''}>제목</option>
					    <option value="content" ${param.search == 'content' ? 'selected' : ''}>내용</option>
					    <option value="user_name" ${param.search == 'user_name' ? 'selected' : ''}>닉네임</option>
					</select>
				</div>
			</div>
			<!-- 
			<div class="search-div flex justify-center items-center border border-radius-43px">
				<div class="search-bar-wrapper item-center flex justify-space-around">
					
					<% String searchParam = request.getParameter("search");
					    if (searchParam == null || searchParam.isEmpty()) {
					        searchParam = "all";
					    }%> 
					   
					<select id="search" name="search" class="search border-none">
						<option value="all" ${searchParam == 'all' ? 'selected' : ''}>전체</option>
					    <option value="job_title" ${param.search == 'job_title' ? 'selected' : ''}>제목</option>
					    <option value="job_content" ${param.search == 'job_content' ? 'selected' : ''}>내용</option>
					    <option value="user_name" ${param.search == 'user_name' ? 'selected' : ''}>작성자</option>
					</select>
					
					<input type="text" name="keyword" id="keyword" class=" rem-2 search search-input"
					value="${not empty param.keyword ? param.keyword : ''}" />
					<input type="hidden" id="city">
					<input type="hidden" id="gu">
					<input type="hidden" id="dong">
					
					<i id="searchBtn" class="glass_icon fa-solid fa-magnifying-glass"></i>
				</div>
			</div>	 -->
			
		</div>	
        <div class="written-container">
            <ul id="written-list" class="written-list">
               
            </ul>
        </div>
        
        <div>
			<div class="text-center">
			    <ul id="pagination" class="pagination pagination_border"></ul>
			</div>
		</div>
    </div>
</body>
</html>