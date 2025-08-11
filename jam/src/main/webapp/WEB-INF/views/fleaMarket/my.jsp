<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>FLEAMARKET</title>
<style>
.f-myStore{
	padding-top: 60px;
}
.search-div {
	max-width: 520px;
	margin-top: 0;
}

.post-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
	gap: 20px;
	padding: 50px 20px;
}

.post-card {
	cursor: pointer;
	border: 1px solid #ddd;
	border-radius: 8px;
	overflow: hidden;
	background-color: #fff;
	font-family: sans-serif;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.post-card img {
	width: 100%;
	height: 210px;
	object-fit: cover;
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

.post-price {
	font-size: 15px;
	color: #000;
	margin: 8px 0 4px;
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
.search-input{
	border-bottom: none;
}

.search-input:focus{
	outline: none;
	border: none; /* 필요하면 border-color만 변경도 가능 */
	box-shadow: none;
}

.image-wrapper {
	position: relative;
	width: 100%;
}

.image-wrapper img {
	width: 100%;
	height: 210px;
	object-fit: cover;
	display: block;
}

.sold-out-overlay {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	background-color: rgba(0, 0, 0, 0.5);
	color: #fff;
	font-size: 16px;
	font-weight: bold;
	padding: 10px 20px;
	border-radius: 50%;
	text-align: center;
	pointer-events: none;
}

.store-header {
	margin: 40px 20px 10px;
	padding: 10px 20px;
	border-left: 4px solid #444;
	background-color: #f9f9f9;
	border-radius: 5px;
}

.store-title {
	font-size: 20px;
	font-weight: bold;
	color: #333;
	margin: 0;
	cursor: pointer;
}

.my-tabs {
	display: flex;
	gap: 10px;
	margin: 20px;
}

.my-tabs button {
	padding: 8px 16px;
	border: 1px solid #ddd;
	background-color: #f9f9f9;
	border-radius: 20px;
	cursor: pointer;
	font-size: 14px;
	transition: all 0.2s;
}

.my-tabs button:hover {
	background-color: #e6f0ff;
	border-color: #8ab4f8;
}

.my-tabs button.active {
	background-color: #8ab4f8;
	color: white;
	border-color: #8ab4f8;
}



</style>
<script>
$(function(){
	let params = new URLSearchParams(window.location.search);
	let view = params.get("view") || "store";
	
	if (view === "favorites") {
		loadData('/api/fleaMarket/my/favorites');
	} else {
		loadData('/api/fleaMarket/my/store');
	}
	
	
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

	
	$("#searchBtn").click(function(){
		let keyword = $("#keyword").val();
		
		if(keyword.replace(/\s/g, "") == ""){
			alert("검색어를 입력하세요.");
			$("#keyword").focus();
			return;
		}
		
		let params = new URLSearchParams(window.location.search);
		let view = params.get("view") || "store";
		
		location.href = "/fleaMarket/my?view=" + view + "&pageNum=1&keyword=" + encodeURIComponent(keyword);
	})
	
	$("#keyword").keypress(function(event) {
        if (event.key === "Enter") {
            event.preventDefault(); 
            $("#searchBtn").click();
        }
    });
	
	$(".store-title").click(function(){
		location.href = '/fleaMarket/my?view=store';
	})
	
	$(".paginate_button a").click(function(e) {
		e.preventDefault();
		$("#searchForm").find("input[name='pageNum']").val($(this).attr("href"));
		goPage();
	})
})

function loadData(apiUrl){
	let params = new URLSearchParams(window.location.search);
	let pageNum = params.get("pageNum") || "1";
	let keyword = params.get("keyword") || "";
	
	let url = apiUrl + '?pageNum=' + pageNum + '&keyword=' + encodeURIComponent(keyword);
console.log(url);
	fetch(url)
	.then(res=>{
		if(res.status === 401){
			if(confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
				location.href = "/member/login";
			}else{
				location.href = "/fleaMarket/board";
			}
			throw new Error("Redirecting due to unauthorized");
		}else if(res.status != 200) {
			alert("시스템 오류입니다. 잠시 후 다시 시도하세요.");
			location.href = "/fleaMarket/board";
			throw new Error("System error");
		}
		return res.json();
	}).then(data=>{
		renderList(data);
	})
	.catch(error => {
		console.error('Error:', error.message);
		if (!error.message.includes("Redirecting")) {
			alert('게시글을 불러오는데 실패했습니다. 잠시 후 다시 시도해 주세요.');
		}
	});
}

function renderList(data){
	let $postGrid = $(".post-grid");
	let $template = $("#postTemplate");

	$template.hide(); 
	$postGrid.empty();
	
	$(".store-title").text(data.userName + '님의 상점');
	
	if(data.fleaMarketList.length === 0) {
		$postGrid.append('<div class="no-posts">등록된 게시글이 없습니다.</div>');
		return;
	}
	data.fleaMarketList.forEach(post =>{
		let $clone = $template.clone().removeAttr("id").show();
		
		$clone.find('.post-title').text(post.title);
		$clone.find('.post-price').text(post.price.toLocaleString() + '원');
		$clone.find('.post-createdAt').text(timeAgo(post.created_at)); 
		
		if (!post.thumbnail || post.thumbnail === '') {
			$clone.find('img').attr('src', '/resources/include/images/no-image.png');
		} else {
			$clone.find('img').attr('src', '/images/flea/' + post.thumbnail);
		}
		
		$clone.attr("data-location", "/fleaMarket/post/" + post.post_id);
		
		if (post.sales_status === 1) {
			let $overlay = $('<div class="sold-out-overlay">판매 완료</div>');
			$clone.find('.image-wrapper').append($overlay);
		}
		
        $postGrid.append($clone);
	})
	
    loadPagination(data.pageMaker);
}


function loadPagination(pageMaker) {
    const $pagination = $("#pagination");
    $pagination.empty(); 

    let params = new URLSearchParams(window.location.search);
	let view = params.get("view") || "store";
	let keyword = params.get("keyword") || "";
	
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

		
		window.location.href = url;
	});
    
	$("#pagination a").click(function (e) {
	    e.preventDefault();
	    let pageNum = $(this).data("page");
	    let params = new URLSearchParams(window.location.search);
		let keyword = params.get("keyword") || "";
				 
		let url = "/fleaMarket/my?view=" + view + "&pageNum=" + pageNum + "&keyword=" + encodeURIComponent(keyword);
		    
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
	
	let dateStr = past.toLocaleDateString();
	return dateStr.replace(/\.$/, '');
}
</script>
</head>
<body class="wrap">
	<div class="f-myStore my-bottom-15">
		<div class="search-div flex justify-center items-center border border-radius-43px">
			<div class="search-bar-wrapper item-center flex justify-space-around">
			
				<input type="text" name="keyword" id="keyword" class=" rem-2 search search-input"
				value="${not empty param.keyword ? param.keyword : ''}" />
				
				<i id="searchBtn" class="glass_icon fa-solid fa-magnifying-glass"></i>
			</div>
		</div>
		
		<div class="store-header">
			<h2 class="store-title"></h2>
		</div>
	
		<div class="my-tabs">
			<button id="storeTab" onclick="location.href='/fleaMarket/my?view=store'">내 상점</button>
			<button id="favTab" onclick="location.href='/fleaMarket/my?view=favorites'">찜한 상품</button>
		</div>
	
		<div class="post-grid">
			<div id="postTemplate" class="post-card">
				<div class="image-wrapper">
					<img>
				</div>
				<div class="post-content">
					<div class="post-title"></div>
					<div class="post-price"></div>
					<div class="post-meta">
						<span class="post-createdAt"></span>
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
</body>
</html>