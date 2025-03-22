<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM- 내 글 모아보기</title>
<style>

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
	let params = new URLSearchParams(window.location.search);
    let boardType = params.get("boardType") || "community";
    let pageNum = parseInt(params.get("pageNum")) || 1;

    getWrittenList(boardType, pageNum);
	
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
        
        let newUrl = window.location.pathname + "?boardType=" + newBoardType;
        window.history.pushState(null, "", newUrl);
        
        getWrittenList(newBoardType, 1);
    });
	
	$(document).on("click", ".boardLink", function (e) {
	    e.preventDefault();
	    var location = $(this).attr("data-location");
	    
	    if (location) {
	        window.location.href = location;
	    }
	});

})



function getWrittenList(boardType, pageNum) {
    let url = '/api/written/boards?boardType=' + boardType + '&pageNum=' + pageNum;
    
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
        const $titleSpan = $("<span>").addClass("written-title").text(written.board_title);
        const $metaDiv = $("<div>").addClass("written-meta").text(written.created_at);
        $infoDiv.append($titleSpan, $metaDiv);

        $li.append($infoDiv);
        $writtenUl.append($li);
    });
	
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
	    
	    let url = "/member/written?pageNum="+pageNum + "&boardType=" + boardType;
        
		window.location.href = url;
    });
}
</script>
</head>
<body class="wrap">
	<div class="content">
		<h2 class="written-title">📖 내 글 모아보기</h2>
        
        <div class="button-group">
		    <button data-boardType="community">커뮤니티</button>
		    <button data-boardType="fleaMarket">중고악기</button>
		    <button data-boardType="roomRental">연습실</button>
		    <button data-boardType="job">Jobs</button>
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