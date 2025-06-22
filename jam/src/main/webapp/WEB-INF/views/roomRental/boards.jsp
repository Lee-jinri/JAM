<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 연습실</title>

<script src="/resources/include/dist/js/userToggle.js"></script>
<script src="/resources/include/dist/js/area.js"></script>
<script src="/resources/include/dist/js/favorite.js"></script>

<style>

.search-div {
    max-width: 700px;   /* 포지션/지역과 동일하게 맞춤 */
    width: 100%;
    margin: 0 auto;     /* 가운데 정렬 */
    padding: 1rem 2rem;
    margin-top: 30px;
}
.search-input{
	border: none;
    border-bottom: 1px solid #e5e7eb;
}

.search-bar-wrapper {
    display: flex;
    gap: 8px; /* 요소 간 간격 */
    width: 100%;
    max-width: 800px; /* 포지션/지역과 동일한 폭 */
}

.search-bar-wrapper select {
    flex: 0.2;  /* select는 전체의 20%만 차지 */
    min-width: 100px; /* 너무 작아지는 거 방지 */
}

.search-bar-wrapper input {
    flex: 1;  /* input은 나머지 공간 차지 */
    min-width: 0;
    box-sizing: border-box;
}

.search-bar-wrapper .glass_icon {
    cursor: pointer;
    font-size: 18px;
    align-self: center;
}


.selected-wrapper {
    max-width: 600px; 
    width: 100%;
    margin: 10px auto;
    text-align: left;
    margin-bottom: 8px;
    min-height: 23px;
}

.selected-location, .selected-position{
    font-weight: bold;
    font-size: 14px;
    color: #333;
}

.switch-container {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 20px 0;
    font-weight: bold;
    font-size: 16px;
}

.switch {
    position: relative;
    display: inline-block;
    width: 50px;
    height: 26px;
}

.switch input {
    opacity: 0;
    width: 0;
    height: 0;
}

.slider {
    position: absolute;
    cursor: pointer;
    top: 0; left: 0; right: 0; bottom: 0;
    background-color: #ddd;
    transition: 0.4s;
    border-radius: 34px;
}

.slider:before {
    position: absolute;
    content: "";
    height: 20px; width: 20px;
    left: 3px; bottom: 3px;
    background-color: white;
    transition: 0.4s;
    border-radius: 50%;
}

/* 기본 색상 (unchecked 상태일 때) */
.slider {
    background-color: #003366;  
}

input:checked + .slider {
    background-color: #ffdd77;
}

input:checked + .slider:before {
    transform: translateX(24px);
}

.filterBtnContainer{
    gap: 10px; /* 버튼 사이 간격 */
    align-items: center; /* 버튼 높이 정렬 */
	max-width: 700px;
    margin: 10px auto;	
}
.setting-filter {
    display: none;
    overflow: hidden;
    border: 1px solid #ddd;
    border-radius: 6px;
    background-color: #fff;
    max-width: 800px;  /* 컨테이너 최대 너비 (원하는 값으로 조절 가능) */
    margin: 0 auto;  /* 중앙정렬 */
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);  /* 살짝 그림자 */
    padding: 10px;
}



.setting-base-row {
    display: flex;
    justify-content: space-between;
    gap: 8px;  /* 각 컬럼 사이 간격 */
}

.setting-base__col--title {
    flex: 1;
    height: 40px;
    line-height: 40px;
    font-weight: 600;
    font-size: 1.2rem;
    text-align: center;
    border-bottom: 1px solid #e5e8ec;
    color: #4c515b;
    background-color: #f7f8fa;
    border-right: 1px solid #e5e8ec;
}



.setting-base__col {
    flex: 1;  /* 3등분으로 균등 배분 */
    border: 1px solid #e5e8ec;
    border-radius: 4px;
    background-color: #f9f9f9;
    overflow: hidden;
}

.setting-base__col--list {
    max-height: 200px;  /* 높이 제한 (스크롤 추가 가능) */
    overflow-y: auto;
    background-color: #fff;
    padding: 8px;
    border: 1px solid #e5e8ec;
    border-radius: 4px;
}

