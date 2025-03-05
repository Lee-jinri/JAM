<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 합주실/연습실</title>
<script src="/resources/include/dist/js/userToggle.js"></script>
<script src="/resources/include/dist/js/location.js"></script>
	
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
		fetch('/api/member/checkAuthentication')
		.then(response =>{
			if(!response.ok){
				alert('시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
				locaion.attr('/roomRental/boards');
			}
			return response.json();
		})
		.then((data) => {
			if(data.authenticated) $(location).attr('href', '/roomRental/board/write');
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
	
	fetch(url)
	.then(response=>{
		return response.json();
	}).then(data=>{
		let $template = $("#boardTemplate");
		let $boardList = $("#boardList");
	
		if ($template) {
			$template.innerHTML = ""; 
	    }
	
		data.roomList.forEach(board => {
			
			let $clone = $template.clone().removeAttr("id").show();
			
			$clone.find(".boardArea").text(board.city + "\u00A0\u00A0\u00A0" + board.gu + "\u00A0\u00A0\u00A0" + board.dong);
			
			$clone.find(".userName").text(board.user_name);
			$clone.find(".boardDate").text(board.roomRental_date);
			$clone.find(".boardTitle").text(board.roomRental_title);
			$clone.find(".boardHits").text(board.roomRental_hits);
			$clone.find(".boardReplyCnt").text(board.roomRental_reply_cnt);
			$clone.find(".boardLink").attr("data-location", "/roomRental/board/" + board.roomRental_no);
			
			let duration = board.roomRental_duration === 0 ? "시" : board.roomRental_duration === 1 ? "일" : "월"; // 단위 설정
			let formattedPrice = board.roomRental_price.toLocaleString();
			
			$clone.find(".boardPrice").text(formattedPrice + "/" + duration);
			
			let $roomStatus = $clone.find(".boardRoomStatus");
			
			
			if(board.roomRental_status == 0){
				console.log(board.roomRental_status);
				$roomStatus.text("거래중");
				$roomStatus.css({"color": "#04B431"});
				$roomStatus.addClass("border-g");
			}else if(board.roomRental_status == 1){
				console.log(board.roomRental_status);
				$roomStatus.text("거래 완료");
				$roomStatus.css({"color": "#A4A4A4"});
				$roomStatus.addClass("border");
			}
			
			$boardList.append($clone);
			
			
		});
		$template.empty();
		loadPagination(data.pageMaker);
	})
	
	$("#search").val(search);
	$("#keyword").val(keyword);
	
}
		

function updateUrl(newParams){
	let currentParams = new URLSearchParams(window.location.search);

	if (newParams.hasOwnProperty("search") || newParams.hasOwnProperty("keyword")) {
        newParams.pageNum = 1; 
    }
	
    // 변경된 값 업데이트
    for (const key in newParams) {
        if (newParams[key] === undefined || newParams[key] === null || newParams[key] === '') {
            currentParams.delete(key); 
        } else {
            currentParams.set(key, newParams[key]); 
        }
    }

    let newUrl = "/roomRental/boards?" + currentParams.toString();
    location.href = newUrl;

}



function loadPagination(pageMaker) {
    const $pagination = $("#pagination");
    $pagination.empty(); // 기존 페이지 버튼 초기화

    // 이전 버튼
    if (pageMaker.prev) {
        $pagination.append(
            '<li class="paginate_button previous">' +
                '<a href="#" data-page="' + (pageMaker.startPage - 1) + '">Previous</a>' +
            '</li>'
        );
    }

    // 페이지 번호 버튼
    for (let num = pageMaker.startPage; num <= pageMaker.endPage; num++) {
        $pagination.append(
            '<li class="paginate_button ' + (pageMaker.cvo.pageNum === num ? 'active' : '') + '">' +
                '<a href="#" data-page="' + num + '" class="font-weight-bold">' + num + '</a>' +
            '</li>'
        );
    }

    // 다음 버튼
    if (pageMaker.next) {
        $pagination.append(
            '<li class="paginate_button next">' +
                '<a href="#" data-page="' + (pageMaker.endPage + 1) + '">Next</a>' +
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
</script>
</head>
<body class="wrap">
	<div class="rem-20 my-top-15 my-bottom-15">
		<div class="title">
			<p class="text-center font-color-blue">합주실 / 연습실 </p>
		</div>
		<div class="content">
			<div class="justify-between flex py-4">
				<div class="write_btn write_btn_border flex items-center border-radius-7px">
					<img class="icon" src="/resources/include/images/write.svg">
					<button type="button" id="roomWriteBtn" class="write_btn_font border-none bColor_fff">작성하기</button>
				</div>
			</div>
			
			<div class="border-top border-bottom py-2rem flex justify-center">
				
				<div class="items-center">
					<form id="searchForm">
						<!-- 페이징 처리를 위한 파라미터 -->
						<input type="hidden" id="pageNum"  name="pageNum" value="${pageMaker.cvo.pageNum }">
						<input type="hidden" name="amount" value="${pageMaker.cvo.amount }">
						<input type="hidden" name="cd" id="cd" value="${not empty param.cd ? param.cd : ''}">
						
						<div class="py-4">
							<div class="flex justify-space-around">
								<div>
									<div class="block mr-1">
										<select name="city" id="city" class="w-10rem">
											<option value="" class="city">시·도 전체</option>
											<option value="서울" data-index="11" class="city">서울</option>
											<option value="부산" data-index="21" class="city">부산</option>
											<option value="대구" data-index="22" class="city">대구</option>
											<option value="인천" data-index="23" class="city">인천</option>
											<option value="광주" data-index="24" class="city">광주</option>
											<option value="대전" data-index="25" class="city">대전</option>
											<option value="울산" data-index="26" class="city">울산</option>
											<option value="세종" data-index="29" class="city">세종</option>
											<option value="경기" data-index="31" class="city">경기</option>
											<option value="강원" data-index="32" class="city">강원</option>
											<option value="충북" data-index="33" class="city">충북</option>
											<option value="충남" data-index="34" class="city">충남</option>
											<option value="전북" data-index="35" class="city">전북</option>
											<option value="전남" data-index="36" class="city">전남</option>
											<option value="경북" data-index="37" class="city">경북</option>
											<option value="경남" data-index="38" class="city">경남</option>
											<option value="제주" data-index="39" class="city">제주</option>
										</select>
									</div>
								</div> 
									
								<div>
									<div id="guDiv" class="mr-1">
										<select name="gu" id="gu" class="w-10rem">
											<option value="">시·구·군 전체</option>
										</select>
									</div>
								</div>
									
								<div>
									<div id="dongDiv" class="">
										<select name="dong" id="dong" class="w-10rem">
											<option value="">동·읍·면 전체</option>
										</select>
									</div>
								</div>
							</div>
						</div>
						
						
						<div class="items-center flex">
							<select id="search" name="search" class="search">
								<option value="all">전체</option>
								<option value="room_title">제목</option>
								<option value="room_content">내용</option>
								<option value="user_name">작성자</option>
							</select>
							<input type="text" name="keyword" id="keyword" placeholder="합주실/연습실 내에서 검색" class="border border-radius-43px rem-2 search"/>
							<img class="icon" id="searchBtn" style="cursor:pointer; width:3rem;" src="/resources/include/images/search.svg">
						</div>
					</form>
				</div>
			</div>	
			
			
			<div>
				<ul id="boardList">
					<li class="border-bottom">
						<div class="pd-2rem room-list-row text-alignC items-center">
							<span></span>
							<span class="">지역</span>
							<span class="">제목</span>
							<span class="">가격</span>
							<span class="">작성자</span>
							<span class="">등록일</span>
						</div>
					</li>
					<li id="boardTemplate" class="border-bottom">
						<div class="pd-2rem boardLink " style="cursor: pointer;">
							<div class="my-bottom-4 room-list-row text-alignC items-center">
								<span class="boardRoomStatus"></span>
								<span class="boardArea"></span>	
								<span class="font-weight-bold font-size-5 boardTitle"></span>
								<span class="boardPrice"></span>
								
								<span class="padding-right-1 userName"></span>
								<div class="userNameToggle absolute" style="z-index: 5;"></div>		
								<span class="boardDate"></span>
								
								<div>
									<img class="icon" id="" style="width:2.5rem;" src="/resources/include/images/hits.svg">
									<span class="font-size-4 ml-05 boardHit"></span>
								</div>
								<div>
									<img class="icon ml-2" id="" style="width:2.5rem;" src="/resources/include/images/reply.svg">
									<span class="font-size-4 ml-05 boardReplyCnt"></span>
								</div>
							</div>
							
							<div class="flex float-right items-center width-13rem justify-center my-top-4">
								
							</div>
						</div>
					</li>
				</ul>
			
			</div>
			
			<div>
				<!-- 페이징 -->
				<div class="text-center">
					<ul class="pagination pagination_border">
						<c:if test="${pageMaker.prev}">
							<li class="paginate_button previous">
								<a href="${pageMaker.startPage -1}">Previous</a>
							</li>
						</c:if>
						
						<c:forEach var="num" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
							<li class="paginate_button"  >
								<a id="${pageMaker.cvo.pageNum == num ? 'btnColor':''}" class="font-weight-bold" href="${num}">${num}</a>
							</li>
						</c:forEach>
						
						<c:if test="${pageMaker.next}">
							<li class="paginate_button next">
								<a href="${pageMaker.endPage +1 }">Next</a>
							</li>
						</c:if>
					</ul>
				</div>
			</div>
		</div>
	</div>
</body>
