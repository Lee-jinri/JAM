<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<head>
<meta charset="UTF-8">
<title>JAM</title>
<!-- 
<style>
.contents {
    width: 100%;
    margin: 40px auto;
}
.contents {
    width: 90%;
    margin: 40px auto;
}

.mainDiv {
    width: 100%;
    margin-bottom: 30px;
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s ease-in-out;
}

.mainDiv:hover {
    transform: translateY(-3px); 
}

.mainListDiv {
    padding: 15px;
    font-size: 18px;
    font-weight: bold;
    border-radius: 12px;
    margin-bottom: 15px;
    color: white;
    cursor: pointer;
    transition: background 0.3s ease-in-out;
}

.board-list {
    list-style: none;
    padding: 0;
}

.board-item {
    padding: 12px 15px;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: background 0.2s ease-in-out;
}

.board-item:hover {
    background: rgba(0, 0, 0, 0.05);
    border-radius: 8px;
}

.listTitle {
    text-decoration: none;
    font-size: 16px;
    font-weight: 600;
    color: #333;
}

.listTitle:hover {
    text-decoration: underline;
    color: #4A90E2;
}

.meta-info {
    font-size: 14px;
    color: #666;
    display: flex;
    gap: 15px;
}

.jobs-banner{
  width: 100%;
  background-color: #f5f5f5;
  padding: 30px 20px;
  margin: 20px 0;
  border-radius: 10px;
  transition: background-color 0.3s;
}


.jobs-banner:hover{
  background-color: #e0e0e0;
}

/* 링크 스타일 제거 */
.banner-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

/* 텍스트 정렬 및 스타일 */
.banner-content {
  text-align: center;
}

.banner-content h2 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.banner-content p {
  margin-top: 10px;
  font-size: 16px;
  color: #555;
}


</style>	 -->
<style>
body {
    margin: 0;
    padding: 0;
    font-family: 'Pretendard', sans-serif;
    color: #3A1E13;
}

.contents {
    width: 92%;
    max-width: 1100px;
    margin: 40px auto;
}

.section {
    border-radius: 18px;
    border: 1px solid rgba(255,255,255,0.25);
    box-shadow: 0 8px 22px rgba(0,0,0,0.06);
    margin-top: 30px;
}

.section-title {
    font-size: 20px;
    font-weight: 700;
    letter-spacing: 2px;
    margin-top: 12px;
    position: relative;
    margin-left: 30px;
}
.section-title::after {
    content: "";
    display: block;
    width: 44px;
    height: 2px;
    background: var(--pink);
    margin-top: 14px;
    border-radius: 2px;
}

.board-list {
    list-style: none;
    padding: 10px;
    padding-top: 0;
    margin: 0 20px;
    border-radius: 20px;
    background-color: #eff8ff;
}

.board-item {
    padding: 12px 6px;
    border-bottom: 1px solid #E9E3DD;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: .2s;
    cursor: pointer;
}

.board-item:hover {
    transform: translateX(2px);
    opacity: 0.85;
    color: #CF93B6;
}
.listTitle {
    font-size: 16px;
    font-weight: 500;
    color: var(--brown);
    letter-spacing: 0.6px;
}

.listTitle:hover {
    color: #CF93B6;
}

.meta-info {
    font-size: 15px;
    color: rgba(58,30,19,0.65);
    letter-spacing: 0.3px;
}
.main-pagination{
    margin: 10px 0;
}
.post-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
	gap: 20px;
	padding: 4px 20px 0 20px;
}

.post-card {
	cursor: pointer;
	border: 1px solid #ddd;
	border-radius: 14px;
	background: #fff;
	box-shadow: 0 4px 10px rgba(0,0,0,0.06);
	transition: transform .18s, box-shadow .18s;
}

.post-card:hover {
	transform: translateY(-6px);
	box-shadow: 0 10px 24px rgba(0,0,0,0.12);
}

.post-card img {
	width: calc(100% - 20px);
	height: 180px;
	object-fit: cover;
	border-radius: 14px;
	margin: 10px;
}

