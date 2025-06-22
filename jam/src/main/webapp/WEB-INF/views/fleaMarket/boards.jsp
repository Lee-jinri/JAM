<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>

<script src="/resources/include/dist/js/userToggle.js"></script>
<script src="/resources/include/dist/js/favorite.js"></script>	
<style>


.title-container {
	flex-grow: 1;
    min-width: 250px;
    max-width: 680px;
    margin-left: 7rem;
}

.userName-div{
	max-width: 150px;
    flex: 1;
    justify-content: center;
}

.date-container {
    display: flex;
    flex-direction: column;
    align-items: flex-end;  /* 오른쪽 정렬 */
    flex-shrink: 0; /* 자동 크기 축소 방지 */
    min-width: 120px; /* 너무 넓지 않게 조절 */
    max-width: 170px; /* 필요 이상으로 커지지 않게 제한 */
}

.user_toggle {
    left: -6rem;
    top: 3rem;
}
</style>
<script type="text/javascript">
$(function(){
	getBoards().then(() => {
		toggleUserMenu(); 
    })
    .catch(error => {
        console.error('Error while executing fleaMarket boards:', error);
    });
	
	$(document).on("click", ".boardLink", function (e) {
	    e.preventDefault();
	    var location = $(this).attr("data-location");
	    if (location) {
	    	window.location.href = location;
	    }
	});

	
	$("#searchBtn").click(function(){
		let search = $("#search").val();
		let keyword = $("#keyword").val();
		
		if(search == "all") keyword = "";
		else{
			if(keyword.replace(/\s/g, "") == ""){
				alert("검색어를 입력하세요.");
				$("#keyword").focus();
				return;
			} 
		}
		location.href = '/fleaMarket/boards?search='+search+'&keyword='+keyword+'&pageNum='+'1';
	})
		
	$("#fleaWriteBtn").click(function(){
		fetch("/api/member/auth/check").then((res) => {
			if (res.status === 401) {
				if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
					location.href = "/member/login";
				} else {
					location.href = "/fleaMarket/boards";
				}
			} else {
				location.href = "/fleaMarket/board/write";
			}
		})
	})	
})

function getBoards(){
	return new Promise((resolve, reject) => {
		let params = new URLSearchParams(window.location.search);
	    let pageNum = params.get("pageNum") || "1";
	    let search = params.get("search") || "all";
	    let keyword = params.get("keyword") || "";
	    
	    fetch('/api/fleaMarket/boards?pageNum='+pageNum+'&search='+search+'&keyword='+keyword)
		.then(response=>{
			if(response.ok) return response.json();
		}).then(data=>{
			renderList(data);
			resolve();
		})
		.catch(error => {
            console.error('Error:', error);
            reject(error);
        });
	})
}

function renderList(data){
	let $template = $("#boardTemplate");
    let $boardList = $("#boardList");
    
    $boardList.empty(); 

    console.log(data);
    data.fleaMarketList.forEach(board => {
        let $clone = $template.clone().removeAttr("id").show();
        $clone.find(".userName").text(board.user_name);
        $clone.find(".userName").attr("data-userId", board.user_id);
        $clone.find(".boardDate").text(board.flea_date);
        $clone.find(".boardTitle").text(board.flea_title);
        $clone.find(".boardHits").text("👀" +board.flea_hits);
        $clone.find(".boardReplyCnt").text(board.flea_reply_cnt);
        $clone.find(".boardLink").attr("data-location", "/fleaMarket/board/" + board.flea_no);

        let $favoriteSpan = $clone.find(".favoriteSpan");
        $favoriteSpan.attr("data-board-no", board.flea_no);
		$favoriteSpan.attr("data-board-type", "fleaMarket");
		
		let $icon = $favoriteSpan.find("i"); 
		board.favorite ? $icon.addClass("fa-solid")
					   : $icon.addClass("fa-regular");
		
        $boardList.append($clone);
    });
    
    loadPagination(data.pageMaker);
    
}


function loadPagination(pageMaker) {
    const $pagination = $("#pagination");
    $pagination.empty(); // 기존 페이지 버튼 초기화

    // 페이지 번호 버튼
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
        let params = new URLSearchParams(window.location.search);
	    let search = params.get("search") || "all";
	    let keyword = params.get("keyword") || "";
	    
	    let url = "/fleaMarket/boards?pageNum="+pageNum+"&search="+search+"&keyword="+keyword;
        
		window.location.href = url;
    });
}
</script>
</head>
<body class="wrap">
	<div class="content fleaMarket">
		<div class="my-top-15 my-bottom-15">
			<div class="title flea-title ">
				<p class="text-center font-color-blue">중고 악기</p>
			</div>
		
			<div class="search-div flex justify-center items-center border border-radius-43px">
				<div class="search-bar-wrapper item-center flex justify-space-around">
					
					<% String searchParam = request.getParameter("search");
					    if (searchParam == null || searchParam.isEmpty()) {
					        searchParam = "all";
					    }%> 
					   
					<select id="search" name="search" class="search border-none">
						<option value="all" ${searchParam == 'all' ? 'selected' : ''}>전체</option>
					    <option value="flea_title" ${param.search == 'flea_title' ? 'selected' : ''}>제목</option>
					    <option value="flea_content" ${param.search == 'flea_content' ? 'selected' : ''}>내용</option>
					    <option value="user_name" ${param.search == 'user_name' ? 'selected' : ''}>작성자</option>
					</select>
					
					<input type="text" name="keyword" id="keyword" class=" rem-2 search search-input"
					value="${not empty param.keyword ? param.keyword : ''}" />
					
					<i id="searchBtn" class="glass_icon fa-solid fa-magnifying-glass"></i>
				</div>
			</div>
			
			<div class="justify-end py-4 flex">
				<div class="write_btn write_btn_border write_border flex items-center border-radius-7px">
					<button type="button" id="fleaWriteBtn" class="write_btn_font border-none bColor_fff ">작성하기</button>
				</div>
			</div>
			
			<div>
				<ul id="boardList">
			        <li id="boardTemplate" class="border-bottom">
			            <div class="pd-2rem flex items-center" >
			                
			                <div class="flex items-center justify-center ml-2 mr-2" style="width: 3rem;">
			                    <span class="favoriteSpan">
			                        <i class="favorite fa-star" style="color: #FFD43B; cursor: pointer;"></i>
			                    </span>
			                </div>
			
			                <div class="title-container boardLink flex-1 flex items-center cursor-pointer">
			                    <div>
			                        <span class="font-weight-bold font-size-5 boardTitle"></span>
			                    </div>
			                </div>
							<div class="userName-div my-bottom-2 flex">
			                    <span class="userName"></span>
			                    <div class="userNameToggle"></div>
			                </div>
			                
			                <div class="date-container flex-1 text-right">
			                    <div class="my-bottom-2">
			                        <span class="boardDate"></span>
			                    </div>
			                    <div class="flex items-center justify-end my-top-2">
			                        <span class="ml-05 boardHits"></span>
			                        <span class="ml-05"><i class="fa-regular fa-comment-dots"></i></span>
			                        <span class="ml-05 boardReplyCnt"></span>
			                    </div>
			                    
			                </div>
			
			            </div>
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