<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM</title>

<script src="/resources/include/dist/js/categories.js"></script>
<script src="/resources/include/dist/js/favorite.js"></script>	
<style>

.f-board {
	padding-top: 40px;
}

.postTopBar {
	height: 45px;
}

.title-container {
	flex-grow: 1;
	min-width: 250px;
	max-width: 680px;
	margin-left: 7rem;
}

.userName-div {
	max-width: 150px;
	flex: 1;
	justify-content: center;
}

.date-container {
	display: flex;
	flex-direction: column;
	align-items: flex-end; /* 오른쪽 정렬 */
	flex-shrink: 0; /* 자동 크기 축소 방지 */
	min-width: 120px; /* 너무 넓지 않게 조절 */
	max-width: 170px; /* 필요 이상으로 커지지 않게 제한 */
}

.user_toggle {
	left: -6rem;
	top: 3rem;
}

.search-div {
	max-width: 520px;
	margin-left: 20px;
	margin-right: 155px;
	margin-top: 0;
}

.f-board-button {
	font-size: 15px;
	width: 87px;
	position: relative;
	padding: 0 12px;
}

.f-board-button:not(:first-child) {
	margin-left: 8px;
}

.f-board-button:not(:first-child)::before {
	content: '';
	position: absolute;
	left: 0;
	top: 50%;
	transform: translateY(-50%);
	width: 1px;
	height: 14px;
	background-color: #ccc;
}


/**/
.category-table {
	margin: 46px 0 30px 0;
	display: table;
	width: 100%;
	table-layout: fixed;
}

.category-row {
	display: table-row;
	outline: 1px solid #eee;
	border-radius: 16px;
	overflow: hidden;
}

.category-cell {
	display: table-cell;
	text-align: center;
	vertical-align: middle;
	padding: 7px 0;
	font-size: 15px;
	color: #444;
	cursor: pointer;
}

.category-cell a {
	display: inline-block; 
	padding: 7px 31px;
	border-radius: 12px;
	transition: .22s;
	line-height: 1;
}

.category-cell:hover a, .selected-category a {
	background: #e5ebff;
	padding: 7px 16px; 
	border-radius: 14px;
	color: #3b4abb;
	font-weight: 600;
	box-shadow: 0 4px 8px rgba(80, 110, 255, 0.22);
}


.post-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
	gap: 20px;
	padding: 10px 20px 60px 20px;
}

.post-card {
	cursor: pointer;
	border: 1px solid #ddd;
	border-radius: 14px;
	overflow: hidden;
	background-color: #fff;
	font-family: sans-serif;
	box-shadow:
		0 4px 10px rgba(0, 0, 0, 0.06);
	transition:
		transform 0.18s ease-out,
		box-shadow 0.18s ease-out;
}

.post-card img {
	width: calc(100% - 20px);
    height: 200px;
    margin: 10px;
    border-radius: 14px;
    object-fit: cover;
}
.post-card:hover {
	transform: translateY(-6px);
	box-shadow:
		0 10px 24px rgba(0, 0, 0, 0.12); 
}
.post-content {
	padding: 10px;
}

.post-title {
	font-size: 16px;
	line-height: 1.3em;
	height: 2.6em;
	overflow: hidden;
	text-overflow: ellipsis;
}

.post-category{
	font-size: 13px;
    color: #666;
}
.post-price {
	font-size: 15px;
	color: #000;
	margin: 0 0 8px;
}

.post-meta {
	color: #666;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.post-createdAt {
	font-size: 14px;
}

