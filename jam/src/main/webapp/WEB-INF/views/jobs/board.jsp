<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Jobs</title>
<script src="/resources/include/dist/js/userToggle.js"></script>
<script src="/resources/include/dist/js/area.js"></script>
<script src="/resources/include/dist/js/favorite.js"></script>
<style>
.filterDiv{
	margin: 5rem 0 3rem;
}
.selected-wrapper {
	max-width: 600px;
	width: 100%;
	margin: 10px auto;
	text-align: left;
	margin-bottom: 8px;
	min-height: 23px;
	border: 1px solid #d2d2d2;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    flex-direction: column;
}

.selected-location, .selected-position {
	font-weight: bold;
	font-size: 14px;
	color: #333;
	display: flex;
	flex-direction: row;
	gap: 10px;
	margin-bottom: 8px;
	width: fit-content;
}
.selected-position{
	flex-wrap: wrap;
}
.selected-position-span, .selected-area-span{
    height: 2rem;
    margin-top: 8px;
    padding: 9px 12px;
    border-color: #e8e8e8;
    background-color: #f8f8f8;
    border: 1px solid #e8e8e8;
    align-items: center;
    display: inline-flex;
    border-radius: 4px;
}
.selected-position-span{
	justify-content: center;
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
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: #ddd;
	transition: 0.4s;
	border-radius: 34px;
}

.slider:before {
	position: absolute;
	content: "";
	height: 20px;
	width: 20px;
	left: 3px;
	bottom: 3px;
	background-color: white;
	transition: 0.4s;
	border-radius: 50%;
}

/* 기본 색상 (unchecked 상태일 때) */
.slider {
	background-color: #003366;
}

input:checked+.slider {
	background-color: #ffdd77;
}

input:checked+.slider:before {
	transform: translateX(24px);
}

.filterBtnContainer {
	gap: 10px; 
	align-items: center; 
	max-width: 600px;
	margin: 10px auto;
}

.setting-filter {
	display: none;
	overflow: hidden;
	border: 1px solid #ddd;
	border-radius: 6px;
	background-color: #fff;
	max-width: 800px;
	margin: 0 auto;
	box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
	padding: 10px;
}

.setting-base-row {
	display: flex;
	justify-content: space-between;
	gap: 8px;
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
	flex: 1; 
	border: 1px solid #e5e8ec;
	border-radius: 4px;
	background-color: #f9f9f9;
	overflow: hidden;
}

.setting-base__col--list {
	max-height: 200px; 
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
	gap: 10px; 
}
.icon {
    width: fit-content;
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
	font-weight: 500;
	transition: background-color 0.3s;
	justify-content: center;
	width: 90px;
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
	max-width: 600px;
	margin-top: 8px;
}

.position-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); 
	gap: 8px; 
}

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

.position-grid label:hover {
	background-color: #f0f0f0;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.position-grid input[type="checkbox"] {
	width: 16px;
	height: 16px;
	accent-color: #007bff;
}

.remove-btn, .area-remove-btn {
	background: none;
	border: none;
	color: #ff6b6b;
	font-weight: bold;
	font-size: 14px;
	cursor: pointer;
	padding-right: 0;
}

.boardDiv{
	height: 110px;
}
.job-board-header span{
	font-weight: bold;
}
.title{
	font-weight: normal;
}
.payDiv{
	display: flex;
	justify-content: flex-end;
	gap: 6px;
}
.pay_category {
background-color: #fff;
    padding: 2px 8px;
    border-radius: 28px;
    font-size: .75rem;
    line-height: 1rem;
    align-items: center;
    display: inline-flex;
    font-weight: 400;
}  
.daily {
	border: 1px solid #ff501b;
    border-color: #ff501b;
    color: #ff501b;
}  
.weekly {
	border: 1px solid #00b0a6;
    border-color: #00b0a6;
    color: #00b0a6;
}  
.monthly {
	border: 1px solid #00a1ef;
    border-color: #00a1ef;
    color: #00a1ef;
}
#pagination{
	padding-top: 40px;
}
.pagination > li > .selected_btn{
	color: #0F2D4A;
    border-color: #0F2D4A;
    font-weight: 700;
    background-color: #fff;
    border: none;
    font-size: 1rem;
    font-weight: normal;
}
.pagination > li > .default_btn{
	border: none;
	font-size: 1rem;
	font-weight: normal;
}
.created_at{
	color: #00a1ef;
}
.searchDiv{
	display: flex;
	justify-content: end;
}
.searchBtn{
    border: 1px solid #e8e8e8;
    padding: 7px 10px 8px;
    border-radius: 6px;
    width: 95.5px;
    text-align: center;
    height: 36px;
}

