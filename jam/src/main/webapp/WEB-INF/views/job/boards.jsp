<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - Jobs</title>
<script src="/resources/include/dist/js/userToggle.js"></script>
<script src="/resources/include/dist/js/area.js"></script>
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
let memberListCache = null;
let companyListCache = null;
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
	

	$('#typeSwitch').on('change', function() {
	    if ($(this).is(':checked')) {
	    	
	        if (memberListCache) {
	        	console.log('memberListCache');
	            $('.job-boardList').html(memberListCache);  // 캐시에서 불러오기
	        } else {
	        	location.href = "/job/boards?job_category=1"
	        }
	        
	        
	    } else {
	    	
	        // 기업 구인 리스트 표시 
	       if(companyListCache){
	    	   $(".boardList").html(companyListCache);
	       }else{
	        	location.href = "/job/boards?job_category=0"
	       }
	       
	       setCompanyStyle();
	    }
	});


	$("#jobWriteBtn").click(function(){
		fetch('/api/member/checkAuthentication')
		.then(response =>{
			if(!response.ok){
				alert('시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
				locaion.attr('/job/boards');
			}
			return response.json();
		})
		.then((data) => {
			if(data.authenticated) $(location).attr('href', '/job/board/write');
			else if(confirm("로그인 후 이용할 수 있는 서비스 입니다. 로그인 하시겠습니까?"))$(location).attr('href', '/member/login');
			
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
		
		let job_category;
		if ($("#typeSwitch").is(":checked")) job_category = "1"; else job_category = "0";
		
		let city = $("#city").val();
		let gu = $("#gu").val();
		let dong = $("#dong").val();
		
		let data = {
				search : search,
				keyword: keyword,
				job_category: job_category ,
				city: city,
				gu: gu,
				dong: dong,
				positions: getSelectedPositions()
		}
		
		updateUrl(data);
		
	})
	
	$("#positionBtn").click(function() {
	    const $icon = $(this).find("i");
	
	    if ($("#positionContainer").css("display") === "none") {
	        $("#positionContainer").css("display", "block");
	        $("#areaContainer").css("display", "none");
	
	        // ▼ → ▲ 변경
	        $icon.removeClass("fa-caret-down").addClass("fa-caret-up");
	    } else {
	        $("#positionContainer").css("display", "none");
	
	        // ▲ → ▼ 변경
	        $icon.removeClass("fa-caret-up").addClass("fa-caret-down");
	    }
	});

	
	/*
	$(".remove-btn").click(function(){
		removePosition();
	})*/
	
	$("#areaBtn").click(function() {
		const $icon = $(this).find("i");
		
	    if ($("#areaContainer").css("display") === "none") {
	        $("#areaContainer").css("display", "block");
	        $("#positionContainer").css("display", "none");
	        
	        $icon.removeClass("fa-caret-down").addClass("fa-caret-up");
	    } else {
	        $("#areaContainer").css("display", "none");
	        $icon.removeClass("fa-caret-up").addClass("fa-caret-down");
	    }
	});
	
	$(document).on("change", ".position-checkbox", function() {
	    updateSelectedPosition();
	});

})

function getBoards(){
	
	let params = new URLSearchParams(window.location.search);
	
	let pageNum = params.get("pageNum") || "1";
	let search = params.get("search") || "all";
	let keyword = params.get("keyword") || undefined;
	let job_category = params.get("job_category") || "0";  
	
    let city = params.get("city") || undefined;
    let gu = params.get("gu") || undefined;
    let dong = params.get("dong") || undefined;
	
    let positions = params.getAll("positions");
    
	let queryParams  = {
	    pageNum: pageNum,
	    search: search,
	    keyword: keyword,
	    job_category: job_category,
	    city: city,
	    gu: gu,
	    dong: dong,
	    positions: positions.length > 0 ? positions : undefined
	};

	let filteredParams = Object.fromEntries(
	    Object.entries(queryParams ).filter(([_, v]) => v !== undefined)
	);

	let queryString = new URLSearchParams(filteredParams).toString();
	let url = "/api/job/boards?" + queryString;
	
    $(".position-checkbox").each(function() {
        const posValue = $(this).val();
        if (params.getAll("positions").includes(posValue)) {
            $(this).prop("checked", true);
        }
    });
    updateSelectedPosition();
    if(city) updateSelectedArea(city, gu, dong);

	fetch(url)
	.then(response=>{
		return response.json();
	}).then(data=>{
		
		if(job_category == "0") {
			companyRecruit(data);
			setCompanyStyle();
			$('#typeSwitch').prop('checked', false);
		}
		else{
			memberRecruit(data);
			setMemberStyle();
			$('#typeSwitch').prop('checked', true);
		}
		
		
	})
	
	$("#search").val(search);
	$("#keyword").val(keyword);
    
}

const positionMap = {
	    vocal: '보컬',
	    piano: '피아노',
	    guitar: '기타',
	    bass: '베이스',
	    drum: '드럼',
	    midi: '작곡·미디',
	    lyrics: '작사',
	    chorus: '코러스',
	    brass: '관악기',
	    string: '현악기'
	};

function companyRecruit(data){
	console.log(data);
	let $template = $("#boardTemplate");
	let $boardList = $("#boardList");
	let $jobHeader = $("#jobListHeader");
	
	$jobHeader.empty();
	$jobHeader.nextAll().remove();
	
	$jobHeader.append(
		    '<div class="pd-2rem company-list-row text-alignC items-center">' +
		        '<span></span>' +
		        '<span>공고제목/기업명</span>' +
		        '<span>근무지</span>' +
		        '<span>포지션</span>' +
		        '<span>급여</span>' +
		        '<span>등록일</span>' +
		    '</div>'
	);

	$("#boardDiv").addClass("company-list-row");
	
	data.jobList.forEach(board => {
		let $clone = $template.clone().removeAttr("id").show();
		
		$clone.find(".boardArea").text(board.gu + "\u00A0\u00A0\u00A0" + board.dong);
		$clone.find(".boardPosition").text(positionMap[board.position]);
		$clone.find(".boardTitle").text(board.job_title);
		$clone.find(".boardHits").text(board.job_hits);
		$clone.find(".boardReplyCnt").text(board.job_reply_cnt);
		$clone.find(".boardLink").attr("data-location", "/job/board/" + board.job_no);
		
		if(board.pay_category == 0) $clone.find(".boardPay").text("시급 " + board.pay + "원");
		else if(board.pay_category == 1) $clone.find(".boardPay").text("월급 " + board.pay + "원");
		
		let date = formatRelativeTime(board.job_date);
		$clone.find(".boardDate").text(date);
		
		
		$boardList.append($clone);
		
		
	});
	
	loadPagination(data.pageMaker);
}

function memberRecruit(data){
	let $template = $("#boardTemplate");
	let $boardList = $("#boardList");
	let $jobHeader = $("#jobListHeader");
	
	$jobHeader.empty();
	$jobHeader.nextAll().remove();
	
	$jobHeader.append(
		    '<div class="pd-2rem member-list-row text-alignC items-center">' +
		        '<span></span>' +
		        '<span>제목</span>' +
		        '<span>연습/활동 장소</span>' +
		        '<span>포지션</span>' +
		        '<span>등록일</span>' +
		    '</div>'
	);

	$("#boardDiv").addClass("member-list-row");

	data.jobList.forEach(board => {
		let $clone = $template.clone().removeAttr("id").show();
		
		$clone.find(".boardPay").remove();  
		$clone.find(".boardArea").text(board.gu + "\u00A0\u00A0\u00A0" + board.dong);
		$clone.find(".boardPosition").text(positionMap[board.position]);
		$clone.find(".boardTitle").text(board.job_title);
		$clone.find(".boardHits").text(board.job_hits);
		$clone.find(".boardReplyCnt").text(board.job_reply_cnt);
		$clone.find(".boardLink").attr("data-location", "/job/board/" + board.job_no);
		
		let date = formatRelativeTime(board.job_date);
		$clone.find(".boardDate").text(date);
		
		
		$boardList.append($clone);
		
		
	});
	
	loadPagination(data.pageMaker);
}



function updateUrl(newParams){
	let currentParams = new URLSearchParams(window.location.search);

	if (newParams.hasOwnProperty("search") || newParams.hasOwnProperty("keyword") || newParams.hasOwnProperty("job_category")) {
        newParams.pageNum = 1; 
    }
	
	[...currentParams.keys()].forEach(key => {
        if (key.startsWith('positions')) {
            currentParams.delete(key);
        }
    });

	// 변경된 값 업데이트
    for (const key in newParams) {
        if (newParams[key] === undefined || newParams[key] === null || newParams[key] === '') {
            currentParams.delete(key); 
        } else if (key === "positions") {
            // 포지션 각각 추가
            newParams[key].split(',').forEach(pos => {
                currentParams.append("positions", pos);
            });
        } else {
            currentParams.set(key, newParams[key]);
        }
    }
    
    
    let newUrl = "/job/boards?" + currentParams.toString();
    console.log(newUrl);
    
    location.href = newUrl;
}


function getSelectedPositions() {
    const positions = [];
    $(".position-checkbox:checked").each(function() {
        positions.push($(this).val());
    });
    return positions.join(",");  // 쉼표로 구분해서 문자열로 반환
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
	
    const postDate = new Date(dateString.replaceAll('/', '-'));  // '2025/03/01' -> '2025-03-01'
    const now = new Date();

    const diffMs = now - postDate;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffDay >= 7) {
        // 7일 넘으면 원래 날짜 표시
        return dateString;
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

//선택한 포지션 업데이트 함수
function updateSelectedPosition() {
    const selectedPosition = [];

    $(".position-grid input[type='checkbox']:checked").each(function() {
    	selectedPosition.push($(this).parent().text().trim());
    });

    displaySelectedPosition(selectedPosition);
}

// 선택된 지역 업데이트 함수
function updateSelectedArea(city, gu, dong) {
    selectedCity = city;
    selectedGu = gu !== 'all' ? gu : '';
    selectedDong = dong !== 'all' ? dong : '';

    $("#city").val(city);
    $("#gu").val(selectedGu);
    $("#dong").val(selectedDong);

    let selectedAreaText = city;
    if (gu) selectedAreaText += ' > ' + (gu === 'all' ? '전체' : gu);
    if (dong) selectedAreaText += ' > ' + (dong === 'all' ? '전체' : dong);

    // 선택 지역 표시 업데이트
    $("#selectedArea").html(selectedAreaText);

    // x 버튼 추가 (기존 있으면 삭제 후 새로 추가)
    $(".area-remove-btn").remove();
    const removeBtn = $("<button>")
        .addClass("area-remove-btn")
        .text("x")
        .on("click", function() {
            removeArea();  // 👈 이거 실행
        });

    $("#selectedAreaWrapper").append(removeBtn);
}

// 선택한 포지션 표시 함수
function displaySelectedPosition(Position) {
    const $selectedPosition = $("#selectedPosition");
    $selectedPosition.empty();  // 기존 내용 비우기

    if (Position.length === 0) {
        $selectedPosition.hide();  // 선택된 게 없으면 숨김
        return;
    }

    $selectedPosition.show();  // 선택된 게 있으면 표시

    Position.forEach(pos => {
        const span = $("<span>").addClass("selected-position").text(pos);
        const removeBtn = $("<button>").addClass("remove-btn").text("x").on("click", function() {
            removePosition(pos);
        });

        span.append(removeBtn);
        $selectedPosition.append(span);
    });
}

// 포지션 제거 함수 (x버튼 클릭 시)
function removePosition(position) {
    $(".position-grid input[type='checkbox']").each(function() {
        if ($(this).parent().text().trim() === position) {
            $(this).prop("checked", false);  // 체크 해제
        }
    });

    updateSelectedPosition();  // 다시 리스트 업데이트
}

function setMemberStyle() {
    $(".content").addClass("member-mode").removeClass("company-mode");
}
function setCompanyStyle() {
    $(".content").addClass("company-mode").removeClass("member-mode");
}

</script>
</head>
<body class="wrap" data-recruit-type="0">
	<div class="content company-mode" >
		<div class="rem-20 my-top-15 my-bottom-15">
			<div class="title job-title">
				<p class="text-center font-color-blue">Jobs</p>
			</div>
		
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
					
					<i id="searchBtn" class="glass_icon fa-solid fa-magnifying-glass""></i>
				</div>
				
			</div>
			<div class="selected-wrapper">
			    <div id="selectedAreaWrapper" class="selected-location inline">
			        <span id="selectedArea"></span>
			    </div>
			    <div id="selectedPosition" class="selected-position inline">
			    </div>
			</div>
			<div>
				<div class="filterBtnContainer flex">
					<button id="positionBtn" class="filter-btn">
			        	포지션
			        	<span class="icon"><i class="fa-solid fa-caret-down"></i></span>
				    </button>
				    <button id="areaBtn" class="filter-btn">
				        지역
				        <span class="icon"><i class="fa-solid fa-caret-down"></i></span>
				    </button>
				</div>
				<div>
					<div id="positionContainer" class="setting-filter" style="display: none;">
					    <div class="position-grid">
					        <label><input type="checkbox" class="position-checkbox" value="vocal"> 보컬</label>
					        <label><input type="checkbox" class="position-checkbox" value="piano"> 피아노</label>
					        <label><input type="checkbox" class="position-checkbox" value="guitar"> 기타</label>
					        <label><input type="checkbox" class="position-checkbox" value="bass"> 베이스</label>
					        <label><input type="checkbox" class="position-checkbox" value="drum"> 드럼</label>
					        <label><input type="checkbox" class="position-checkbox" value="midi"> 작곡·미디</label>
					        <label><input type="checkbox" class="position-checkbox" value="lyrics"> 작사</label>
					        <label><input type="checkbox" class="position-checkbox" value="chorus"> 코러스</label>
					        <label><input type="checkbox" class="position-checkbox" value="brass"> 관악기</label>
					        <label><input type="checkbox" class="position-checkbox" value="string"> 현악기</label>
					    </div>
					</div>
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
				<div class="switch-container">
				    <span class="switch-label">기업 구인🏢</span>
				    <label class="switch">
				        <input type="checkbox" id="typeSwitch">
				        <span class="slider"></span>
				    </label>
				    <span class="switch-label">멤버 모집🏕️</span>
				</div>
				<div class="write_btn write_btn_border job_write_border flex items-center border-radius-7px">
					<button type="button" id="jobWriteBtn" class="write_btn_font border-none bColor_fff ">작성하기</button>
				</div>
			</div>
			<!-- <h2>pay를 숫자말고 String으로 변경 해서(db도) 협의 후 결정도 포함해야 할 듯</h2> -->
			<div>
				<ul style="display:none">
					<li id="boardTemplate" class="border-bottom">
						<div id="boardDiv" class="pd-2rem job-list-row text-alignC items-center">
							<span class="favorite">
								<i class="fa-solid fa-star" style="color: #FFD43B; cursor: pointer;"></i> 
								<i class="fa-regular fa-star" style="color: #FFD43B;"></i>
							</span>
							<span class="font-weight-bold font-size-5 boardTitle boardLink"  style="cursor: pointer;"></span>
					        
							<span class="boardArea"></span>
							<span class="boardPosition"></span>
					        <span class="boardPay"></span>
					        <span class="boardDate"></span>
						</div>
					</li>
				</ul>
				<ul id="boardList">
					<li id="jobListHeader" class="border-top border-bottom">
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