.setting-base__col--list ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.setting-base__col--list li {
    padding: 8px 12px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;
    border-bottom: 1px solid #f0f0f0;
}

.setting-base__col--list li:last-child {
    border-bottom: none;
}

.setting-base__col--list li:hover {
    background-color: #f0f5ff;
}

.setting-base__col--list .placeholder {
    color: #aaa;
    font-style: italic;
    text-align: center;
}

	
.button-group {
    display: flex;
    gap: 10px; /* 버튼 간격 */
}

.filter-btn {
    position: relative;
    padding: 10px 16px;
    font-size: 14px;
    color: #333;
    background-color: #f9f9f9;
    border: 1px solid #ddd;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px; /* 텍스트와 아이콘 간격 */
    font-weight: 500;
    transition: background-color 0.3s;
}

.filter-btn:hover {
    background-color: #f0f0f0;
}

.filter-btn .icon {
    display: flex;
    align-items: center;
    justify-content: center;
}

.filter-btn i {
    font-size: 12px;
    color: #555;
}

#positionContainer {
    background: #f9f9f9;
    padding: 16px;
    border: 1px solid #ddd;
    border-radius: 6px;
    max-width: 800px; /* 필요 시 조정 */
    margin-top: 8px;
}

/* 포지션 그리드 레이아웃 */
.position-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); /* 반응형 */
    gap: 8px; /* 항목 간격 */
}

/* 개별 포지션 항목 (체크박스 + 라벨) */
.position-grid label {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s, box-shadow 0.2s;
}

/* 선택 시 강조 */
.position-grid input:checked + span {
    font-weight: bold;
    color: #007bff;
}

/* hover 시 효과 */
.position-grid label:hover {
    background-color: #f0f0f0;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* 기본 체크박스 숨기고 커스텀 디자인 */
.position-grid input[type="checkbox"] {
    width: 16px;
    height: 16px;
    accent-color: #007bff; /* 체크 색상 */
}

.remove-btn, .area-remove-btn{
    background: none;
    border: none;
    color: #ff6b6b;
    font-weight: bold;
    font-size: 14px;
    cursor: pointer;
}
</style>	
<script type="text/javascript">
$(function(){
	toggleUserMenu();
	
	getBoards();
	
	$(".boardLink").click(function(e){
		if (!$(e.target).hasClass('userName')) {
			
			var locationUrl = $(this).data('location');
		    location.href = locationUrl;
	   }
	})
	
	$(document).on("click", ".boardLink", function (e) {
		e.preventDefault();
		var location = $(this).attr("data-location");
		if (location) {
			window.location.href = location;
		}
	});
	


	$("#roomWriteBtn").click(function(){
		fetch("/api/member/auth/check").then((res) => {
			if (res.status === 401) {
				if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
					location.href = "/member/login";
				} else {
					location.href = "/roomRental/boards";
				}
			} else {
				location.href = "/roomRental/board/write";
			}
		})
	})
		
			
	$("#searchBtn").click(function(){
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
		
		let city = $("#city").val();
		let gu = $("#gu").val();
		let dong = $("#dong").val();
		
		let data = {
				search : search,
				keyword: keyword,
				city: city,
				gu: gu,
				dong: dong
		}
		
		updateUrl(data);
		
	})
	
	/*
	$(".remove-btn").click(function(){
		removePosition();
	})*/
	
	$("#areaBtn").click(function() {
		const $icon = $(this).find("i");
		
	    if ($("#areaContainer").css("display") === "none") {
	        $("#areaContainer").css("display", "block");
	        
	        $icon.removeClass("fa-caret-down").addClass("fa-caret-up");
	    } else {
	        $("#areaContainer").css("display", "none");
	        $icon.removeClass("fa-caret-up").addClass("fa-caret-down");
	    }
	});

})

