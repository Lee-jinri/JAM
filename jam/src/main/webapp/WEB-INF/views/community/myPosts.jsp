<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>커뮤니티 작성 글</title>
<style>
.container {
    max-width: 1000px;
    margin: 0 auto 4rem;
    padding-top: 50px;
}
.user-profile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  background-color: #f9f9f9;
  border-radius: 12px;
  padding: 14px 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  width: fit-content;
  margin: 0 0 20px 0;
}
.profile-info .nickname {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

#userNickname{
	font-size: 20px;
    font-weight: 600;
    color: #7c7c7c;
    margin: 7px 7px;
}


.profile-icon {
  width: 40px;
  height: 40px;
  background-color: #c7e7f3;
  border-radius: 50%;
  color: white;
  font-weight: bold;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-btn-div{
	margin-left: 4px;
}
.sub{
	margin-left: 7px;
	color: #7c7c7c;
}
.posts-meta{
    display: flex;
    justify-content: end;
    align-items: flex-end;
    flex-shrink: 0;
    min-width: 120px;
    max-width: 170px;
}
.check-box {
	appearance: none;
	-webkit-appearance: none;
	width: 20px;
	height: 20px;
	border: 2px solid #d9d9d9;
	border-radius: 6px;
	background: #fff;
	cursor: pointer;
	transition: all 0.2s ease;
	position: relative;
}

.check-box:hover {
	border-color: #9ac7ff;
	box-shadow: 0 0 4px rgba(138, 190, 255, 0.4);
}

.check-box:checked {
	background: #7bbcff;
	border-color: #7bbcff;
	box-shadow: 0 0 6px rgba(123, 188, 255, 0.5);
}

.check-box:checked::after {
	content: '✔';
	position: absolute;
	top: 47%;
	left: 50%;
	transform: translate(-50%, -55%);
	font-size: 13px;
	color: #fff;
	font-weight: 700;
}

</style>
<script>
const state = {
		keyword: "",
		pageNum: 1
		
}
$(function(){
	loadPosts();
	
	if(!window.MY_ID) {
		if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
			location.href = "/member/login";
		} else {
			location.href = "/community/board";
		}
		return;
    }
	
	if(window.MY_NAME) {
		$("#userNickname").text(window.MY_NAME + " 님의 게시글");
    }
	
	$(document).on("click", ".boardLink", function (e) {
	    e.preventDefault();
	    var location = $(this).attr("data-location");
	    if (location) {
	        window.location.href = location;
	    }
	});
	
	$(document).on("click", ".post-check", function (e) {
	    e.stopPropagation();
	});
	
	$("#searchBtn").click(function(){
		let keyword = $("#keyword").val();
		
		if(keyword.replace(/\s/g, "") == ""){
			alert("검색어를 입력하세요.");
			$("#keyword").focus();
			return;
		} 
		state.keyword = keyword;
		state.pageNum = 1;
		
		loadPosts();
	})
		
	$("#keyword").keypress(function(event) {
        if (event.key === "Enter") {
            event.preventDefault(); 
            $("#searchBtn").click();
        }
    });
	
	$("#deletePostsBtn").click(function () {
	    let ids = [];

	    $(".post-check:checked").each(function () {
	        ids.push($(this).val());
	    });

	    if (ids.length === 0) {
	        alert("삭제할 게시글을 선택하세요.");
	        return;
	    }

	    if (!confirm("정말 삭제하시겠습니까?")) return;

	    deletePosts(ids);
	});
	
	$("#checkAll").change(function () {
	    $(".post-check").prop("checked", $(this).is(":checked"));
	    updateSelectedCount();
	});
	
	$(document).on("change", ".post-check", function() {
	    const total = $(".post-check").length;
	    const checked = $(".post-check:checked").length;
	    $("#checkAll").prop("checked", total === checked);
	    
	    updateSelectedCount();
	});
})

function loadPosts(){
    let pageNum = state.pageNum || "1";
    let keyword = state.keyword || "";
    
	let queryString = new URLSearchParams(state).toString();
	let url = "/api/community/my/posts?" + queryString;
	
    fetch(url)
	.then(res=>{
		if (!res.ok) {
			return res.json().then(err => {
		        throw err;
		    });
		}
		return res.json();
	}).then(data=>{
		renderList(data);
	})
	.catch(err => {
		if (handleApiError(err)) return;
		alert(err.detail || '오류가 발생했습니다. 잠시 후 다시 시도하세요.');
	});
}

