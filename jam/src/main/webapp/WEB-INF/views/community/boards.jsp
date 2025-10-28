<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>
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

.boardReplyCnt{
	font-size: 10px;
    color: #746eff;
}

.boardHits, .boardDate{
    font-size: 13px;
    color: #8b8b8b;
}
.boardLink:hover {
	background-color: #f5f5f5;	
	cursor: pointer;
}



</style>
<script type="text/javascript">
const boardState = {
		keyword: "",
		pageNum: 1
		
}
	$(function(){
		getBoard();
		
		$(document).on("click", ".boardLink", function (e) {
		    e.preventDefault();
		    var location = $(this).attr("data-location");
		    if (location) {
		        window.location.href = location;
		    }
		});

		$("#searchBtn").click(function(){
			let keyword = $("#keyword").val();
			
			if(keyword.replace(/\s/g, "") == ""){
				alert("검색어를 입력하세요.");
				$("#keyword").focus();
				return;
			} 
			boardState.keyword = keyword;
			boardState.pageNum = 1;
			
			getBoard();
		})
			
		$("#comWriteBtn").click(function () {
			if(window.MY_ID == null){
				if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
					location.href = "/member/login";
				} else {
					location.href = "/community/boards";
				}
			}else {
				location.href = "/community/board/write";
			}
		})
	})
	
	function getBoard(){
	    let pageNum = boardState.pageNum || "1";
	    let keyword = boardState.keyword || "";
	    
		let queryString = new URLSearchParams(boardState).toString();
		let url = "/api/community/boards?" + queryString;
		
	    fetch(url)
		.then(response=>{
			if (!response.ok) {
				throw new Error("서버 통신 실패");
			}
			return response.json();
		}).then(data=>{
			renderList(data);
		})
		.catch(error => {
        	console.error('Error:', error);
        });
	}
	
	function renderList(data){
		let $template = $("#boardTemplate");
        let $boardList = $("#boardList");
        
        $boardList.empty(); 

        data.communityList.forEach(board => {
            let $clone = $template.clone().removeAttr("id").show();
            
            $clone.find(".userName").text(board.user_name);
            $clone.find(".boardDate").text(timeAgo(board.created_at));
            $clone.find(".boardTitle").text(board.title);
            $clone.find(".boardHits").text("조회 " +board.view_count);
            $clone.find(".boardReplyCnt").text(board.comment_count);
            $clone.find(".boardLink").attr("data-location", "/community/board/" + board.post_id);

            let $favoriteSpan = $clone.find(".favoriteSpan");
            $favoriteSpan.attr("data-board-no", board.post_id);
    		$favoriteSpan.attr("data-board-type", "community");
    		
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

	    $("#pagination a").click(function (e) {
	        e.preventDefault();
	        let pageNum = $(this).data("page");

	        boardState.pageNum = pageNum;
		    getBoard();
	    });
	}


	
</script>
</head>
<body class="wrap">
	<div class="community my-top-15 my-bottom-15">
		<div class="my-top-7 my-bottom-7 flex justify-between items-center">
			<p class="bttitle">COMMUNITY</p>
			<div class="search-div flex justify-center items-center border border-radius-15px">
				<div class="search-bar-wrapper item-center flex justify-space-around">
					
					<input type="text" name="keyword" id="keyword" class="search search-input"/>
					
					<i id="searchBtn" class="glass_icon fa-solid fa-magnifying-glass"></i>
				</div>
			</div>
			<div class="flex">
				<div class="write_btn write_btn_border write_border flex items-center border-radius-7px">
					<button type="button" id="comWriteBtn" class="write_btn_font border-none bColor_fff ">작성하기</button>
				</div>
			</div>
		</div>
		<div class="content">
			<div>
			    <ul style="display:none;">
			        <li id="boardTemplate" class="border-bottom" >
			            <div class="boardLink cursor-pointer pd-2rem flex items-center " >
			                
			                <div class="flex items-center justify-center ml-2 mr-2" style="width: 3rem;">
			                    <span class="favoriteSpan">
			                        <i class="favorite fa-star" style="color: #FFD43B; cursor: pointer;"></i>
			                    </span>
			                </div>
			
			                <div class="title-container flex-1 flex items-center cursor-pointer">
			                    <div class="flex items-center">
			                        <span class="font-size-5 boardTitle"></span>
			                        <span class="ml-05 boardReplyCnt"></span>
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
			                    </div>
			                </div>
			
			            </div>
			        </li>
			    </ul>
			    
			    <ul id="boardList">
				</ul>
			</div>
			
			<div>
				<!-- 페이징 영역 -->
				<div class="text-center">
				    <ul id="pagination" class="pagination pagination_border"></ul>
				</div>
			</div>
		</div>
	</div>
</body>
</html>