function getBoards(){
	
	let params = new URLSearchParams(window.location.search);
	
	let pageNum = params.get("pageNum") || "1";
	let search = params.get("search") || "all";
	let keyword = params.get("keyword") || undefined;
	
    let city = params.get("city") || undefined;
    let gu = params.get("gu") || undefined;
    let dong = params.get("dong") || undefined;
	
    
	let queryParams  = {
	    pageNum: pageNum,
	    search: search,
	    keyword: keyword,
	    city: city,
	    gu: gu,
	    dong: dong
	};

	let filteredParams = Object.fromEntries(
	    Object.entries(queryParams ).filter(([_, v]) => v !== undefined)
	);

	let queryString = new URLSearchParams(filteredParams).toString();
	let url = "/api/roomRental/boards?" + queryString;
	
    if(city) updateSelectedArea(city, gu, dong);

	fetch(url)
	.then(response=>{
		return response.json();
	}).then(data=>{
		renderList(data);
	})
}


function renderList(data){
	let $template = $("#boardTemplate");
	let $boardList = $("#boardList");
	let $roomHeader = $("#roomListHeader");
	
	$roomHeader.empty();
	$roomHeader.nextAll().remove();
	
	$roomHeader.append(
		    '<div class="pd-2rem company-list-row text-alignC items-center">' +
		        '<span></span>' +
		        '<span>제목</span>' +
		        '<span>연습실 위치</span>' +
		        '<span>가격</span>' +
		        '<span>등록일</span>' +
		    '</div>'
	);

	$("#boardDiv").addClass("company-list-row");
	
	data.roomList.forEach(board => {
		let $clone = $template.clone().removeAttr("id").show();
		
		$clone.find(".boardArea").text(board.gu + "\u00A0\u00A0\u00A0" + board.dong);
		$clone.find(".boardTitle").text(board.roomRental_title);
		$clone.find(".boardLink").attr("data-location", "/roomRental/board/" + board.roomRental_no);
		
		let $favoriteSpan = $clone.find(".favoriteSpan");
		$favoriteSpan.attr("data-board-no", board.roomRental_no);
		$favoriteSpan.attr("data-board-type", "roomRental");
		
		let $icon = $favoriteSpan.find("i"); 
		board.favorite ? $icon.addClass("fa-solid")
					   : $icon.addClass("fa-regular");
		
		let date = formatRelativeTime(board.roomRental_date);
		$clone.find(".boardDate").text(date);
		
		$boardList.append($clone);
	});
	
	loadPagination(data.pageMaker);
}


function loadPagination(pageMaker) {
    const $pagination = $("#pagination");
    $pagination.empty(); // 기존 페이지 버튼 초기화
    
    for (let num = pageMaker.startPage; num <= pageMaker.endPage; num++) {
        $pagination.append(
            '<li class="paginate_button">' +
                '<a href="#" data-page="' + num + '" class="font-weight-bold ' + (pageMaker.cvo.pageNum === num ? 'selected_btn' : 'default_btn') + '">' + num + '</a>' +
            '</li>'
        );
    }


    // 클릭 이벤트 추가 (페이지 이동)
    $("#pagination a").click(function (e) {
        e.preventDefault();
        let pageNum = $(this).data("page");
        
        updateUrl({pageNum : pageNum});
    });
}


