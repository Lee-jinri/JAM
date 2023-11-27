<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<head>
<meta charset="UTF-8">
<title>JAM</title>

	<style>
		.search {	
				display: flex;
	    		justify-content: center;
	    	}
	    	#search_bar {
	    		width : 550px; 
	    		height : 45px;
	    		text-align:center;
			    border: 5px solid #ffb689;
			    border-radius: 30px;
			    font-size: 16px;
			    font-weight: 500;
			    font-color : #ff7130;
	    		}
	    	.search-icon{
				width : 30px;
				object-fit : contain;
				margin-left : 15px;
			}
			input::placeholder {
	  color: #ffb689;
	}
			.flex-container{flex-wrap:wrap; column-gap: 5rem; row-gap: 2.5rem; flex-direction: row; justify-content: center;}
			.w-50rem {width:50rem;}
			.mainListDiv{
				width: 50rem;
			    height: 7rem;
			    border: #FBF3E3 solid;
			    border-radius: 2rem;
			    background-color: #FBF3E3;
			   }
	</style>
	
	<script>
		$(function(){
			$("#jobList").click(function(){
				location.href = "/job/jobList";
			})
			
			$("#roomList").click(function(){
				location.href = "/roomRental/roomRentalList";
			})
			
			$("#fleaList").click(function(){
				location.href = "/fleaMarket/fleaMarketList";
			})
			
			$("#comList").click(function(){
				location.href = "/community/communityList";
			})
			
			
			
			$('#jobList').hover(function() {
			    $("#jobList_text").css("color","#2E9AFE");
				$("#jobList").css("cursor","pointer");
				$("#jobList_text").css('Text-decoration','none');
			}, function(){
				$("#jobList_text").css('color','#FAAC58');
			});
			
			$("#roomList").hover(function(){
				$("#roomList_text").css("color","#2E9AFE");
				$("#roomList").css("cursor","pointer");
				$("#roomList_text").css('Text-decoration','none');
			}, function(){
				$("#roomList_text").css('color','#FAAC58');
			});
			
			$("#fleaList").hover(function(){
				$("#fleaList_text").css("color","#2E9AFE");
				$("#fleaList").css("cursor","pointer");
				$("#fleaList_text").css('Text-decoration','none');
			}, function(){
				$("#fleaList_text").css('color','#FAAC58');
			});
			
			$("#comList").hover(function(){
				$("#comList_text").css("color","#2E9AFE");
				$("#comList").css("cursor","pointer");
				$("#comList_text").css('Text-decoration','none');
			}, function(){
				$("#comList_text").css('color','#FAAC58');
			});
			
			
			
		})
	</script>

</head>