.post-title {
	font-size: 16px;
	height: 2.6em;
	overflow: hidden;
}
.post-content { margin: 10px; }
.post-price { font-size: 15px; margin: 6px 0; color: #111; }
.post-createdAt { font-size: 14px; color: #666; }

</style>
<script>
const communityPopularState = { all: [], pageNum: 1, pageSize: 5, maxPage: 1 };
const fleaState            = { all: [], pageNum: 1, pageSize: 4, maxPage: 1 };
const communityLatestState = { all: [], pageNum: 1, pageSize: 5, maxPage: 1 };

    $(function() {
       	loadMainBoards();
		getPopularBoard();

		$("#prevPopular").click(() => handlePaging(communityPopularState, renderCommunityPopular, "prev"));
		$("#nextPopular").click(() => handlePaging(communityPopularState, renderCommunityPopular, "next"));

		$("#prevFlea").click(() => handlePaging(fleaState, renderFlea, "prev"));
		$("#nextFlea").click(() => handlePaging(fleaState, renderFlea, "next"));

		$("#prevComLatest").click(() => handlePaging(communityLatestState, renderCommunityLatest, "prev"));
		$("#nextComLatest").click(() => handlePaging(communityLatestState, renderCommunityLatest, "next"));
		
		$(document).on("click", ".board-item", function () {
		    location.href = $(this).data("href");
		});
		
		$(document).on("click", ".post-card", function () {
			location.href = $(this).data("location");
		});
    });

    function getPopularBoard(){
		fetch('/api/community/board/popular')
		.then(response=>{
			if(!response.ok) throw new Error('getPopularBoard Error')
			return response.json();
		}).then(data=>{
			communityPopularState.all = data.popularList || [];
			communityPopularState.maxPage = Math.ceil(communityPopularState.all.length / communityPopularState.pageSize) || 1;
			communityPopularState.pageNum = 1;
			
			renderCommunityPopular();
		}).catch(error => {
        	console.error('Error:', error);
        });
	}
    
    function loadMainBoards() {
        fetch('/boards')
        .then(response => response.json())
        .then(data => {
        	communityLatestState.all = data.comList || [];
        	communityLatestState.maxPage = Math.ceil(communityLatestState.all.length / communityLatestState.pageSize) || 1;
        	communityLatestState.pageNum = 1;
			
        	fleaState.all = data.fleaList || [];
        	fleaState.maxPage = Math.ceil(fleaState.all.length / fleaState.pageSize) || 1;
        	fleaState.pageNum = 1;
			
        	renderFlea();
        	renderCommunityLatest();
        })
        .catch(console.error);
    }

    function renderCommunityPopular() {
    	const start = (communityPopularState.pageNum - 1) * communityPopularState.pageSize;
    	const end = start + communityPopularState.pageSize;
    	const pageData = communityPopularState.all.slice(start, end);
    	renderList(pageData, "#hotList", "/community/post/", "com");
    }

    function renderFlea() {
    	const start = (fleaState.pageNum - 1) * fleaState.pageSize;
    	const end = start + fleaState.pageSize;
    	const pageData = fleaState.all.slice(start, end);

    	const $target = $("#fleaList");
    	$target.empty();

    	pageData.forEach(post => {
			console.log(post.post_id);
			let $card = $(`
					<div class="post-card" data-location="/fleaMarket/post/\${post.post_id}">
						<img>
						<div class="post-content">
							<div class="post-title"></div>
							<div class="post-price"></div>
							<div class="post-meta">
								<span class="post-createdAt"></span>
							</div>
						</div>
					</div>
				`);
    		$("#fleaList").append($card);

    		$card.find('.post-title').text(post.title);
    		$card.find('.post-price').text(post.price.toLocaleString() + '원');
    		$card.find('.post-createdAt').text(timeAgo(post.created_at));

    		if (!post.thumbnail) {
    			$card.find('img').attr('src', '/resources/include/images/no-image.png');
    		} else {
    			$card.find('img').attr('src', '/images/flea/' + post.thumbnail);
    		}
    		$target.append($card);
    	});
    }


    function renderCommunityLatest() {
    	const start = (communityLatestState.pageNum - 1) * communityLatestState.pageSize;
    	const end = start + communityLatestState.pageSize;
    	const pageData = communityLatestState.all.slice(start, end);
    	renderList(pageData, "#comList", "/community/post/", "com");
    }
    
    function renderList(list, targetSelector, linkPrefix, listType) {
        const $targetUl = $(targetSelector);
        $targetUl.empty();
		
        list.forEach(item => {
            const title = item[listType + '_title'] || item.title;
            const no = item[listType + '_no'] || item.post_id;
            const view_count = item[listType + '_view_count'] || item.view_count;
            const replies = item[listType + '_comment_count'] || item.comment_count;

            let $li = $("<li>").addClass("board-item").attr("data-href", linkPrefix + no);
            let $a = $("<a>").addClass("listTitle").text(title);
            let $meta = $("<span>").addClass("meta-info").text("조회 " + view_count + " | 댓글 " + replies);

            $li.append($a, $meta);
            $targetUl.append($li);
        });
    }
    function handlePaging(state, renderFn, direction) {
    	if (direction === "prev") {
    		state.pageNum = state.pageNum > 1 ? state.pageNum - 1 : state.maxPage;
    	} else {
    		state.pageNum = state.pageNum < state.maxPage ? state.pageNum + 1 : 1;
    	}
    	renderFn();
    }
    
</script>


</head>
<body>

	<div class="contents">
		<div>
			<img src="/resources/include/images/Jobs-banner.png" style="width: 930px; border-radius: 20px;"">
		</div>
		
	    <div class="section">
	        <div class="section-title">🔥 인기글</div>
	        <ul id="hotList" class="board-list"></ul>
	        <div class="main-pagination text-center my-top-5">
				<button id="prevPopular" class="arrow-btn">
					<i class="fa-solid fa-arrow-left fa-sm"></i>
				</button>
				<button id="nextPopular" class="arrow-btn">
					<i class="fa-solid fa-arrow-right fa-sm"></i>
				</button>
			</div>
	    </div>
	
	    <div class="section">
	        <div class="section-title" id="fleaHeader">🎸 최근 올라온 중고악기 매물</div>
	        <div id="fleaList" class="post-grid"></div>
	        <div class="main-pagination text-center my-top-5">
				<button id="prevFlea" class="arrow-btn">
					<i class="fa-solid fa-arrow-left fa-sm"></i>
				</button>
				<button id="nextFlea" class="arrow-btn">
					<i class="fa-solid fa-arrow-right fa-sm"></i>
				</button>
			</div>
	    </div>
	
	    <div class="section">
	        <div class="section-title" id="comHeader">💬 커뮤니티</div>
	        <ul id="comList" class="board-list"></ul>
	        <div class="main-pagination text-center my-top-5">
				<button id="prevComLatest" class="arrow-btn">
					<i class="fa-solid fa-arrow-left fa-sm"></i>
				</button>
				<button id="nextComLatest" class="arrow-btn">
					<i class="fa-solid fa-arrow-right fa-sm"></i>
				</button>
			</div>
	    </div>
	
	</div>

</body>