function formatRelativeTime(dateString) {
	
    const postDate = new Date(dateString.replaceAll('/', '-')); 
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
	<div class="content company-mode" >
		<div class="my-top-15 my-bottom-15">
			<div class="title room-title">
				<p class="text-center font-color-blue"></p>
			</div>
		
			<div class="search-div flex justify-center items-center border border-radius-43px">
				<div class="search-bar-wrapper item-center flex justify-space-around">
					
					<% String searchParam = request.getParameter("search");
					    if (searchParam == null || searchParam.isEmpty()) {
					        searchParam = "all";
					    }%> 
					   
					<select id="search" name="search" class="search border-none">
						<option value="all" ${searchParam == 'all' ? 'selected' : ''}>전체</option>
					    <option value="room_title" ${param.search == 'room_title' ? 'selected' : ''}>제목</option>
					    <option value="room_content" ${param.search == 'room_content' ? 'selected' : ''}>내용</option>
					    <option value="user_name" ${param.search == 'user_name' ? 'selected' : ''}>작성자</option>
					</select>
					
					<input type="text" name="keyword" id="keyword" class=" rem-2 search search-input"
					value="${not empty param.keyword ? param.keyword : ''}" />
					<input type="hidden" id="city">
					<input type="hidden" id="gu">
					<input type="hidden" id="dong">
					
					<i id="searchBtn" class="glass_icon fa-solid fa-magnifying-glass"></i>
				</div>
			</div>
			
			<div class="selected-wrapper">
			    <div id="selectedAreaWrapper" class="selected-location inline">
			        <span id="selectedArea"></span>
			    </div>
			</div>
			<div>
				<div class="filterBtnContainer flex">
				    <button id="areaBtn" class="filter-btn">
				        지역
				        <span class="icon"><i class="fa-solid fa-caret-down"></i></span>
				    </button>
				</div>
				<div>
					<div id="areaContainer" class="setting-filter">
						<div class="setting-base-row">
							<div class="setting-base__col setting-base__col--title">시·도</div>
							<div class="setting-base__col setting-base__col--title">구·군</div>
							<div class="setting-base__col setting-base__col--title">동·읍·면</div>
						</div>
						<div class="setting-base-row">
							<div class="setting-base__col setting-base__col--list">
								<ul id="cityList">
									<li class="city cursor-pointer" data-city="서울">서울</li>
					                <li class="city cursor-pointer" data-city="부산">부산</li>
					                <li class="city cursor-pointer" data-city="대구">대구</li>
					                <li class="city cursor-pointer" data-city="인천">인천</li>
					                <li class="city cursor-pointer" data-city="광주">광주</li>
					                <li class="city cursor-pointer" data-city="대전">대전</li>
					                <li class="city cursor-pointer" data-city="울산">울산</li>
					                <li class="city cursor-pointer" data-city="세종">세종</li>
					                <li class="city cursor-pointer" data-city="경기">경기</li>
					                <li class="city cursor-pointer" data-city="강원">강원</li>
					                <li class="city cursor-pointer" data-city="충북">충북</li>
					                <li class="city cursor-pointer" data-city="충남">충남</li>
					                <li class="city cursor-pointer" data-city="전북">전북</li>
					                <li class="city cursor-pointer" data-city="전남">전남</li>
					                <li class="city cursor-pointer" data-city="경북">경북</li>
					                <li class="city cursor-pointer" data-city="경남">경남</li>
					                <li class="city cursor-pointer" data-city="제주">제주</li>
								</ul>
							</div>
							<div class="setting-base__col setting-base__col--list">
					            <ul id="guList">
					                <li class="placeholder">구 선택</li>
					            </ul>
					        </div>
					
					        <!-- 동 목록 -->
					        <div class="setting-base__col setting-base__col--list">
					            <ul id="dongList">
					                <li class="placeholder">동 선택</li>
					            </ul>
					        </div>
						</div>							
					</div>	
				</div>	
			</div>
				
			
			
			<div class="justify-between py-4 flex">
				<div class="write_btn write_btn_border write_border flex items-center border-radius-7px">
					<button type="button" id="roomWriteBtn" class="write_btn_font border-none bColor_fff ">작성하기</button>
				</div>
			</div>
			<div>
				<ul style="display:none">
					<li id="boardTemplate" class="border-bottom">
						<div id="boardDiv" class="pd-2rem room-list-row text-alignC items-center">
							<span class="favoriteSpan">
								<i class="favorite fa-star" style="color: #FFD43B; cursor: pointer;"></i>
							</span>
							<span class="font-weight-bold font-size-5 boardTitle boardLink"  style="cursor: pointer;"></span>
							<span class="boardArea"></span>
					        <span class="boardPrice"></span>
					        <span class="boardDate"></span>
						</div>
					</li>
				</ul>
				<ul id="boardList">
					<li id="roomListHeader" class="border-top border-bottom">
					</li>
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