<body class="wrap">
	<div class="contents rem-20 ">
		<div class="flex flex-container my-top-15 my-bottom-15">
		
			<!-- 구인구직 글 리스트 -->
			<div class="w-50rem " >
				<div class="mainListDiv" id="jobList">
					<div class="my-top-75 ml-2">
						<span id="jobList_text" class=" font-size-3 color_o font600">구인/구직</span>
					</div>
				</div>
					
				<div class="my-top-8 mr-1 ml-1">
					<ul>
						<c:forEach items="${jobList }" var="jobList" varStatus="status">
							<li class="border-bottom">
								<div class="">
									<div class="my-top-4 my-bottom-4">
										<span>${jobList.user_name }</span>
										<span> | </span>
										<span>${jobList.job_date }</span>
									</div>
									<div class="flex float-right items-center width-13rem justify-between">
										<img class="icon" id="" style="width:2rem;" src="/resources/include/images/hits.svg">
										<span class="font-size-1 ml-05">${jobList.job_hits }</span>
										<img class="icon ml-2" id="" style="width:2rem;" src="/resources/include/images/reply.svg">
										<span class=" ml-05">${jobList.job_reply_cnt }</span>
									</div>
									<div class="my-bottom-4">
										<a class="font-weight-bold font-size-1" href="/job/jobDetail/${jobList.job_no }" >${jobList.job_title }</a>
									</div>
									
								</div>
							</li>
						</c:forEach>
					</ul>
				</div>
			</div>
		
			<!-- 합주실 글 리스트 -->	
			<div class="w-50rem">
				<div class="mainListDiv" id="roomList">
					<div class="my-top-75 ml-2">
						<span id="roomList_text" class=" font-size-3 color_o font600">합주실/연습실</span>
					</div>
				</div>
				
				
				<div class="my-top-8 mr-1 ml-1">
					<ul>
						<c:forEach items="${roomList }" var="roomList" varStatus="status">
							<li class="border-bottom">
								<div class="">
									<div class="my-top-4 my-bottom-4">
										<span>${roomList.user_name }</span>
										<span> | </span>
										<span>${roomList.roomRental_date }</span>
									</div>
									<div class="flex float-right items-center width-13rem justify-between ">
										<img class="icon" id="" style="width:2rem;" src="/resources/include/images/hits.svg">
										<span class="ml-05">${roomList.roomRental_hits }</span>
										<img class="icon ml-2" id="" style="width:2rem;" src="/resources/include/images/reply.svg">
										<span class="ml-05">${roomList.roomRental_reply_cnt }</span>
									</div>
									<div class="my-bottom-4">
										<a class="font-weight-bold font-size-1" href="/roomRental/roomRentalDetail/${roomList.roomRental_no }" >${roomList.roomRental_title }</a>
									</div>
									
								</div>
							</li>
						</c:forEach>
					</ul>
				</div>
			</div>
		</div>
		<div class="flex flex-container">
			<!-- 중고악기 글 리스트 -->
			<div class="w-50rem ">
				<div class="mainListDiv" id="fleaList">
					<div class="my-top-75 ml-2">
						<span id="fleaList_text" class=" font-size-3 color_o font600">중고 악기</span>
					</div>
				</div>
			
					
				<div class="my-top-8 mr-1 ml-1">
					<ul>
						<c:forEach items="${fleaList }" var="fleaList" varStatus="status">
							<li class="border-bottom">
								<div class="">
									<div class="my-top-4 my-bottom-4">
										<span>${fleaList.user_name }</span>
										<span> | </span>
										<span>${fleaList.flea_date }</span>
									</div>
									<div class="flex float-right items-center width-13rem justify-between">
										<img class="icon" id="" style="width:2rem;" src="/resources/include/images/hits.svg">
										<span class="font-size-1 ml-05">${fleaList.flea_hits }</span>
										<img class="icon ml-2" id="" style="width:2rem;" src="/resources/include/images/reply.svg">
										<span class=" ml-05">${fleaList.flea_reply_cnt }</span>
									</div>
									<div class="my-bottom-4">
										<a class="font-weight-bold font-size-1" href="/fleaMarket/fleaMarketDetail/${fleaList.flea_no }" >${fleaList.flea_title }</a>
									</div>
										
								</div>
							</li>
						</c:forEach>
					</ul>
				</div>
			</div>
		
			<!-- 커뮤니티 글 리스트 -->
			<div class="w-50rem">
				
				<div class="mainListDiv" id="comList">
					<div class="my-top-75 ml-2">
						<span id="comList_text" class=" font-size-3 color_o font600">커뮤니티</span>
					</div>
				</div>
				
				<div class="my-top-8 mr-1 ml-1">
					<ul>
						<c:forEach items="${comList }" var="comList" varStatus="status">
							<li class="border-bottom">
								<div class="">
									<div class="my-top-4 my-bottom-4">
										<span>${comList.user_name }</span>
										<span> | </span>
										<span>${comList.com_date }</span>
									</div>
									<div class="flex float-right items-center width-13rem justify-between">
										<img class="icon" id="" style="width:2rem;" src="/resources/include/images/hits.svg">
										<span class="font-size-1 ml-05">${comList.com_hits }</span>
										<img class="icon ml-2" id="" style="width:2rem;" src="/resources/include/images/reply.svg">
										<span class=" ml-05">${comList.com_reply_cnt }</span>
									</div>
									<div class="my-bottom-4">
										<a class="font-weight-bold font-size-1" href="/community/communityDetail/${comList.com_no }" >${comList.com_title }</a>
									</div>
										
								</div>
							</li>
						</c:forEach>
					</ul>
				</div>
			</div>
		</div>
	</div>
	

</body>
</html>