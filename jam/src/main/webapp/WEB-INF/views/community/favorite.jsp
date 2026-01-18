<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 북마크</title>
<script src="/resources/include/dist/js/favorite.js"></script>
<script src="/resources/include/dist/js/common.js"></script>
<style>

.bookmark-container {
    max-width: 800px;
    margin: 40px auto;
    padding: 20px;
    background-color: #ffffff;
    border-radius: 10px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.bookmark-title {
    text-align: center;
    color: #333;
}

.bookmark-list {
    list-style: none;
    padding: 0;
}

.bookmark-item {
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

.bookmark-item:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
    background-color: #f1f1f1;
}

.bookmark-item a {
    text-decoration: none;
    font-weight: bold;
    color: #666;
}

.bookmark-item a:hover {
    text-decoration: underline;
}

.bookmark-icon {
    color: gold;
    font-size: 20px;
}

.bookmark-info {
    flex-grow: 1;
    margin-left: 10px;
}

.bookmark-meta {
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
	let params = new URLSearchParams(window.location.search);
    let pageNum = parseInt(params.get("pageNum")) || 1;

	getFavorite(pageNum);
	
	$(document).on("click", ".boardLink", function (e) {
	    e.preventDefault();
	    var location = $(this).attr("data-location");
	    
	    if (location) {
	        window.location.href = location;
	    }
	});

})

function getFavorite(pageNum) {
    let url = '/api/community/my/favorites' + '?pageNum=' + pageNum;
    
    fetch(url)
    .then(response => {
    	if (!response.ok) {
			console.log("response : " +response);
			return response.json().then(err => {
		        throw err;
		    });
		}
		return response.json();
    })
    .then(data => {
        renderFavorite(data.favorites);
        loadPagination(data.pageMaker);
    })
    .catch(err => {
    	if (handleApiError(err)) return;
		alert(err.detail || '오류가 발생했습니다. 잠시 후 다시 시도하세요.');
    });
}

function renderFavorite(favoriteList){
	
	let $bookmarkList = $("#bookmark-list");
	$bookmarkList.empty(); // 기존 목록 비우기
	
	if(favoriteList.length === 0){
		const $li = $("<li>").addClass("empty-message").css({
	        padding: "25px",
	        textAlign: "center",
	        color: "#666"
	    });

	    const $span = $("<span>").text("아직 북마크한 글이 없어요.🫠");
	    $li.append($span);

	    $bookmarkList.append($li);
	    return false;
	}
	
	favoriteList.forEach(favorite => {
        const $li = $("<li>").addClass("boardLink bookmark-item cursor-pointer")
            .attr("data-location", "community/post/" + favorite.post_id);

        const $infoDiv = $("<div>").addClass("bookmark-info");
        const $titleSpan = $("<span>").addClass("bookmark-title").text(favorite.title);
        const $metaDiv = $("<div>").addClass("bookmark-meta").text(favorite.created_at);
        $infoDiv.append($titleSpan, $metaDiv);

        const $iconDiv = $("<div>").addClass("flex items-center justify-center ml-2 mr-2").css("width", "3rem");
        const $iconSpan = $("<span>").addClass("favoriteSpan");
        $iconSpan.attr("data-post-id", favorite.post_id);
        $iconSpan.attr("data-board-type", "COM");
        
        const $icon = $("<i>").addClass("favorite fa-star fa-solid").css({ color: "#FFD43B", cursor: "pointer" });
        $iconSpan.append($icon);
        $iconDiv.append($iconSpan);

        $li.append($infoDiv, $iconDiv);
        $bookmarkList.append($li);
    });
}

function loadPagination(pageMaker) {
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
	    
	    let url = "/my/favorites?pageNum="+pageNum;
        
		window.location.href = url;
    });
}
</script>
</head>
<body class="wrap">
	<div class="content">
		<h2 class="bookmark-title">📌 커뮤니티 북마크</h2>
			
        <div class="bookmark-container">
            <ul id="bookmark-list" class="bookmark-list">
               
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