/* ====== 1024px 이하 ====== */
@media (max-width: 1024px) {
	.position-grid { grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); }
	.setting-base__col--list { max-height: 180px; }
	.container{ margin: 4rem 3rem; }
}

/* ====== 768px 이하: 모바일 (카드형)====== */
@media (max-width: 768px) {
	.container{
		margin: 4rem 2rem;
	}
	
	 #jobListHeader { display: none !important; }
	
	 .boardDiv {
		display: grid !important;
		grid-template-columns: 0.2fr 1fr 0.5fr;
		grid-template-areas:
			"favorite title date"
			"favorite area area"
			"favorite position pay";
		gap: 6px 10px;
		align-items: center;
		text-align: left;
		padding: 12px 12px !important;
		height: auto !important;
	 }
	
	.boardDiv .title       { grid-area: title; font-size: 0.95rem; font-weight: 600; }
	.boardDiv .created_at  { grid-area: date; justify-self: end; color: #6b7280; font-size: 0.8rem; }
	.boardDiv .area        { grid-area: area; font-size: 0.9rem; color: #374151; }
	.boardDiv .position    { grid-area: position; font-size: 0.9rem; }
	.boardDiv .payDiv      { grid-area: pay; justify-self: end; display: inline-flex; gap: 6px; align-items:center;}
	.boardDiv .favoriteSpan { grid-area: favorite; }
	
	.pay_category {
		font-size: 0.7rem; padding: 2px 8px; 
	}
	.searchBtn { 
		width: auto; padding: 7px 12px 8px; 
	}
	
	.setting-base-row {
	  flex-direction: column;
	}
	.setting-base__col--title {
	  border-right: 0;
	  border-bottom: 1px solid #e5e8ec;
	  text-align: left;
	  padding-left: 10px;
	}
	
	.position-grid { grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); }
	.position-grid label { padding: 8px 10px; }
	
	.selected-position { 
		gap: 8px; flex-wrap: wrap; 
	}
	.selected-position-span, .selected-area-span {
	  margin-top: 6px;
	  padding: 6px 10px;
	  height: auto;
	}

}

/* ====== 480px 이하 ====== */
@media (max-width: 480px) {
	.filterDiv{
		margin: 0;
	}
	.container{
		margin: 4rem 1rem;
	}
	.filter-btn {         
	 	padding: 3px 3px;
	    font-size: 8px;
	    width: 47px;
	}
	.filter-btn .icon {
	    width: min-content;
	    font-size: 8px;
	}
	.filter-btn i {
	    font-size: 8px;
	}
	.searchBtn {
	    width: auto;
	    padding: 1px 4px;
	    font-size: 8px;
	    height: auto;
	    align-items: center;
	}
	span {
		font-size: 10px;
	}
	.switch {
	    width: 30px;
	    height: 17px;
	}
	.slider:before {
	    height: 12px;
	    width: 12px;
	}
	input:checked+.slider:before {
	    transform: translateX(12px);
	}
	.selected-wrapper { padding: 12px; }
	.position-grid { grid-template-columns: repeat(auto-fill, minmax(90px, 1fr)); }
	.boardDiv .title { font-size: 12px; }
	.boardDiv .area, .boardDiv .position { font-size: 10px; }
	.boardDiv .created_at { font-size: 10px; }
	.payDiv{ align-items: center; }
	.pay_category{
		font-size: 8px;
	    padding: 2px 4px;
	    line-height: normal;
	    align-items: center;
	}
}
</style>
<script type="text/javascript">
let memberListCache = null;
let companyListCache = null;

let boardState = {
		  pageNum: 1,
		  search: "all",
		  keyword: "",
		  category: "0", 
		  city: "",
		  gu: "",
		  dong: "",
		  positions: []
		};


$(function(){
	getBoard();
	
	$(document).on("click", ".postLink", function (e) {
		e.preventDefault();
		var location = $(this).attr("data-location");
		if (location) {
			window.location.href = location;
		}
	});
	
	$('#typeSwitch').on('change', function() {
		removeArea();
		removeAllPosition();
		
		boardState = {
				  pageNum: 1,
				  search: "all",
				  keyword: "",
				  city: "",
				  gu: "",
				  dong: "",
				  positions: []
				};
		
	    let category = $(this).is(':checked') ? "1" : "0";
	    
	    boardState.category = category;
	    getBoard();
	});


	$("#search-btn").click(function(){
	    boardState.category = $("#typeSwitch").is(":checked") ? "1" : "0";
	    boardState.positions= getSelectedPositions(); 

	    boardState.pageNum = 1;

		boardState.city = $("#city").val();
		boardState.gu = $("#gu").val();
		boardState.dong = $("#dong").val();
		
	    getBoard();
	});
	
	$("#jobWriteBtn").click(function(){
	 	if(window.MY_ID == null) {
	 		if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
				location.href = "/member/login";
			} else {
				location.href = "/jobs/board";
			}
	 		return;
	 	}
	 	
		location.href = "/jobs/board/write";
	})
		
	/*
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
		
		let category;
		if ($("#typeSwitch").is(":checked")) category = "1"; else category = "0";
		
		let city = $("#city").val();
		let gu = $("#gu").val();
		let dong = $("#dong").val();
		
		let data = {
				search : search,
				keyword: keyword,
				category: category ,
				city: city,
				gu: gu,
				dong: dong,
				positions: getSelectedPositions()
		}
		updateUrl(data);
	})*/
	
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

function getBoard() {

	const params = new URLSearchParams(window.location.search);
	const keywordParam = params.get("keyword");
	
	if (keywordParam) {
	    boardState.keyword = keywordParam;
	} else {
	    boardState.keyword = '';
	}

	let queryString = new URLSearchParams(boardState).toString();
	let url = "/api/jobs/board?" + queryString;

	console.log(url);
	fetch(url)
		.then(res => res.json())
		.then(data => {
			if (boardState.category === "0") {
				companyRecruit(data);
				setCompanyStyle();
			} else {
				memberRecruit(data);
				setMemberStyle();
			}
		loadPagination(data.pageMaker);
	});
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
	let $template = $("#boardTemplate");
	let $boardList = $("#boardList");
	let $jobHeader = $("#jobListHeader");
	
	$jobHeader.empty();
	$jobHeader.nextAll().remove();
	
	$jobHeader.append(
		    '<div class="job-board-header pd-2rem company-list-row text-alignC items-center">' +
		        '<span></span>' +
		        '<span>공고제목/기업명</span>' +
		        '<span>근무지</span>' +
		        '<span>포지션</span>' +
		        '<span>급여</span>' +
		        '<span>등록일</span>' +
		    '</div>'
	);
//여기용
	data.jobList.forEach(board => {
		let $clone = $template.clone().removeAttr("id").show();
		let $boardDiv = $clone.find(".boardDiv");
		$boardDiv.addClass("company-list-row");
		
		let areaText = board.city;
		if(board.gu != null) areaText += "\u00A0" + board.gu;
		if(board.dong != null) areaText += "\u00A0" + board.dong;
		
		$clone.find(".area").text(areaText);
		$clone.find(".position").text(positionMap[board.position]);
		$clone.find(".title").text(board.title);
		$clone.find(".postLink").attr("data-location", "/jobs/post/" + board.post_id);
		
		let $favoriteSpan = $clone.find(".favoriteSpan");
		$favoriteSpan.attr("data-board-no", board.post_id);
		$favoriteSpan.attr("data-board-type", "job");
		
		let $icon = $favoriteSpan.find("i"); 
		board.favorite ? $icon.addClass("fa-solid")
					   : $icon.addClass("fa-regular");

		const payTexts = ["건별 ", "주급 ", "월급 ", "협의 후 결정"];
		let payText = payTexts[board.pay_category] || "협의 후 결정";
		
		let $payDiv =$clone.find(".payDiv");
		$payDiv.find(".pay").text(board.pay_category < 3 ? board.pay + "원" : "협의 후 결정");
		
		let $payCategory = $payDiv.find(".pay_category");
		if(board.pay_category < 3){
			$payCategory.text(payText)
		} else{
			$payCategory.css("display","none");
			$payDiv.css("justifyContent","center");	
		}
		
		if(board.pay_category == 0) $payCategory.addClass("daily");
		else if(board.pay_category == 1) $payCategory.addClass("weekly");
		else if(board.pay_category == 2) $payCategory.addClass("monthly");
		
		let date = timeAgo(board.created_at);
		$clone.find(".created_at").text(date);
		
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
		    '<div class="job-board-header pd-2rem member-list-row text-alignC items-center">' +
		        '<span></span>' +
		        '<span>제목</span>' +
		        '<span>연습/활동 장소</span>' +
		        '<span>포지션</span>' +
		        '<span></span>' +
		        '<span>등록일</span>' +
		    '</div>'
	);

	$("#boardDiv").removeClass("company-list-row");
	
	$("#boardDiv").addClass("member-list-row");

	data.jobList.forEach(board => {
		let $clone = $template.clone().removeAttr("id").show();
		
		let $favoriteSpan = $clone.find(".favoriteSpan");
		$favoriteSpan.attr("data-board-no", board.post_id);
		$favoriteSpan.attr("data-board-type", "job");
		
		let $icon = $favoriteSpan.find("i"); 
		board.favorite ? $icon.addClass("fa-solid")
					   : $icon.addClass("fa-regular");
		
		$clone.find(".pay").remove();  
		
		let areaText = board.city;
		if(board.gu != null) areaText += "\u00A0" + board.gu;
		if(board.dong != null) areaText += "\u00A0" + board.dong;
		
		$clone.find(".area").text(areaText);
		$clone.find(".position").text(positionMap[board.position]);
		$clone.find(".title").text(board.title);
		$clone.find(".postLink").attr("data-location", "/jobs/post/" + board.post_id);
		
		let date = timeAgo(board.created_at);
		$clone.find(".created_at").text(date);
		
		$boardList.append($clone);
	});
	
	loadPagination(data.pageMaker);
}


function updateUrl(newParams){
	let currentParams = new URLSearchParams(window.location.search);

	if (newParams.hasOwnProperty("search") || newParams.hasOwnProperty("keyword") || newParams.hasOwnProperty("category")) {
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
    
    let newUrl = "/jobs/board?" + currentParams.toString();
    
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
    $pagination.empty(); 
    
    for (let num = pageMaker.startPage; num <= pageMaker.endPage; num++) {
        $pagination.append(
            '<li class="paginate_button">' +
                '<a href="#" data-page="' + num + '" class="font-weight-bold ' + (pageMaker.cvo.pageNum === num ? 'selected_btn' : 'default_btn') + '">' + num + '</a>' +
            '</li>'
        );
    }

    $("#pagination a").click(function (e) {
        e.preventDefault();
        let pageNum = $(this).data("page");
        
        boardState.pageNum = pageNum;
        getBoard();
    });
}

//선택한 포지션 업데이트 함수
function updateSelectedPosition() {
    const selectedPosition = [];

    $(".position-grid input[type='checkbox']:checked").each(function() {
    	selectedPosition.push($(this).parent().text().trim());
    });

    displaySelectedPosition(selectedPosition);
}

// 선택한 포지션 표시 함수
function displaySelectedPosition(Position) {
    const $selectedPosition = $("#selectedPosition");
    $selectedPosition.empty();  

    $selectedPosition.show();  

    Position.forEach(pos => {
        const span = $("<span>").addClass("selected-position-span").text(pos);
        
        const removeBtn = $("<button>")
        .addClass("remove-btn")
        .html('<i class="fa-solid fa-x"></i>')
        .on("click", function() {
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
            $(this).prop("checked", false); 
        }
    });

    updateSelectedPosition(); 
}

// 모든 포지션 선택 취소
function removeAllPosition(){
	$(".position-grid input[type='checkbox']").each(function() {
        $(this).prop("checked", false);  
    });

    updateSelectedPosition();
}

function setMemberStyle() {
    $(".content").addClass("member-mode").removeClass("company-mode");
}
function setCompanyStyle() {
    $(".content").addClass("company-mode").removeClass("member-mode");
}

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
	
	// 개월 계산 (30일 단위)
    let months = Math.floor(diff / 2592000); 
    if (months < 12) return months + '개월 전';
    
	let dateStr = past.toLocaleDateString();
	return dateStr.replace(/\.$/, '');
}

</script>
</head>

<body class="wrap" data-recruit-type="0">
	<div class="content company-mode container" >
		<div class="my-top-15 my-bottom-15">
			<input type="hidden" id="city">
			<input type="hidden" id="gu">
			<input type="hidden" id="dong">
			
			<div class="filterDiv">
				<div class="filterBtnContainer flex">
					<button id="areaBtn" class="filter-btn">
				        지역
				        <span class="icon"><i class="fa-solid fa-caret-down"></i></span>
				    </button>
					<button id="positionBtn" class="filter-btn">
			        	포지션
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
				<div class="selected-wrapper">
				    <div id="selectedAreaWrapper" class="selected-location inline">
				        <span id="selectedArea" class="selected-location-span"></span>
				    </div>
				    <div id="selectedPosition" class="selected-position inline">
				    </div>
				    <div class="searchDiv">
				    	<button id="search-btn" class="searchBtn">검색</button>
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
				<sec:authorize access="hasRole('ROLE_COMPANY')">
				    <div class="write_btn write_btn_border write_border flex items-center border-radius-7px">
						<button type="button" id="jobWriteBtn" class="write_btn_font border-none bColor_fff ">작성하기</button>
					</div>
				</sec:authorize>
			</div>
			
			<div>
				<ul style="display:none">
					<li id="boardTemplate" class="border-bottom">
						<div class="boardDiv pd-2rem job-list-row text-alignC items-center">
							<span class="favoriteSpan">
								<i class="favorite fa-star" style="color: #FFD43B; cursor: pointer;"></i>
							</span>
							<span class="font-size-5 title postLink"  style="cursor: pointer;"></span>
					        
							<span class="area"></span>
							<span class="position"></span>
							
					        <div class="payDiv">
					        	<span class="pay"></span>
					        	<span class="pay_category"></span>
					        </div>
					        
					        <span class="created_at"></span>
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