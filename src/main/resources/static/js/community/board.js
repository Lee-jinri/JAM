/**
 * 
 */
const boardState = {
		keyword: "",
		pageNum: 1
		
}

$(document).on("click", ".boardLink", function (e) {
    e.preventDefault();
    var location = $(this).attr("data-location");
    if (location) {
        window.location.href = location;
    }
});

$(document).on("click", "#prevPopular", function(){
    if (popularState.pageNum > 1) {
        popularState.pageNum--;
        renderPopularList();
    }else{
    	 popularState.pageNum = popularState.maxPage;
    	 renderPopularList();
    }
});

$(document).on("click", "#nextPopular", function(){
	if (popularState.pageNum < popularState.maxPage) {
        popularState.pageNum++;
        renderPopularList();
    }else{
    	 popularState.pageNum = 1;
    	 renderPopularList();
    }
})

$(document).on("click", ".popular-item", function(){
    var postId = $(this).data("post-id");
    location.href = "/community/post/" + postId;
});

function getBoard(){
    let pageNum = boardState.pageNum || "1";
    let keyword = boardState.keyword || "";
    
	let queryString = new URLSearchParams(boardState).toString();
	let url = "/api/community/board?" + queryString;
	
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
        $clone.find(".boardLink").attr("data-location", "/community/post/" + board.post_id);

        let $favoriteSpan = $clone.find(".favoriteSpan");
        $favoriteSpan.attr("data-post-id", board.post_id);
		$favoriteSpan.attr("data-board-type", "COM");
		
		let $icon = $favoriteSpan.find("i"); 
		board.favorite ? $icon.addClass("fa-solid")
					   : $icon.addClass("fa-regular");
		
        $boardList.append($clone);
    });
    
    loadPagination(data.pageMaker);
}

const popularState = {
    all: [],     
    pageNum: 1,
    pageSize: 5, 
    maxPage: 3 
};

function getPopularBoard(){
	fetch('/api/community/board/popular')
	.then(response=>{
		if(!response.ok) throw new Error('getPopularBoard Error')
		return response.json();
	}).then(data=>{
		popularState.all = data.popularList || [];
		popularState.maxPage = Math.ceil(popularState.all.length / popularState.pageSize) || 1;
		popularState.pageNum = 1;
		renderPopularList();
	}).catch(error => {
    	console.error('Error:', error);
    });
}

function renderPopularList() {
	const startIdx = (popularState.pageNum - 1) * popularState.pageSize;
    const endIdx = startIdx + popularState.pageSize;
    const slice = popularState.all.slice(startIdx, endIdx);

    const $popularList = $("#popularList");
    $popularList.empty();

    slice.forEach(function(p){
        const $item = $('<li class="popular-item cursor-pointer flex border-bottom" data-post-id="' + p.post_id + '">' +
							'<span class="hot-post-title">' + p.title + '</span>' +
							'<span class="hot-comment-count ml-05">' + p.comment_count + '</span>' + 
						'</li>');
        
        $popularList.append($item);
    });
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