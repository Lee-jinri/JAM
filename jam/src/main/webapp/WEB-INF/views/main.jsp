<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<head>
<meta charset="UTF-8">
<title>JAM</title>
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

	

</style>	
	<script>
		$(function(){

			mainBoards().then(() => {
				toggleUserMenu(); 
	        })
	        .catch(error => {
	            console.error('Error while executing mainBoards:', error);
	        });
			
	        
			$("#jobHeader").click(function(){
				location.href = "/job/boards";
			})
			
			$("#roomHeader").click(function(){
				location.href = "/roomRental/boards";
			})
			
			$("#fleaHeader").click(function(){
				location.href = "/fleaMarket/boards";
			})
			
			$("#comHeader").click(function(){
				location.href = "/community/boards";
			})
			
		})
		
		function mainBoards() {
			return new Promise((resolve, reject) => {
		    	fetch('/boards')
		        .then(response => response.json())
		        .then(data => {
		        	
		            const jobList = data.jobList;
		            const roomList = data.roomList;
		            const fleaList = data.fleaList;
		            const comList = data.comList;

		            renderList(jobList, "#jobList", "#jobTemplate", "/job/board/", "job");
		            renderList(roomList, "#roomList", "#roomTemplate", "/roomRental/board/", "roomRental");
		            renderList(comList, "#comList", "#comTemplate", "/community/board/", "com");
		            renderList(fleaList, "#fleaList", "#fleaTemplate", "/fleaMarket/board/", "flea");
		            resolve(); // 모든 작업이 끝나면 resolve 호출
	            })
	            .catch(error => {
	                console.error('Error:', error);
	                reject(error); // 에러가 발생하면 reject 호출
	            });
			});
		}


		function renderList(list, targetSelector, templateSelector, linkPrefix, listType) {
			
		    const $targetUl = $(targetSelector); 
		    $targetUl.empty(); // 기존 목록 초기화

		    list.forEach(item => {
		        const hitsPropertyName = listType + '_hits';
		        const replyCntPropertyName = listType + '_reply_cnt';
		        const titlePropertyName = listType + '_title';
		        const numberPropertyName = listType + '_no';

		        console.log(item);
		        let $listItem = $("<li>").addClass("board-item");

		        let $titleLink = $("<a>")
		            .addClass("listTitle")
		            .attr("href", linkPrefix + item[numberPropertyName])
		            .text(item[titlePropertyName]);

		        let $metaInfo = $("<span>")
		            .addClass("meta-info")
		            .text('조회 ' +  item[hitsPropertyName] + ' | 댓글 ' + item[replyCntPropertyName]);

		        $listItem.append($titleLink, $metaInfo);

		        $targetUl.append($listItem);
		    });
		}
	</script>

</head>
<body>
<div class="contents">
	<!-- Jobs -->
    <div class="mainDiv">
        <div class="mainListDiv" id="jobHeader">📌 Jobs</div>
        <ul id="jobList" class="board-list">
            <li class="board-item">
                <a class="listTitle" href="#"></a>
                <span class="meta-info"></span>
            </li>
        </ul>
    </div>

	<!-- 중고악기 -->
    <div class="mainDiv">
        <div class="mainListDiv" id="fleaHeader">🎸 중고악기</div>
        <ul id="fleaList" class="board-list">
            <li class="board-item">
                <a class="listTitle" href="#"></a>
                <span class="meta-info"></span>
            </li>
        </ul>
    </div>
    
    <!-- 합주실 -->
    <div class="mainDiv">
        <div class="mainListDiv" id="roomHeader">🎵 합주실/연습실</div>
        <ul id="roomList" class="board-list">
            <li class="board-item">
                <a class="listTitle" href="#"></a>
                <span class="meta-info"></span>
            </li>
        </ul>
    </div>

	<!-- 커뮤니티 -->
    <div class="mainDiv">
        <div class="mainListDiv" id="comHeader">💬 커뮤니티</div>
        <ul id="comList" class="board-list">
            <li class="board-item">
                <a class="listTitle" href="#"></a>
                <span class="meta-info"></span>
            </li>
        </ul>
    </div>
</div>
</body>