.search-input:focus{
	outline: none;
	border: none; 
	box-shadow: none;
}
.f-board-button::after {
	content: "";
	position: absolute;
	left: 50%;
	bottom: -7px;
	width: 0;
	height: 2px;
	background: #bfc8ea;
	border-radius: 2px;
	transition: width .25s ease, left .25s ease;
}
.f-board-button:hover {
	color: #4a4a4a;
}
.f-board-button:hover::after {
	width: 100%;
	left: 0;
}
</style>
<script type="text/javascript">
$(function(){
	getBoard().then(() => {
		toggleUserMenu(); 
    })
    .catch(error => {
        console.error('Error while executing fleaMarket board:', error);
        alert('게시글을 불러오는데 실패했습니다. 잠시 후 다시 시도해 주세요.');
    });
	
	$(document).on("click", ".post-card", function (e) {
	    e.preventDefault();
	    var location = $(this).attr("data-location");
	    if (location) {
	    	window.location.href = location;
	    }
	});
	
	document.querySelector('.search-input').addEventListener('keydown', function(event) {
		if (event.key === 'Enter') {
			event.preventDefault(); 
			document.querySelector('#searchBtn').click();
		}
	});
	
	$("#keyword").keypress(function(event) {
        if (event.key === "Enter") {
            event.preventDefault(); 
            $("#searchBtn").click();
        }
    });
	
	$("#searchBtn").click(function(){
		let keyword = $("#keyword").val();
		
		if(keyword.replace(/\s/g, "") == ""){
			alert("검색어를 입력하세요.");
			$("#keyword").focus();
			return;
		}
		
		location.href = '/fleaMarket/board?&keyword='+keyword+'&pageNum=1';
	})
	
	$(".category-cell").click(function(e){
		e.preventDefault();
	
		let categoryId = $(this).find("a").data("category"); 
		
		if(categoryId == "0") location.href = "/fleaMarket/board";
		else{
			let params = new URLSearchParams(window.location.search);
			let keyword = params.get("keyword") || "";
			let pageNum = 1; 
		
			location.href = "/fleaMarket/board?pageNum=" + pageNum +
			                "&keyword=" + keyword +
			                "&category_id=" + categoryId;
		}
	});

	$("#flea-writeBtn").click(function(){
		redirectWithLogin("/fleaMarket/board/write");
	});

	$("#flea-myStoreBtn").click(function(){
		redirectWithLogin("/fleaMarket/my?view=store");
	});

	$("#flea-favoriteBtn").click(function(){
		redirectWithLogin("/fleaMarket/my?view=favorites");
	});

	$("#flea-chatBtn").click(function(){
		redirectWithLogin("/chat", function(){
			sessionStorage.removeItem("chatRoomId");
		});
	});
})

function redirectWithLogin(authedUrl, beforeAuthed) {
	if (!window.MY_ID) {
		if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
			location.href = "/member/login";
		} else {
			location.href = "/fleaMarket/board";
		}
	} else {
		if (beforeAuthed) beforeAuthed(); 
		location.href = authedUrl;
	}
}