function renderList(data){
	let $template = $("#boardTemplate");
	let $boardList = $("#boardList");
	     
	$boardList.empty(); 
	
	data.posts.forEach(board => {
		console.log(board);
		let $clone = $template.clone().removeAttr("id").show();
		         
		$clone.find(".boardDate").text(timeAgo(board.created_at));
		$clone.find(".boardTitle").text(board.title);
		$clone.find(".boardHits").text("조회 " +board.view_count);
		$clone.find(".boardReplyCnt").text(board.comment_count);
		$clone.find(".boardLink").attr("data-location", "/community/post/" + board.post_id);
		$clone.find(".check-box-template").removeClass("check-box-template")
	    	.addClass("post-check").prop("disabled", false).val(board.post_id);
		
		$boardList.append($clone);
	});
	     
	loadPagination(data.pageMaker);
}

function deletePosts(ids) {
    fetch("/api/community/posts/my", {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(ids)
    })
    .then(res => {
        if (!res.ok) {
	    	return res.json().then(err => {
	            throw err;
	        });
        }

        alert("삭제되었습니다.");
        $("#checkAll").prop("checked", false);
        $("#selectedCount").text(""); 
        loadPosts();
    })
    .catch(err => {
		if (handleApiError(err)) return;
    	alert(err.detail || '오류가 발생했습니다. 잠시 후 다시 시도하세요.');
	});
}

function updateSelectedCount() {
    const count = $(".post-check:checked").length;
    $("#selectedCount").text(count > 0 ? count + '개 선택됨' : "");
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

        state.pageNum = pageNum;
	    loadPosts();
    });
}
</script>
</head>
<body class="wrap">
	<div class="container my-bottom-15 ">
		<div class="user-profile">
			<div class="profile-info">
			    <div class="nickname">
			    	<span id="userNickname" style="margin-left: 7px"></span>
			    </div>
   				<p class="sub">작성한 커뮤니티 글을 확인할 수 있어요</p>
			</div>
		</div>
		<div class="flex justify-between items-center" style="margin-bottom: 40px;" >
			<div class="flex items-center">
				<div class="neo-wrap flex items-center">
					<input type="text" name="keyword" id="keyword" class="neo-input" placeholder="제목 / 내용 검색">
					<button id="searchBtn" class="search-btn border-none background-none">
					    <img src="/resources/include/images/bubble-search.svg" class="search-icon">
					</button>
				</div>
			</div>
			<button id="comWriteBtn" class="rainbow-btn">
				<i class="fa-solid fa-pencil"></i> 새 글
			</button>
		</div>
		<div class="content">
			<div>
			    <ul style="display:none;">
			        <li id="boardTemplate" class="border-bottom" >
			            <div class="boardLink cursor-pointer pd-2rem flex items-center " >
			                <div class="flex items-center justify-center " style="width: 3rem;">
			                    <input class="check-box-template check-box"type="checkbox" disabled>
			                </div>
			
			                <div class="title-container flex-1 flex items-center cursor-pointer">
			                    <div class="flex items-center">
			                        <span class="font-size-5 boardTitle"></span>
			                        <span class="ml-05 boardReplyCnt"></span>
			                    </div>
			                </div>
			                
			                <div class="posts-meta flex-1 text-right">
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
			    <ul>
			    	<li class="border-bottom">
			    		<div class="cursor-pointer pd-2rem flex items-center ">
			    			<div class="flex items-center justify-center" style="width: 3rem;">
			    				<input type="checkbox" class="check-box" id="checkAll">
			    			</div>
			    			<div>
			    				<button id="deletePostsBtn" class="action-btn" style="margin-left: 2rem;">삭제</button>
			    			</div>
			    			<div>
			    				<span id="selectedCount" class="ml-05" style="color:#7c7c7c; font-size:14px;"></span>
			    			</div>
			    		</div>
			    	</li>
			    </ul>
			    <ul id="boardList">
				</ul>
			</div>
			
			<div>
				<!-- 페이징 영역 -->
				<div class="text-center my-top-8">
				    <ul id="pagination" class="pagination pagination_border"></ul>
				</div>
			</div>
		</div>
	</div>	
</body>
</html>