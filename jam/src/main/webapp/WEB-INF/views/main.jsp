<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<head>
<meta charset="UTF-8">
<title>JAM</title>
	<script src="/resources/include/dist/js/userToggle.js"></script>
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
		    list.forEach(item => {
		        const $targetDiv = $(targetSelector);
		        
		        let $templateElement = $(templateSelector).clone().removeAttr('id');
		        
		        const datePropertyName = listType + '_date';
		        const hitsPropertyName = listType + '_hits';
		        const replyCntPropertyName = listType + '_reply_cnt';
		        const titlePropertyName = listType + '_title';
				const numberPropertyName = listType + '_no';
				
				
				$templateElement.find(".userName").attr("data-userId", item.user_id);
				$templateElement.find(".userName").html(item.user_name);
		        $templateElement.find(".listDate").html(item[datePropertyName]);
		        $templateElement.find(".listHits").html(item[hitsPropertyName]);
		        $templateElement.find(".listReplyCnt").html(item[replyCntPropertyName]);
		        $templateElement.find(".listTitle").html(item[titlePropertyName]);
		        $templateElement.find(".listTitle").attr("href", linkPrefix + item[numberPropertyName]);

		        $targetDiv.append($templateElement);
		    });
		}
	</script>

</head>

	<div class="contents rem-20 ">
	    <div class="flex flex-container my-top-15">
	        <!-- 구인구직 글 리스트 -->
	        <div class="w-45rem mainDiv">
	            <div class="mainListDiv" id="jobHeader">
	                <div class="ml-2">
	                    <span id="jobHeader_text" class=" font-size-3 font600">Jobs</span>
	                </div>
	            </div>
	
	            <div id="jobList" class="my-top-8 mr-1 ml-1">
	            	<div id="jobTemplate">
		                <ul>
		                    <li class="border-bottom">
	                            <div class="my-top-4 my-bottom-4">
	                                <span class="userName mr-2"></span>
	                                <div class="userNameToggle"></div>
	                                <span class="listDate"></span>
	                            </div>
	                            <div class="flex float-right items-center width-13rem justify-between">
	                                <img class="hitsIcon icon" style="width:2rem;">
	                                <span><i class="fa-regular fa-eye"></i></span>
	                                <span class="listHits font-size-1 ml-05"></span>
	                                <img class="replyIcon icon ml-2"style="width:2rem;">
	                                <span><i class="fa-regular fa-comment-dots"></i></span>
	                                <span class="listReplyCnt ml-05"></span>
	                                
	                            </div>
	                            <div class="my-bottom-4">
	                                <a class="listTitle font-weight-bold font-size-1" ></a>
	                            </div>
		                    </li>
		                </ul>
	                </div>
	            </div>
	        </div>

	        <!-- 합주실 글 리스트 -->   
	        <div class="w-45rem mainDiv">
	            <div class="mainListDiv" id="roomHeader">
	                <div class="ml-2">
	                    <span id="roomHeader_text" class=" font-size-3 font600">합주실/연습실</span>
	                </div>
	            </div>
	
	            <div id="roomList" class="my-top-8 mr-1 ml-1">
	            	<div id="roomTemplate">
		                <ul>
		                    <li class="border-bottom">
	                            <div class="my-top-4 my-bottom-4">
	                                <span class="userName mr-2"></span>
	                                <div class="userNameToggle"></div>
	                                <span class="listDate"></span>
	                            </div>
	                            <div class="flex float-right items-center width-13rem justify-between">
	                                <span><i class="fa-regular fa-eye"></i></span>
	                                <span class="ml-05 listHits"></span>
	                                <span><i class="fa-regular fa-comment-dots"></i></span>
	                                <span class="ml-05 listReplyCnt"></span>
	                            </div>
	                            <div class="my-bottom-4">
	                                <a class="font-weight-bold font-size-1 listTitle"></a>
	                            </div>
		                    </li>
		                </ul>
	                </div>
	            </div>
       		</div>
    	</div>
		<div class="flex flex-container my-top-15">
			<!-- 중고악기 글 리스트 -->
			<div class="w-45rem mainDiv">
				<div class="mainListDiv" id="fleaHeader">
					<div class="ml-2">
						<span id="fleaHeader_text" class=" font-size-3 font600">중고 악기</span>
					</div>
				</div>
			
					
				<div id="fleaList" class="my-top-8 mr-1 ml-1">
					<div id="fleaTemplate">
						<ul>
							<li class="border-bottom">
								
								<div class="my-top-4 my-bottom-4">
									<span class="userName mr-2"></span>
									<div class="userNameToggle"></div>
	                                <span class="listDate"></span>
								</div>
								<div class="flex float-right items-center width-13rem justify-between">
									<span><i class="fa-regular fa-eye"></i></span>
									<span class="listHits font-size-1 ml-05"></span>
									<span><i class="fa-regular fa-comment-dots"></i></span>
									<span class="listReplyCnt ml-05"></span>
								</div>
								<div class="my-bottom-4">
									<a class="listTitle font-weight-bold font-size-1"></a>
								</div>
							</li>
						</ul>
					</div>
				</div>
			</div>
		
			<!-- 커뮤니티 글 리스트 -->
			<div class="w-45rem mainDiv">
				
				<div class="mainListDiv" id="comHeader">
					<div class="ml-2">
						<span id="comHeader_text" class=" font-size-3 font600">커뮤니티</span>
					</div>
				</div>
				
				<div id="comList" class="my-top-8 mr-1 ml-1">
					<div id="comTemplate">
						<ul>
							<li class="border-bottom">
								<div class="my-top-4 my-bottom-4">
									<span class="userName mr-2"></span>
									<div class="userNameToggle"></div>
	                                <span class="listDate"></span>
								</div>
								<div class="flex float-right items-center width-13rem justify-between">
									
									<span><i class="fa-regular fa-eye"></i></span>
									<span class="listHits font-size-1 ml-05"></span>
									<span><i class="fa-regular fa-comment-dots"></i></span>
									<span class="listReplyCnt ml-05"></span>
								</div>
								<div class="my-bottom-4">
									<a class="listTitle font-weight-bold font-size-1" ></a>
								</div>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>
	