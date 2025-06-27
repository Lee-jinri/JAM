<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM- 글 모아보기</title>
<style>
.written-div{
	margin-top: 60px;
}
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
width: inherit;
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
.search-select:focus {
  outline: none;
  box-shadow: none;
}



.search-select option {
	direction: rtl;
	text-align: center;
	background-color: #fff;
}

.user-profile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  background-color: #f9f9f9;
  border-radius: 12px;
  padding: 14px 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  width: fit-content;
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

#userNickname{
	font-size: 16px;
    font-weight: 600;
    color: #7c7c7c;
}


.profile-icon {
  width: 40px;
  height: 40px;
  background-color: #c7e7f3;
  border-radius: 50%;
  color: white;
  font-weight: bold;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-btn-div{
	margin-left: 4px;
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
    font-size: 20px;
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
    margin: 0 auto;
    padding: 20px;
    background-color: #ffffff;
    border-radius: 10px;
    box-shadow: 0 4px 10px #EAE4DC;
    border: 3px solid #EAE4DC;
}

.written-title {
    text-align: center;
    color: #333;
    margin-top: 60px;
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
    justify-content: start;
    gap: 10px;
    margin-top: 50px;
    margin-left: 10px;
}

.button-group button {
    padding: 10px 15px;
    border: 2px solid #EAE4DC;
    border-top-left-radius: 8px;
    border-top-right-radius: 8px;
    cursor: pointer;
    background-color: #EAE4DC;
    color: #6e6e6e;
    font-size: 16px;
    font-weight: bold;
    transition: all 0.3s ease-in-out;
    box-shadow: 0 4px 10px #EAE4DC;
}

.button-group button:hover{
	background-color: #e8e0d4; 
	color: #2e2e2e;          
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
	transition: all 0.2s ease-in-out;
	text-decoration: underline;
}

.button-group button.active:hover{
	color: #2e2e2e;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
	transition: all 0.2s ease-in-out;
	text-decoration: underline;
}

.button-group button.active {
    background-color: white;    
    color: #6e6e6e;
    box-shadow: 0 4px 10px #EAE4DC;
    border: 3px solid #EAE4DC;
    border-bottom: none;
    z-index: 2;
}

.paginate-a {
	background-color: #BFC8EA;
}

ul.pagination > li > a.selected_btn {
	background-color: #f2eadd;
	color: #6e6e6e;
	border: 1px solid transparent;
}

@media screen and (max-width: 768px) {
	.written-div{
		margin: 60px 30px;
	}
	.user-profile{
		padding: 2px 10px;
	}
	#userNickname{
		font-size: 13px;
	}
	.search-container{
		height: 40px;
	}
	.search-select{
		padding:2px 18px 2px 12px;
        font-size: 12px;
	}
	.button-group button{
		font-size: 13px;
	    padding: 0 13px;
	    height: 40px;
	}
	.written-meta{
		font-size: 12px;
	}
	.written-item{
		padding: 7px;
   		font-size: 13px;
	}
	.button-group{
		margin-top: 26px;
	}
	.search-div{
		margin-top: 12px;
	}
	.search-icon {
    	font-size: 17px;
    }
    .profile-icon{
    	width: 30px;
	    height: 30px;
	    font-size: 15px;
    }
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
        
        $("#keyword").val('');
        $("#search").val('all');
        
        newBoardType = $(this).attr('data-boardType');
        
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
		handleSearch(params.get("user_id"));
	})
	
	document.getElementById("keyword").addEventListener("keydown", function(event) {
	    if (event.key === "Enter") {
	        event.preventDefault();
	        handleSearch(params.get("user_id"), boardType);
	    }
	});
	
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
    
    if(search && keyword) {
		url += "&search=" + search + "&keyword=" + keyword;
    }
    
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
    	
		$("#userNickname").text(data.userProfile.user_name + "님");
		
        renderList(data.writtenList, boardType);
        loadPagination(data.pageMaker, boardType);
    })
    .catch(error => {
    	alert("시스템 오류 입니다. 잠시 후 다시 시도해주세요.");
        console.error("데이터 불러오기 실패:", error);
    });
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
        const $metaDiv = $("<div>").addClass("written-meta").text(written.created_at);
        const $titleSpan = $("<span>").addClass("written-title").text(written.board_title);
        
        $infoDiv.append($metaDiv,$titleSpan);

        $li.append($infoDiv);
        $writtenUl.append($li);
    });
	
}

function handleSearch(user_id){

	let search = $("#search").val();
	let keyword = $("#keyword").val().trim();

	let boardType = new URLSearchParams(window.location.search).get("boardType") || "community";
	
	let url = "/mypage/written?&boardType=" + boardType + "&user_id="+user_id;
			
	if(search == "all"){
		$("#keyword").val('');
	} else{
		if(keyword.replace(/\s/g, "") == ""){
			alert("검색어를 입력하세요.");
			$("#keyword").focus();
			return;
		}else{
			url += "&search=" + encodeURIComponent(search) + "&keyword=" + encodeURIComponent(keyword);
		}
	}
	
	window.history.pushState(null, "", url);
    
	getWrittenList();
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
		<div class="written-div">
			<div class="user-profile">
				<div class="profile-icon">👤</div>
				<div class="profile-info">
				    <div class="nickname">
				    	<span id="userNickname" style="margin-left: 7px"></span>
				    </div>
				</div>
				<div class="profile-btn-div">
					<button id="chat" class="profile-btn">💬</button>
				</div>
			</div>
	
	        <div>
	        
		        
				<div class="search-div flex justify-center items-center ">
					<div class="search-container">
						<i id="searchBtn" class="fa-solid fa-magnifying-glass search-icon cursor-pointer"></i>
						<input type="text"  id="keyword" class="search-input" name="keyword" placeholder="검색어를 입력하세요" value="${param.keyword}" />
						<select id="search" name="search" class="search-select">
							<option value="all" ${searchParam == 'all' ? 'selected' : ''}>전체</option>
						    <option value="title" ${param.search == 'title' ? 'selected' : ''}>제목</option>
						    <option value="content" ${param.search == 'content' ? 'selected' : ''}>내용</option>
						</select>
					</div>
				</div>
				
			</div>	
			<div class="button-group">
				<button data-boardType="community">커뮤니티</button>
				<button data-boardType="fleaMarket">중고악기</button>
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
    </div>
</body>
</html>