function getBoard(){
	return new Promise((resolve, reject) => {
		let params = new URLSearchParams(window.location.search);
		let pageNum = params.get("pageNum") || "1";
		let keyword = params.get("keyword");
		let category_id = params.get("category_id") || null;

		let url = '/api/fleaMarket/board?pageNum=' + pageNum;

		if (keyword && keyword.trim() !== "") {
			url += '&keyword=' + encodeURIComponent(keyword);
		}
		if (category_id != null){
 			url += '&category_id=' + category_id;
			
 			$('.category-cell a[data-category="' + category_id + '"]')
				.parent()
				.addClass("selected-category");
		}else{
			$('.category-cell a[data-category="' + 0 + '"]')
			.parent()
			.addClass("selected-category");
		}
		
	    fetch(url)
		.then(response=>{
			if (!response.ok) throw new Error("서버 응답 오류");
			return response.json();
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
	let $postGrid = $(".post-grid");
	let $template = $("#postTemplate");

	$template.hide(); 
	$postGrid.empty();
	/* FIXME: sales_status 판매중인 것만 가져오기 즐겨찾기한 글에서는 모든 sales_status 조회
	*/
	let subMap = {};
	setTimeout(function () {
	    subMap = window.subMap;
	    
	    data.fleaMarketList.forEach(post =>{
			let $clone = $template.clone().removeAttr("id").show();
			
			$clone.find('.post-title').text(post.title);
			$clone.find('.post-price').text(post.price.toLocaleString() + '원');
			$clone.find('.post-createdAt').text(timeAgo(post.created_at)); 
			
			if (!post.thumbnail || post.thumbnail === '') {
				$clone.find('img').attr('src', '/resources/include/images/no-image.png');
			} else {
				$clone.find('img').attr('src', '/upload/flea/' + post.thumbnail);
			}

			let savedSub = post.category_id;
			let { big, name } = subMap[savedSub];

			$clone.find('.post-category').text(savedSub >= 8 ? name : ""); 
			
			$clone.attr("data-location", "/fleaMarket/post/" + post.post_id);
			
			let $favoriteSpan = $clone.find(".favoriteSpan");
			$favoriteSpan.attr("data-post-id", post.post_id);
			$favoriteSpan.attr("data-board-type", "fleaMarket");
			
			let $icon = $favoriteSpan.find("i"); 
			post.favorite ? $icon.addClass("fa-solid")
						   : $icon.addClass("fa-regular");
			
	        $postGrid.append($clone);
		})
	    
	    loadPagination(data.pageMaker);
	}, 0);
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
	    let keyword = params.get("keyword") || "";
	    
	    let url = "/fleaMarket/board?pageNum="+pageNum+"&keyword="+keyword;
        
		window.location.href = url;
    });
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
<body class="wrap">
	<div class="f-board fleaMarket">
		<div class="my-bottom-15">
			
			<div class="postTopBar flex justify-between item-center">
				<div class="flex items-center">
					<div class="neo-wrap flex items-center">
						<input type="text" name="keyword" id="keyword" class="search-input neo-input"
						value="${not empty param.keyword ? param.keyword : ''}" />
						
						<button id="searchBtn" class="search-btn border-none background-none">
						    <img src="/resources/include/images/bubble-search.svg" class="search-icon">
						</button>
					</div>
				</div>
				
				<div class="f-board-buttons flex items-center border-radius-7px">
					<button id="flea-writeBtn" class="f-board-button write_btn_font bColor_fff border-none font-color-gray ">판매하기</button>
					<button id="flea-myStoreBtn" class="f-board-button write_btn_font border-none bColor_fff font-color-gray ">내 상점</button>
					<button id="flea-favoriteBtn" class="f-board-button write_btn_font border-none bColor_fff font-color-gray ">찜한상품</button>
					<button id="flea-chatBtn" class="f-board-button write_btn_font border-none bColor_fff font-color-gray ">채팅</button>
					
				</div>
			</div>
			
			<!-- 대분류 -->
			<div class="category-table">
				<div class="category-row">
					<div class="category-cell"><a href="#" data-category="0">전체</a></div>
					<div class="category-cell"><a href="#" data-category="1">기타</a></div>
					<div class="category-cell"><a href="#" data-category="2">건반악기</a></div>
					<div class="category-cell"><a href="#" data-category="3">드럼</a></div>
					<div class="category-cell"><a href="#" data-category="4">관악기</a></div>
					<div class="category-cell"><a href="#" data-category="5">현악기</a></div>
					<div class="category-cell"><a href="#" data-category="6">장비</a></div>
					<div class="category-cell"><a href="#" data-category="7">그 외</a></div>
				</div>
			</div>
			
			<div class="post-grid">
				<div id="postTemplate" class="post-card">
					<img>
					<div class="post-content">
						<div class="post-title"></div>
						<div class="post-category"></div>
						<div class="post-price"></div>
						<div class="post-meta">
							<span class="post-createdAt"></span>
							<span class="favoriteSpan">
								<i class="favorite fa-star" style="color: #FFD43B; cursor: pointer;"></i>
							</span>
						</div>
					</div>
				</div>
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